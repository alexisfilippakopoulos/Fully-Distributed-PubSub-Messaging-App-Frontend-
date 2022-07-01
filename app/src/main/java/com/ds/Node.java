package com.ds;

import java.util.List;

public interface Node {

    List<BrokerInterface> brokers = null;

    public void connect(String ip, int port);
    public void disconnect();
    public void init();
    public void updateNodes();

}
