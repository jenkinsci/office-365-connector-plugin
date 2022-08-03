package jenkins.plugins.office365connector;

import org.apache.commons.lang.StringUtils;

public class Proxy {

    private String ip;
    private Integer port;
    private String username;
    private String password;


    public Proxy(String ip, Integer port, String username, String password) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean proxyConfigured() {
        return (!StringUtils.isEmpty(this.ip) && !StringUtils.isEmpty(String.valueOf(this.port)));
    }
}
