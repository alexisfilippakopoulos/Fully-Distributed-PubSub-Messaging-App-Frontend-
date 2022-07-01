package com.ds;

import java.util.ArrayList;

public interface Publisher extends Node{

    public ArrayList<Value> generateChunks(Value v);


    public void getBrokerList();
    public BrokerInterface hashTopic(String topic);
    public void notifyBrokersNewMessage(String message);
    public void notifyFailure(BrokerInterface broker);

    public void push(Value s);

}
