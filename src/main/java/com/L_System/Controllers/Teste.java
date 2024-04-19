package com.L_System.Controllers;

import java.util.HashMap;
import java.util.Map;

import com.L_System.L_API.Controller;
import com.L_System.L_API.Annotation.Route;
import com.L_System.L_API.HTTP.Request;
import com.L_System.L_API.HTTP.Response;
import com.L_System.L_API.HTTP.StatusCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Teste extends Controller {

    public Teste(Request request, ObjectNode routeParans) {
        super(request, routeParans);
    }

    @Route(method = "GET", path = "/person")
    public Response home() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Leandro de Meirelles");
        map.put("age", 22);
        map.put("city", "Lorena-SP");

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String json = objectMapper.writeValueAsString(map);
            return new Response("{\"error\":false,\"content\":" + json + "}").JSON()
                    .StatusCode(StatusCode.OK);
        } catch (JsonProcessingException e) {
            return new Response("{\"error\":true,\"content\":\"\"}").ERROR()
                    .StatusCode(StatusCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Route(method = "GET", path = "/rota/{int:id}")
    public int rota(int id) {
        return id;
    }

    @Route(method = "GET", path = "/tela/{str:valor}")
    public String tela(String valor) {
        return valor;
    }
}
