package org.circuitsoft.slack.api.web;

public class Response {
 
    private int statusCode;
    private String data;
    private String Contenttype;
 
    public Response(){
        this("<html>" +
                "<body>" +
                    "<h1>Empthy Response!</h1>" +
                "</body>" +
            "</html>");
    }
 
    public Response(String data){
        this(200, data, "text/html");
    }

    public Response(int code){
        this(code, "", "text/html");
    }
 
    public Response(int statusCode, String data, String contenttype){
        this.statusCode = statusCode;
        this.data = data;
    }
 
    public int getStatusCode() {
        return statusCode;
    }
 
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
 
    public void setData(String data) {
        this.data = data;
    }
 
    public String getData() {
        return data;
    }
 
    public String getContenttype() {
        return Contenttype;
    }
 
    public void setContenttype(String contenttype) {
        Contenttype = contenttype;
    }
}