package com.ds;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ProfileName {

    private String username;
    private Date dateAccountCreated;
    private MultimediaFile profilePic;
    private ArrayList <Date> dateRegistered, storyCreationDate;
    private ArrayList<String> friendsList, notifications, friendRequestsSent, subbedTopics, blockList;
    private HashMap<String, ArrayList<Value>> userVideoFilesMap;
    private ArrayList<MultimediaFile> stories, subbedTopicsImages;
    private String bio = "";
    //MultimediaFile defaultPFP = new MultimediaFile("res/defaultpfp.png",null);

    public ProfileName(String username) {
        this.username = username;
        this.dateAccountCreated = new Date();
        this.dateRegistered = new ArrayList<>();
        this.subbedTopics = new ArrayList<>();
        this.userVideoFilesMap = new HashMap<>();
        this.friendsList = new ArrayList<>();
        this.notifications = new ArrayList<>();
        this.subbedTopicsImages = new ArrayList<>();
        this.friendRequestsSent = new ArrayList<>();
        this.stories = new ArrayList<>();
        this.blockList = new ArrayList<>();
        this.storyCreationDate = new ArrayList<>();
        this.bio = bio;
        //this.profilePic = defaultPFP;
    }

    public void addFriend(String friend) {
        friendsList.add(friend);
    }

    public void unfriend(String friend) {
        friendsList.remove(friend);
    }

    public void removeNotification(int i) {
        notifications.remove(i);
    }

    public void addNotification(String notification) {
        notifications.add(notification);
    }

    public void addFriendRequest(String friendName) {
        friendRequestsSent.add(friendName);
    }

    public void removeFriendRequest(String name) {
        friendRequestsSent.remove(name);
    }


    public void blockUser(String name){
        blockList.add(name);
        if(friendsList.contains(name)){
            unfriend(name);
        }
    }

    public void removeBlockedUser(String name){
        blockList.remove(name);
    }

    public void addStory(MultimediaFile story) {
        stories.add(story);
        storyCreationDate.add(new Date());
    }
    public void removeStory(int index){
        stories.remove(index);
        storyCreationDate.remove(index);
    }

    public ArrayList<MultimediaFile> getStories(){
        return stories;
    }

    public ArrayList<Date> getStoryCreationDate() {
        return storyCreationDate;
    }
    public ArrayList<String> getBlockList(){
        return blockList;
    }

    //getters & setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getDateAccountCreated() {
        return dateAccountCreated;
    }

    public void setDateAccountCreated(Date dateAccountCreated) {
        this.dateAccountCreated = dateAccountCreated;
    }

    public ArrayList<Date> getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(ArrayList<Date> dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    public ArrayList<String> getSubbedTopics() {
        return subbedTopics;
    }

    public MultimediaFile getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(MultimediaFile profilePic) {
        this.profilePic = profilePic;
    }

    public ArrayList<String> getNotifications() {
        return notifications;
    }

    public void setNotifications(ArrayList<String> notifications) {
        this.notifications = notifications;
    }

    public ArrayList<String> getFriendsList() {
        return friendsList;
    }

    public HashMap<String, ArrayList<Value>> getUserVideoFilesMap() {
        return userVideoFilesMap;
    }

    public ArrayList<String> getFriendRequestsSent() {
        return friendRequestsSent;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public ArrayList<MultimediaFile> getSubbedTopicsImages() {
        return subbedTopicsImages;
    }

    public void setSubbedTopicsImages(ArrayList<MultimediaFile> subbedTopicsImages) {
        this.subbedTopicsImages = subbedTopicsImages;
    }

}