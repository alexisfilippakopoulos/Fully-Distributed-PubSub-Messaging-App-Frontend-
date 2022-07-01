package com.ds;

import java.io.Serializable;

public class Value implements Serializable {
    private static final long serialVersionUID = -2984529267217354395L;
    private MultimediaFile multiMediaFile;
    private String message, sender;
    private boolean command;

    public Value() {}

    public Value(String message, MultimediaFile file) {
        this.message = message;
        this.multiMediaFile = file;
        this.sender = file.getProfileName();
    }

    public Value(String message)
    {
        this.message = message;
    }

    public Value(MultimediaFile file)
    {
        multiMediaFile = file;
        this.sender = file.getProfileName();
    }

    public Value(MultimediaFile file, byte[] FileChunk){
        multiMediaFile = new MultimediaFile(file, FileChunk);
        this.sender = file.getProfileName();
    }

    public String getDisplayMessage() {
        String final_message = "";
        if(sender != null) {
            final_message += sender + ": ";
        }
        final_message += message;
        return final_message;
    }

    //getters & setters
    public MultimediaFile getMultiMediaFile() {
        return multiMediaFile;
    }
    public void setMultiMediaFile(MultimediaFile multiMediaFile) {
        this.multiMediaFile = multiMediaFile;
    }
    public String getName(){
        return multiMediaFile.getFileName();
    }
    public String getProfileName(){
        return multiMediaFile.getProfileName();
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }
    public boolean isCommand() {
        return command;
    }
    public void setCommand(boolean command) {
        this.command = command;
    }

    @Override
    public String toString() {
        String s = "";
        if (message!=null){
            s += "Message: " + message + "\n";
        }
        if (multiMediaFile!=null) {
            s += multiMediaFile;
        }
        if (s.equalsIgnoreCase("")) {
            s = "empty_message";
        }
        return s;
    }
}
