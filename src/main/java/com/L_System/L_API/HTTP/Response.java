package com.L_System.L_API.HTTP;

import com.fasterxml.jackson.databind.JsonNode;

public class Response {
    public ResponseType type;
    public String content = "";
    public StatusCode statusCode = StatusCode.OK;

    public Response() {
    }

    public Response(int content) {
        this.content = String.valueOf(content);
    }

    public Response(String content) {
        this.content = content;
    }

    public Response(JsonNode content) {
        this.content = content.toString();
    }

    public int length() {
        return content.length();
    }

    public Response Content(String content) {
        this.content = content;
        return this;
    }

    public Response Content(JsonNode content) {
        this.content = content.toString();
        return this;
    }

    public Response JSON() {
        type = ResponseType.JSON;
        return this;
    }

    public Response Text() {
        type = ResponseType.Text;
        return this;
    }

    public Response ERROR() {
        type = ResponseType.ERROR;
        return this;
    }

    public Response StatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
        return this;
    }
}
