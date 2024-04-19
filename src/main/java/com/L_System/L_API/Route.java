package com.L_System.L_API;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Pattern;

import com.L_System.L_API.HTTP.Method;
import com.L_System.L_API.HTTP.Request;
import com.L_System.L_API.HTTP.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Route {
    ObjectMapper objectMapper = new ObjectMapper();
    private ArrayList<Route> children = new ArrayList<>();

    private Method method = null;
    private String path;
    private java.lang.reflect.Method callback;

    private Boolean isRouteParam = false;
    private String routeParamType = "";
    private String routeParamName = "";

    public Route(String path) {
        this.path = path;
    }

    public Route(ArrayList<String> path, Method method, java.lang.reflect.Method callback) {
        this.path = path.get(0);

        if (Pattern.compile("\\{[a-zA-Z]+:[a-zA-Z]+\\}").matcher(this.path).matches()) {
            this.path = this.path.replaceAll("^\\{", "");
            this.path = this.path.replaceAll("}$", "");

            String[] routeParam = this.path.split(":");
            this.routeParamType = routeParam[0];
            this.routeParamName = routeParam[1];
            this.isRouteParam = true;
        }

        path.remove(0);

        if (path.size() >= 1)
            children.add(new Route(path, method, callback));
        else {
            this.method = method;
            this.callback = callback;
        }
    }

    public void addRouter(ArrayList<String> path, Method method, java.lang.reflect.Method callback) {
        String currentPath = path.get(0);

        if (this.children.size() == 0)
            children.add(new Route(path, method, callback));
        else {
            boolean routeAdded = false;

            for (Route route : this.children)
                if (route.isEquals(currentPath, method)) {
                    path.remove(0);
                    route.addRouter(path, method, callback);
                    routeAdded = true;
                }

            if (!routeAdded)
                children.add(new Route(path, method, callback));
        }
    }

    public Boolean isEquals(String path, Method method) {
        return this.path.equals(path) && this.method == method;
    }

    private Boolean validRouteParam(String param) {
        try {

            if (this.isRouteParam) {
                if (this.routeParamType.equals("int")) {
                    Integer.parseInt(param);
                }
            } else
                return false;

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    private RouteProvider findRoute(ArrayList<String> path, Method method, ObjectNode routeParams) {

        String currentPath = path.get(0);
        Route correctRoute = null;

        for (Route route : this.children) {
            if (route.validRouteParam(currentPath)) {

                if (path.size() == 1 && method != route.method)
                    continue;

                ArrayNode valorNode = objectMapper.createArrayNode();

                valorNode.add(route.routeParamType);

                if (route.routeParamType.equals("int"))
                    valorNode.add(Integer.parseInt(currentPath));
                else
                    valorNode.add(currentPath);

                routeParams.set(route.routeParamName, valorNode);

                correctRoute = route;
                break;
            } else if (route.isEquals(currentPath, method)) {
                correctRoute = route;
                break;
            }
        }

        if (correctRoute != null) {
            path.remove(0);

            if (path.size() == 0)
                return new RouteProvider(routeParams, correctRoute);
            else
                return correctRoute.findRoute(path, method, routeParams);
        } else
            return null;
    }

    public RouteProvider findRoute(String path, Method method) {
        ArrayList<String> pathSlisted = new ArrayList<>(Arrays.asList(path.split("/")));

        pathSlisted.remove(0);

        return this.findRoute(
                pathSlisted,
                method,
                new ObjectMapper().createObjectNode());
    }

    public class RouteProvider {
        public ObjectNode routeParams;
        public Route route;

        public RouteProvider(ObjectNode routeParams, Route route) {
            this.routeParams = routeParams;
            this.route = route;
        }

        public String make(Request request) {
            try {
                java.lang.reflect.Method callback = route.callback;

                Class<?> clazz = callback.getDeclaringClass();

                Constructor<?> constructor = clazz.getDeclaredConstructor(Request.class, ObjectNode.class);

                Object instancia = constructor.newInstance(request, routeParams);

                ArrayList<Object> parameters = new ArrayList<>();
                Iterator<String> routeParamsFieldNames = routeParams.fieldNames();
                ArrayList<Class<?>> types = new ArrayList<>(Arrays.asList(callback.getParameterTypes()));

                while (routeParamsFieldNames.hasNext()) {
                    String fieldName = routeParamsFieldNames.next();
                    JsonNode paramValue = routeParams.get(fieldName);
                    String valueType = paramValue.get(0).asText();
                    String value = paramValue.get(1).asText();

                    for (int i = 0; i < types.size(); i++) {
                        Class<?> type = types.get(i);

                        System.out.println("valueType: " + valueType);
                        System.out.println("type: " + type.getName());
                        System.out.println("int".equals(type.getName()));
                        System.out.println("int".equals(valueType));

                        if ("int".equals(valueType) && "int".equals(type.getName())) {
                            parameters.add(Integer.parseInt(value));
                            types.remove(i);
                        } else if ("str".equals(valueType) && "str".equals(type.getName())) {
                            parameters.add(value);
                            types.remove(i);
                            break;
                        }
                    }

                }

                Object result = callback.invoke(instancia, parameters.toArray());

                if (result instanceof Response) {
                    result = (Response) result;
                } else if (result instanceof Integer) {
                    result = (Integer) result;
                } else if (result instanceof String) {
                    result = (String) result;
                } else {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode jsonNode = mapper.valueToTree(result);
                    String json = mapper.writeValueAsString(jsonNode);
                    result = json;
                }

                return "alo";
            } catch (Exception e) {
                System.out.println(e);
            }

            return "not alo";
        }

    }

    private ObjectNode _serialize() {
        ObjectNode rootNode = objectMapper.createObjectNode();

        rootNode.put("path", path);
        rootNode.put("isRouteParam", isRouteParam);
        rootNode.put("routeParamName", routeParamName);
        rootNode.put("routeParamType", routeParamType);

        if (method != null)
            rootNode.put("method", method.name());

        if (callback != null)
            rootNode.put("callback", method.getDeclaringClass().getName() + callback.getName());

        ArrayNode childrenNode = objectMapper.createArrayNode();
        if (!children.isEmpty()) {
            for (Route child : children)
                childrenNode.add(child._serialize());
        }
        rootNode.set("childrens", childrenNode);

        return rootNode;
    }

    public String serialize() {
        return this._serialize().toPrettyString();
    }

}
