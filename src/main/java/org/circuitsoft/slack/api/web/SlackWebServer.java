package org.circuitsoft.slack.api.web;

import org.circuitsoft.slack.api.SlackCollector;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

public class SlackWebServer implements Runnable {
 
    private ServerSocket serverSocket;
    private SlackCollector collector;
    private boolean running = true;

    public static void main(String[] args){
        try {
            new SlackWebServer(8080, new SlackCollector() {
                @Override
                public void onMessage(Map<String, String> message) {
                    System.out.println(message);
                }
            }).run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    public SlackWebServer(int port, SlackCollector collector) throws IOException {
        this.collector = collector;
        this.serverSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        while (running) {
            try {
                new Request(serverSocket.accept(), SlackWebServer.this).call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

 
    protected Response onRequest(Request request){
        try {
            Map<String, String> data = new HashMap<>();
            for (String keyline: request.getPayLoad().split("&")){
                String[] keydata = keyline.split("=");
                data.put(keydata[0], keydata[1]);
            }
            if (!data.get("user_id").equals("USLACKBOT"))
                collector.onMessage(data);
            return new Response(200);
        }catch (Exception e){
            return new Response(400);
        }
    }

    public void setRunning(boolean running) {
        try {
            serverSocket.close();
        } catch (IOException e) {}
        this.running = running;
    }
}