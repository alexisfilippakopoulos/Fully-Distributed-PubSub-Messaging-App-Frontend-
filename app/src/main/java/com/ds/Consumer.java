package com.ds;


public interface Consumer extends Node{

    public void disconnect(String s);
    public void register(String topic);
    public void showConversationData(String topic, Value v);

}
