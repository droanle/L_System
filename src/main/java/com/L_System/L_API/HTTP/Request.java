package com.L_System.L_API.HTTP;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Request {
    private ObjectMapper objectMapper = new ObjectMapper();
    private HttpExchange Request;

    public Method method;
    public String path;
    public JsonNode parameters = null;
    public JsonNode body = null;

    public Request(HttpExchange exchange) {
        this.Request = exchange;

        String requestMethod = exchange.getRequestMethod();
        if ("GET".equals(requestMethod)) {
            this.method = Method.GET;
        } else if ("POST".equals(requestMethod)) {
            this.method = Method.POST;
        } else if ("PUT".equals(requestMethod)) {
            this.method = Method.PUT;
        } else if ("DELETE".equals(requestMethod)) {
            this.method = Method.DELETE;
        } else {
            this.method = null;
        }

        this.path = exchange.getRequestURI().getPath();
    }

    public boolean BodyDecoding() {
        try {
            if (this.method == Method.POST || this.method == Method.PUT) {
                InputStream is = this.Request.getRequestBody();
                byte[] RequestBody = is.readAllBytes();

                this.body = this.objectMapper.readTree(RequestBody);

                return false;
            } else
                return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void ParameterDecoding() {
        try {
            URI uri = this.Request.getRequestURI();
            String queryString = uri.getQuery();
            Map<String, String> queryMap = new HashMap<>();

            if (queryString != null) {
                String[] pairs = queryString.split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    queryMap.put(
                            URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8),
                            URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8));
                }

                this.parameters = objectMapper.valueToTree(queryMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
