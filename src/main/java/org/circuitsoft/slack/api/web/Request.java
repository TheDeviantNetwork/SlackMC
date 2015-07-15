package org.circuitsoft.slack.api.web;
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.Callable;

public class Request implements Callable<Request> {
 
    private PrintStream out;
    private BufferedReader in;
 
    private String method;
    private String path;
    private Map<String, String> header = new HashMap<String, String>();
    private String payLoad;
    private Socket socket;
    private SlackWebServer server;
 
    public Request(Socket socket, SlackWebServer server) throws IOException {
        this.socket = socket;
        this.server = server;
        out = new PrintStream(socket.getOutputStream());
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
 
    public Map<String, String> getHeader() {
        return header;
    }
 
    public String getMethod() {
        return method;
    }
 
    public String getPath() {
        return path;
    }
 
    public String getPayLoad() {
        return payLoad;
    }
 
    @Override
    public Request call() throws Exception {
        try {
            String[] firstline = in.readLine().split(" ");
            method = firstline[0];
            path = firstline[1];
 
            while (true){
                String l = in.readLine();
                if (l.equals(""))
                    break;
                if (l.contains(":")) {
                    String[] headerLine = l.split(":");
                    header.put(headerLine[0], headerLine[1].substring(1));
                }
            }
 
            if (header.containsKey("Content-Length")){
                int lenght = Integer.parseInt(header.get("Content-Length"));
                char[] buf = new char[lenght];
                in.read(buf, 0, buf.length);
                payLoad = java.net.URLDecoder.decode(String.valueOf(buf), "UTF-8");
            }

            handleResponse();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return this;
    }
 
    private void handleResponse() throws IOException {
        Response response = server.onRequest(this);
        socket.close();
    }
}