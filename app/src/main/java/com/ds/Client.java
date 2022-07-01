package com.ds;

import android.content.Context;


import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class Client implements Publisher, Consumer
{
    private static final String EXIT_CHAT_COND = "quit_topic";
    private static final String SEND_FILE = "send_file";
    private static final String SECRET_CHAT = "secret_chat";
    private static final String SET_NICKNAME = "set_nickname";
    private static final String FRIEND_REQUEST = "friend_request";
    private static final String FRIEND_REQUEST_RESPONSE = "friend_request_response";
    private static final String SET_TOPIC_NAME = "set_topic_name";

    //Profile Information
    private ProfileName profile;
    private ArrayList<String> retrieved_topics;
    private ArrayList<MultimediaFile> retrieved_topic_images;
    private String userPath, othersStoriesPath, storyPath, savedMedia, topics;
    private final long story_deletion_delay = 1000 * 60;//1000 ms * X sec
    private MultimediaFile defaultTopicImage, defaultUserImage;
    private int get_stories_count=-1;
    private ArrayList<Value> chatMessages = new ArrayList<>();
    //private int chat_history_len;
    boolean secretToggle;
    private HashMap<String,String> nicknames;




    Context context;

    //IO Objects
    private Socket socket;
    private ObjectInputStream reader;
    private ObjectOutputStream writer;
    private String currentBroker;

    /**
     * Constructs the object, creates the user path, and loads subbed topics
     * @param socket
     * @param username
     * @param ois
     * @param oos
     */
    public Client(Socket socket, String username, ObjectInputStream ois, ObjectOutputStream oos) {
        try {
            //Profile Information Init
            profile = new ProfileName(username);
            //IO Objects Init
            this.socket = socket;
            this.reader = ois;
            this.writer = oos;
            this.userPath = "user/" + getUsername();
            this.storyPath = "/stories/";
            this.othersStoriesPath = "/others_stories/";
            this.savedMedia = "/saved media/";
            this.topics = "/topics/";
            this.secretToggle = false;
            this.nicknames = new HashMap<>();
            //loadSubbedTopics(userPath + "/subbed_topics.txt");
        } catch (Exception e) {
            closeEverything();
        }
    }

    /**
    * A thread that accepts chat messages, checking for special messages for features such
    * as friend requests, redirection/reconnection to other broker etc
    */
    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(8);
                Value msgFromGroupChat;
                while(socket.isConnected()){
                    try{
                        msgFromGroupChat = (Value) reader.readObject();
                        System.out.println(msgFromGroupChat);
                        if (msgFromGroupChat.getMessage() != null) {
                            String msg = msgFromGroupChat.getMessage();
                            System.out.println(msgFromGroupChat);
                            System.out.println(msgFromGroupChat.isCommand());
                            //check if message is friend request response
                            if (msgFromGroupChat.isCommand()){
                                if (msg.contains(FRIEND_REQUEST_RESPONSE)) {
                                    String[] info = msg.split(" ");
                                    String name = info[1];
                                    String answer = info[2];
                                    if (answer.equals("yes")) {
                                        profile.addFriend(name);
                                    } else {
                                        profile.removeFriendRequest(name);
                                    }
                                    continue;
                                }
                                //check if message is friend request
                                else if (msg.contains("friend request")) {
                                    profile.addNotification(msg);
                                    continue;
                                }
                                //check if message is topic retrieval from broker
                                else if (msg.equalsIgnoreCase("RETRIEVE_TOPICS")) {
                                    ArrayList<String> topic_names = (ArrayList<String>) reader.readObject();  //get topic names
                                    ArrayList<MultimediaFile> topic_images = (ArrayList<MultimediaFile>) reader.readObject();  //get topic images
                                    retrieved_topics = topic_names;
                                    retrieved_topic_images = topic_images;
                                    continue;
                                }
                                //check if client asked for story
                                else if (msg.equalsIgnoreCase("GET_STORY")) {
                                    get_stories_count = ((Integer) reader.readObject());
                                    while (get_stories_count != 0) {
                                        MultimediaFile story = ((Value) reader.readObject()).getMultiMediaFile();
                                        System.out.println(story);
                                        saveFile(story, othersStoriesPath);
                                        scheduleDeletion(story, othersStoriesPath);
                                        get_stories_count--;
                                    }
                                    get_stories_count = -1;
                                    continue;
                                }
                                //check for topic image change
                                else if(msg.contains("get_topic_image")){
                                    Value v = (Value) reader.readObject();
                                    String[] info = msg.split(" ");
                                    String topicName = info[1];
                                    getProfile().getSubbedTopicsImages().set(getSubbedTopics().indexOf(topicName),v.getMultiMediaFile());
                                    continue;
                                }
                                //check for broker message
                                else if (msg.equals("WRONG_BROKER")) {
                                    String correct_address = ((Value) reader.readObject()).getMessage();
                                    currentBroker = correct_address;
                                    String topic = (((Value) reader.readObject()).getMessage());
                                    String[] info = correct_address.split(":");
                                    disconnect();
                                    reconnect(new BrokerAddressInfo(info[0], Integer.parseInt(info[1])), topic);
                                    continue;
                                }
                                //broker is down, redirect if client is in menu (not in chat)
                                else if(msg.equals("WRONG_BROKER_NOTOPIC")){
                                    String correct_address = ((Value)reader.readObject()).getMessage();
                                    currentBroker = correct_address;
                                    String[] info = correct_address.split(":");
                                    disconnect();
                                    reconnect1(new BrokerAddressInfo(info[0], Integer.parseInt(info[1])));
                                    continue;
                                }
                                //get default pfp and topic images
                                else if (msg.equals("GET_DEFAULT_IMAGES")) {
                                    defaultTopicImage = ((Value) reader.readObject()).getMultiMediaFile();  //get default topic image
                                    defaultUserImage = ((Value) reader.readObject()).getMultiMediaFile();  //get default user image
                                }
                                //set topic name
                                else if (msg.contains(SET_TOPIC_NAME)) {
                                    String names[] = msg.split(" ");
                                    String oldName = names[1];
                                    String newName = names[2];
                                    getSubbedTopics().set(getSubbedTopics().indexOf(oldName), newName);
                                }
                            }
                            //lastly, its a message for the chat
                            if (!profile.getBlockList().contains(msgFromGroupChat.getSender()) && !msgFromGroupChat.isCommand()){
                                System.out.println(msgFromGroupChat.getDisplayMessage());
                                chatMessages.add(msgFromGroupChat);
                            }
                        }
                        if (msgFromGroupChat.getMultiMediaFile() != null) {
                            saveFile((MultimediaFile)msgFromGroupChat.getMultiMediaFile(), savedMedia);
                            chatMessages.add(msgFromGroupChat);
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                        closeEverything();
                    }
                }
            }
        }).start();
    }

    /**
     * Enters a topic conversation
     * @param topic
     */
    public void initiateChat(String topic){
        push(new Value("MESSAGE"));
        push(new Value(topic));
        Scanner sc = new Scanner(System.in);
        while (socket.isConnected()){
            String messageToSend = sc.nextLine();
            if (messageToSend.length() > 512*1024) {
                try {
                    File temp = new File("user/"+getUsername()+"/temp_msg.txt");
                    FileWriter fileWriter = new FileWriter(temp);
                    fileWriter.write(messageToSend);
                    MultimediaFile fileToSend = new MultimediaFile(temp, getUsername());
                    push(new Value(fileToSend));
                    fileWriter.close();
                    temp.deleteOnExit();
                    continue;
                } catch (Exception e) {e.printStackTrace();}
            }
            //exit chat
            if (messageToSend.equalsIgnoreCase(EXIT_CHAT_COND)) {
                push(new Value("quit_topic"));
                System.out.println("Exited topic..");
                break;
            }
            //send multimedia file
            else if (messageToSend.contains(SEND_FILE)) {
                String file_path = messageToSend.substring(SEND_FILE.length()+1);
                MultimediaFile fileToSend = new MultimediaFile(file_path, getUsername());
                if (fileToSend.getFileName() == null) {
                    System.out.println("File not found (" + file_path + ")");
                    continue;
                }
                push(new Value(fileToSend));
                continue;
            }
            //enter secret chat
            else if (messageToSend.equals(SECRET_CHAT)) {
                push(new Value(messageToSend));
                continue;
            }
            //set your nickname in the topic
            else if (messageToSend.contains(SET_NICKNAME)) {
                push(new Value(messageToSend));
                continue;
            }
            //else just send a normal string message
            push(new Value(messageToSend));
        }
    }

    /**
     * Sends a Value to the clientHandler. It can be a string for the chat or a file
     * @param value
     */
    @Override
    public void push(Value value) {
        try {
            //send message
            value.setSender(getUsername());
            if (value.getMessage() != null) {
                writer.writeObject(value);
                writer.flush();
            }
            //send media
            if (value.getMultiMediaFile() != null) {
                //if the same media is sent again => media is cached, retrieve the chunks
                if (profile.getUserVideoFilesMap().containsKey(value.getMultiMediaFile().getFileName())) {
                    ArrayList<Value> chunks = profile.getUserVideoFilesMap().get(value.getMultiMediaFile().getFileName());
                    for (Value chunk : chunks) {
                        writer.writeObject(chunk);
                        writer.flush();
                    }
                    return;
                }
                //else generate chunks
                ArrayList<Value> chunks = generateChunks(value);
                profile.getUserVideoFilesMap().put(value.getMultiMediaFile().getFileName(), chunks);
                for (Value chunk : chunks) {
                    writer.writeObject(chunk);
                    writer.flush();
                }
                writer.writeObject(value);
                writer.flush();
            }
        } catch (Exception e){
            closeEverything();
        }
    }

    /**
     * Pushes a Value to the clientHandler containing the topic to subscribe
     * @param topicName
     * @param topicImage
     */
    public void sub(String topicName, MultimediaFile topicImage) {
        if (!profile.getSubbedTopics().contains(topicName)) {
            push(new Value("SUB"));
            push(new Value(topicName));
            nicknames.put(topicName,getUsername());
            profile.getSubbedTopics().add(topicName);
            profile.getDateRegistered().add(new Date());
            profile.getSubbedTopicsImages().add(topicImage);
            //saveTopic(topic);
        } else {
            System.out.println("You are already subscribed");
        }
    }

    @Override  //PLACEHOLDER FOR THE INTERFACE
    public void register(String topicName) {}

    /**
     * Pushes a Value to the clientHandler containing the topic to create
     * It creates a topic and automatically subscribes this user to the topic
     * @param topicName
     * @param topicImage
     */
    public void register(String topicName, MultimediaFile topicImage) {
        if (!profile.getSubbedTopics().contains(topicName)) {
            push(new Value("CREATE"));
            push(new Value(topicName));
            profile.getSubbedTopics().add(topicName);
            profile.getDateRegistered().add(new Date());
            profile.getSubbedTopicsImages().add(topicImage);
            //saveTopic(topic);
        } else {
            System.out.println("You are already subscribed");
        }
    }

    /**
     * Saves a topic in the subbed_topics.txt file located in the directory of the user
     * @param topic
     */
    public void saveTopic(String topic) {
        //saves topic to subbed_topics.txt
        try (FileWriter fw = new FileWriter(userPath + "/subbed_topics.txt", true)) {
            fw.append(topic);
            fw.append('\n');
            fw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Unsubscribes a user from a topic
     * @param topic
     */
    public void unsubscribe(String topic) {
        if (profile.getSubbedTopics().contains(topic)) {
            push(new Value("UNSUB"));
            push(new Value(topic));
            int index = profile.getSubbedTopics().indexOf(topic);
            profile.getSubbedTopics().remove(topic);
            profile.getSubbedTopicsImages().remove(index);
            try {
                //removes the topic from subbed_topics.txt
//                File file = new File(userPath+"/subbed_topics.txt");
//                List<String> out = Files.lines(file.toPath())
//                        .filter(line -> !line.contains(topic))
//                        .collect(Collectors.toList());
//                Files.write(file.toPath(), out, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("You are not subscribed to the topic " + topic);
        }
    }

    /**
     * Loads the subscribed topics of the user from previous sessions from the subbed_topics.txt file
     * @param user_path
     */
    private void loadSubbedTopics(String user_path) {
        try {
            File savedSubbedTopics = new File(user_path);
            if (!savedSubbedTopics.exists()) {
                savedSubbedTopics.createNewFile();
                return;
            }
            if (savedSubbedTopics.length() == 0) {
                return;
            }
            Scanner scanner = new Scanner(savedSubbedTopics);
            while (scanner.hasNext()) {
                profile.getSubbedTopics().add(scanner.nextLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean getSecretToggle (){
        return secretToggle;
    }

    public void setSecretToggle (boolean secretToggle){
        this.secretToggle = secretToggle;
    }

    /**
     * Breaks the Value given to chunks if the MultimediaFile contained in the Value is larger
     * than 512 kb
     * @param value
     * @return An array list of type Value. Each position is a chunk of the original Value
     */
    @Override
    public ArrayList<Value> generateChunks(Value value) {
        MultimediaFile file = value.getMultiMediaFile();
        ArrayList<Value> chunks = new ArrayList<Value>();
        int sizePerChunk = 524288;  //512kb = 512*1024 byte
        try {
            if(file.getLength() > sizePerChunk) {
                int numberOfChunks = (int) file.getLength() / sizePerChunk;
                for (int i = 0; i < numberOfChunks; i++) {
                    byte[] currentChunk = new byte[sizePerChunk];
                    for (int j = 0; j < sizePerChunk; j++) {
                        currentChunk[j] = file.getMultimediaFileChunk()[j + i * sizePerChunk];
                    }
                    Value temp_value = new Value(file, currentChunk);
                    temp_value.getMultiMediaFile().setMf(1);
                    chunks.add(temp_value);
                }

                int remainingChunk = (int) file.getLength() % sizePerChunk;
                byte[] currentChunk = new byte[remainingChunk];
                for (int j = 0; j < remainingChunk; j++) {
                    currentChunk[j] = file.getMultimediaFileChunk()[j + (int)file.getLength()-remainingChunk];
                }
                Value temp_value = new Value(file, currentChunk);
                chunks.add(temp_value);
            }else{
                chunks.add(value);
            }
            chunks.get(chunks.size()-1).getMultiMediaFile().setMf(0);
        }catch(Exception e){
            e.printStackTrace();
        }
        return chunks;
    }

    /**
     * Saves a file locally to the specified path
     * @param mmf
     * @param path
     */
    public void saveFile(MultimediaFile mmf, String path){
        try{
            File file_path = new File( path+mmf.getFileName());
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            //os.write(mmf.getMultimediaFileChunk());
            //byte[] data = os.toByteArray();
            FileOutputStream fos = new FileOutputStream(file_path.getAbsolutePath());
            System.out.println(file_path.getAbsolutePath());
            fos.write(mmf.getMultimediaFileChunk());
            fos.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void scheduleDeletion(MultimediaFile story, String path) {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                synchronized (this){
                    try {
                        File storyfile = new File(path + story.getFileName());
                        FileWriter fw = new FileWriter(storyfile);
                        if (storyfile.exists()) {
                            fw.close();
                            System.out.println(storyfile.delete());
                        }
                        if (path.equals(storyPath)) {
                            int index = profile.getStories().indexOf(story);
                            profile.removeStory(index);
                        }
                    } catch (Exception e) {
                        System.out.println("File was not found");
                    }
                }
            }
        };
        System.out.println(story.getExpiryDate());
        timer.schedule(task, story.getExpiryDate());
    }

    /**
     * Using regex we prevent the user from giving invalid input when we ask
     * integers as input. e.g. "1q" is invalid, we need an int value
     * @param scanner
     * @return The int value that was given as input
     */
    public int chooseAction(Scanner scanner) {
        String action = scanner.nextLine();
        while (action.matches("[a-zA-Z]*$")) {
            System.out.print("Invalid input, choose action again: ");
            action = scanner.nextLine();
        }
        return Integer.parseInt(action);
    }

    /**
     * Closes all the connections of the user with the clientHandler - socket, input and output stream
     */
    public void closeEverything() {
        try {
            if(reader != null){
                reader.close();
            }
            if(writer != null){
                writer.close();
            }
            if(socket != null){
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("An error occurred - Exiting program");
            System.exit(0);
        }
    }

    /**
     * Send some initialization data to the clientHandler
     */
    @Override
    public void init() {
        try {
            writer.writeObject(profile.getUsername());
            writer.writeObject(profile.getBlockList());
            writer.flush();
            //defaultTopicImage = ((Value)reader.readObject()).getMultiMediaFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showConversationData(String topic, Value v) {

    }

    @Override
    public void connect(String ip, int port) {

    }

    /**
     * Reconnects to another broker when user needs to chat to a topic that does not belong to
     * the current one
     * @param address address of the broker to connect to
     * @param topic in which topic the client wants to connect
     */
    public void reconnect(BrokerAddressInfo address, String topic) {
        try {
            socket = new Socket(address.getIp(), address.getPort());
            writer = new ObjectOutputStream(socket.getOutputStream());
            writer.writeObject(getUsername());  //readObject ston broker
            reader = new ObjectInputStream(socket.getInputStream());
            init();  //readObject ston clientHandler
            writer.writeObject(new Value("MESSAGE"));
            writer.writeObject(new Value(topic));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reconnect1(BrokerAddressInfo address) {
        try {
            System.out.println(address);
            socket = new Socket(address.getIp(), address.getPort());
            writer = new ObjectOutputStream(socket.getOutputStream());
            writer.writeObject(getUsername());  //readObject ston broker
//            reader.reset();
            reader = new ObjectInputStream(socket.getInputStream());
            init();  //readObject ston clientHandler
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Calls <i>closeEverything</i> method
     */
    @Override
    public void disconnect() {
        closeEverything();
    }


    @Override
    public void updateNodes() {

    }

    @Override
    public void disconnect(String s) {
    }

    @Override
    public void getBrokerList() {

    }

    @Override
    public BrokerInterface hashTopic(String topic) {
        return null;
    }

    @Override
    public void notifyBrokersNewMessage(String message) {

    }

    @Override
    public void notifyFailure(BrokerInterface broker) {

    }



    public void addMsgToChat(Value value) {
        chatMessages.add(value);
    }

    // getters & setters
    public void setUsername(String username) {
        this.profile.setUsername(username);
    }

    public String getUsername(){
        return profile.getUsername();
    }

    public ArrayList<String> getSubbedTopics() {
        return profile.getSubbedTopics();
    }

    public ProfileName getProfile() {
        return profile;
    }

    public ArrayList<String> getNotifications() {
        return profile.getNotifications();
    }

    public String getNotification(int index) {
        return profile.getNotifications().get(index);
    }

    public String getCurrentBroker() {
        return currentBroker;
    }

    public void setCurrentBroker(String currentBroker) {
        this.currentBroker = currentBroker;
    }

    public String getUserPath() {
        return userPath;
    }

    public String getOthersStoriesPath() {
        return othersStoriesPath;
    }

    public String getStoryPath() {
        return storyPath;
    }

    public String getSavedMedia() {
        return savedMedia;
    }

    public Context getContext() {
        return context;
    }

    public ArrayList<String> getRetrieved_topics() {
        return retrieved_topics;
    }

    public ArrayList<MultimediaFile> getRetrieved_topic_images() {
        return retrieved_topic_images;
    }

    public MultimediaFile getDefaultUserImage() {
        return defaultUserImage;
    }

    public MultimediaFile getDefaultTopicImage() {
        return defaultTopicImage;
    }

    public int get_stories_count() {
        return get_stories_count;
    }

    public ArrayList<Value> getChatMessages() {
        return chatMessages;
    }

    public long getStory_deletion_delay() {
        return story_deletion_delay;
    }

    public void setChatMessages(ArrayList<Value> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public void setNicknames(HashMap<String, String> nicknames) {
        this.nicknames = nicknames;
    }

    public HashMap<String, String> getNicknames() {
        return nicknames;
    }

    public void setContext(Context context) {
        this.context = context;
        userPath = context.getFilesDir() + "/" + userPath;
        storyPath = userPath + storyPath;
        savedMedia = userPath + savedMedia;
        othersStoriesPath = userPath + othersStoriesPath;
        topics = userPath + topics;

        if (!Files.exists(Paths.get(context.getFilesDir() + "/user"))) {
            new File(context.getFilesDir() + "/user").mkdir();
        }

        if (!Files.exists(Paths.get(userPath))) {
            new File(userPath).mkdir();
        }
        if (!Files.exists(Paths.get(storyPath))) {
            new File(storyPath).mkdir();
        }
        if (!Files.exists(Paths.get(savedMedia))) {
            new File(savedMedia).mkdir();
        }
        if (!Files.exists(Paths.get(othersStoriesPath))) {
            new File(othersStoriesPath).mkdir();
        }
        if (!Files.exists(Paths.get(topics))) {
            new File(topics).mkdir();
        }
    }

    @Override
    public String toString() {
        return  "Username: " + profile.getUsername() + "\n" +
                "Date account created: " + profile.getDateAccountCreated() + "\n" +
                "Bio: " + profile.getBio() + "\n" +
                "Subscribed topics: " + String.join(", ", profile.getSubbedTopics()) + "\n" +
                "Friends: " + String.join(", ", profile.getFriendsList()) + '\n' +
                "Current broker: " + currentBroker + '\n' +
                "Blocked users: " + String.join(", ", profile.getBlockList()) + '\n' +
                "Stories: " + profile.getStories();
    }
}
