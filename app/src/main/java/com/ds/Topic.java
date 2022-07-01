package com.ds;

import android.net.Uri;

import java.util.ArrayList;
import java.util.Date;

public class Topic
{
    String name;
    ArrayList<String> clientsSubbed;
    ArrayList<Value> messages;
    Date dateCreated;
    ArrayList<String> nicknames;
    MultimediaFile topicPicture;
    MultimediaFile defaultImg = new MultimediaFile("res/TopicImages/default.png",null);

    Topic(String name)
    {
        this.name = name;
        clientsSubbed = new ArrayList<String>();
        messages = new ArrayList<Value>();
        nicknames =new ArrayList<String>();
        dateCreated = new Date();
        this.topicPicture = defaultImg;
    }

    Topic(String name, String path)
    {
        this.topicPicture = new MultimediaFile(path,null);
        this.name = name;
        clientsSubbed = new ArrayList<String>();
        messages = new ArrayList<Value>();
        nicknames =new ArrayList<String>();
        dateCreated = new Date();
    }

    void subscribe(String client)
    {
        if (!clientsSubbed.contains(client))
        {
            clientsSubbed.add(client);
            nicknames.add(client.toString());
        }
    }

    void unsubscribe(String client)
    {
        if (clientsSubbed.contains(client))
        {
            clientsSubbed.remove(client);
        }
    }

    @Override
    public boolean equals(Object other_topic) {
        if ((Topic)other_topic == this) {
            return true;
        }
        if (!(other_topic instanceof Topic)) {
            return false;
        }

        if (this.name.equals(((Topic)other_topic).name)) {
            return true;
        } else {
            return false;
        }
    }

    void addMessage(Value message)
    {
        messages.add(message);
    }

    void addMessage(ArrayList<Value> msgs)
    {
        messages.addAll(msgs);
    }

    //getters & setters
    public Value getMessage(Value message)
    {
        return messages.get(messages.indexOf(message));
    }

    public Date getMessageDate(Value message){
        return messages.get(messages.indexOf(message)).getMultiMediaFile().getDateCreated();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getClientsSubbed() {
        return clientsSubbed;
    }

    public void setClients(ArrayList<Client> clients) {
        this.clientsSubbed = clientsSubbed;
    }

    public ArrayList<Value> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Value> messages) {
        this.messages = messages;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void setClientNickname(String clientUsername, String clientNickname){
        nicknames.set(nicknames.indexOf(clientUsername), clientNickname);
    }

    public ArrayList<String> getClientNicknames(){
        return nicknames;
    }

    public String getClientNickname(String username) {
        return nicknames.get(clientsSubbed.indexOf(username));
    }

    public String getClientUsernames() {
        return clientsSubbed.toString();
    }

    public MultimediaFile getTopicPicture() {
        return topicPicture;
    }

    public void setTopicPicture(MultimediaFile topicPicture) {
        this.topicPicture = topicPicture;
    }

    public String toString() {
        return "Topic name: " + name + "\n" +
                "Subscribers: " + String.join(", ", clientsSubbed) + "\n" +
                "Date created: " + dateCreated + "\n";
    }

}