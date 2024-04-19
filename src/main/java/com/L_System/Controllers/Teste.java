package com.L_System.Controllers;

import com.L_System.L_API.Controller;
import com.L_System.L_API.Annotation.Route;
import com.L_System.L_API.HTTP.Request;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Teste extends Controller {

    public Teste(Request request, ObjectNode routeParans) {
        super(request, routeParans);
    }

    @Route(method = "GET", path = "/")
    public void main() {

    }

    @Route(method = "PUT", path = "/oto")
    public void mainPost() {

    }

    @Route(method = "PUT", path = "/oto/e/oto")
    public void mainPut() {

    }

    @Route(method = "GET", path = "/{int:id}/ola")
    public String mainDelete(int id) {
        System.out.println("ola");
        return "ola";
    }

    @Route(method = "GET", path = "/get")
    public String get() {
        return "ola";
    }
}
