package com.ds;

import java.io.Serializable;

public class BrokerAddressInfo implements Serializable {

    private String ip;
    private int port;

    public BrokerAddressInfo(String ip, int port) {
        super();
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String toString() {
        return ip + ":" + port;
    }

    @Override
    public boolean equals(Object object) {
        boolean same = false;

        if (object != null && object instanceof BrokerAddressInfo) {
            same = ip.equals(((BrokerAddressInfo) object).ip) && port == ((BrokerAddressInfo) object).port;
        }

        return same;
    }


}