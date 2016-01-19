package org.powertrip.excalibot.common.plugins.ddos;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Ping extends Thread {

    private String address;
    private boolean state=false;
    private int timeout=500;

    public Ping(String address){
        this.address=address;
    }

    public Ping(String adress, int totaltime) {
        this.address = adress;
        this.timeout=totaltime;
    }

    @Override
    public void run() {
        Annoy_ping();
    }

    public boolean get_state(){
        return state;
    };
    
    private void Annoy_ping() {
        while (true) {
            try {
                InetAddress address2 = InetAddress.getByName(address);
                state=address2.isReachable(timeout);
            } catch (UnknownHostException e) {
                state=false;
                System.err.println("Unable to lookup");
            } catch (IOException e) {
                state=false;
                System.err.println("Unable to reach");
            }
        }
    }
}