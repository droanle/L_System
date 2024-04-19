package com.L_System.L_API;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.L_System.L_API.Route.RouteProvider;
import com.L_System.L_API.HTTP.Request;
import com.L_System.L_API.HTTP.Response;
import com.L_System.L_API.HTTP.ResponseType;
import com.sun.net.httpserver.HttpServer;

public class Server {
    private HttpServer server;
    private ArrayList<Route> routeList = new ArrayList<>();

    public Server(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new RouteHendler());

        server.setExecutor(null);
    }

    @SuppressWarnings("unchecked")
    public void addControllers(Class<? extends Controller>... classes) {
        for (Class<? extends Controller> clazz : classes) {
            String clazzName = clazz.getSimpleName().toLowerCase();

            Route route = new Route(clazzName);

            for (Method classMethod : clazz.getDeclaredMethods()) {
                com.L_System.L_API.Annotation.Route classMethodRouteAnnotation = classMethod
                        .getDeclaredAnnotation(com.L_System.L_API.Annotation.Route.class);

                try {
                    ArrayList<String> path;
                    if (classMethodRouteAnnotation.path().equals("/"))
                        path = new ArrayList<String>() {
                            {
                                add("");
                            }
                        };
                    else {
                        path = new ArrayList<>(
                                Arrays.asList(
                                        classMethodRouteAnnotation.path().split("/")));

                        if (path.get(0).equals(""))
                            path.remove(0);
                    }

                    route.addRouter(
                            path,
                            com.L_System.L_API.HTTP.Method.fromString(classMethodRouteAnnotation.method()),
                            classMethod);
                } catch (Exception e) {
                    throw (e);
                }
            }

            routeList.add(route);
        }
    }

    public void serializeRouteList() {
        try (FileWriter fileWriter = new FileWriter("serializeRouteList.json")) {
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < routeList.size(); i++) {
                json.append(routeList.get(i).serialize());
                if (i != routeList.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");

            fileWriter.write(json.toString());
            System.out.println("JSON written to serializeRouteList.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        server.start();
        System.out.println("Servidor iniciado");
    }

    public void stopAfterDelay(long delay) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                server.stop(0);
                System.out.println("Servidor parado apos " + delay + " milissegundos");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public class RouteHendler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                Request request = new Request(exchange);
                RouteProvider correctRoute = null;

                for (Route route : routeList) {
                    correctRoute = route.findRoute(request.path, request.method);
                }

                if (correctRoute == null)
                    exchange.sendResponseHeaders(404, 0); // Not Found
                else {

                    Response response = correctRoute.make(request);

                    exchange.sendResponseHeaders(response.statusCode.code, response.length());

                    if (response.type == ResponseType.JSON)
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                    else if (response.type == ResponseType.Text)
                        exchange.getResponseHeaders().set("Content-Type", "text/plain");
                    else
                        exchange.getResponseHeaders().set("Content-Type", "application/json");

                    OutputStream os = exchange.getResponseBody();
                    os.write(response.content.getBytes(StandardCharsets.UTF_8));
                    os.close();
                }
            } catch (Exception e) {
                exchange.sendResponseHeaders(500, 0); // Internal Server Error
            } finally {
                exchange.close();
            }
        }

    }
}
