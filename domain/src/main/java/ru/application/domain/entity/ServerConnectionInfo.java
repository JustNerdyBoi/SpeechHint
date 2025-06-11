package ru.application.domain.entity;

public class ServerConnectionInfo {
    private final String ip;
    private final int port;

    public ServerConnectionInfo(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    public String getIp(){
        return ip;
    }

    public int getPort(){
        return port;
    }
}
