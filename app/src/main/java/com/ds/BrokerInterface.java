package com.ds;

import java.util.List;

public interface BrokerInterface extends Node {

    List<Topic> topics = null;


    void connect();

    public Consumer acceptConnection(Consumer consumer);
    public Publisher acceptConnection(Publisher publisher);
    public void calculateKeys();
    public void filterConsumers(String filter);
    public void notifyBrokersOnChanges();
    public void notifyPublisher(String notification);
    public void pull(String s);

}
