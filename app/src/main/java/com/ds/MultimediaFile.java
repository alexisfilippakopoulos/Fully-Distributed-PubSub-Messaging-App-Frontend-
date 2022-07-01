package com.ds;

import java.io.*;
import java.util.Date;

public class MultimediaFile implements Serializable {
    private static final long serialVersionUID = -2845096289187611411L;
    private String fileName, profileName;
    private Date dateCreated, expiryDate;
    private long length;
    private byte[] multimediaFileChunk;
    private int frameWidth, frameHeight, mf; //mf -> more fragments
    private String framerate;

    public MultimediaFile() {}

    public MultimediaFile(String path,String profileName)
    {
        try (FileInputStream fis = new FileInputStream(path)) {
            this.length = new File(path).length();
            this.fileName = path.substring(path.lastIndexOf('/')+1);
            this.dateCreated = new Date();
            this.profileName = profileName;
            multimediaFileChunk = new byte[(int)length];
            fis.read(multimediaFileChunk);
        } catch (Exception e) {
            System.out.println(e);
            this.fileName = null;
        }
    }

    public MultimediaFile(MultimediaFile other, byte[] chunk) {
        this.fileName = other.fileName;
        this.dateCreated = new Date();
        this.length = other.length;
        this.profileName = other.profileName;
        this.framerate = other.framerate;
        this.frameHeight = other.frameHeight;
        this.frameWidth = other.frameWidth;
        this.expiryDate = other.getExpiryDate();
        this.multimediaFileChunk = chunk;
    }

    public MultimediaFile(File file, String profileName) {
        try (FileInputStream fis = new FileInputStream(file)) {
            this.profileName = profileName;
            this.fileName = file.getName();
            this.dateCreated = new Date();
            this.length = file.length();
            this.multimediaFileChunk = new byte[(int)length];
            fis.read(multimediaFileChunk);
        } catch (Exception e) {
            System.out.println(e);
            this.fileName = null;
        }
    }

    public MultimediaFile(byte[] chunk, String fileName, String profileName) {
        this.profileName = profileName;
        this.fileName = fileName;
        this.dateCreated = new Date();
        this.multimediaFileChunk = chunk;
        this.length = chunk.length;
    }


    //setters & getters
    public String getFileExtension() {
        return this.fileName.substring(fileName.lastIndexOf('.')+1, fileName.length());
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public byte[] getMultimediaFileChunk() {
        return multimediaFileChunk;
    }

    public void setMultimediaFileChunk(byte[] multimediaFileChunk) {
        this.multimediaFileChunk = multimediaFileChunk;
    }

    public String getFramerate() {
        return framerate;
    }

    public void setFramerate(String framerate) {
        this.framerate = framerate;
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public void setFrameWidth(int frameWidth) {
        this.frameWidth = frameWidth;
    }

    public int getFrameHeight() {
        return frameHeight;
    }

    public void setFrameHeight(int frameHeight) {
        this.frameHeight = frameHeight;
    }

    public int getMf() {
        return mf;
    }

    public void setMf(int mf) {
        this.mf = mf;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Override
    public String toString() {
        return "File name: " + fileName + "\n" +
                "Type: " + getFileExtension() + "\n" +
                "Length: " + length + "\n" +
                "Date created: " + dateCreated + "\n";
    }
}
