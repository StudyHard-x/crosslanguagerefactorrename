//package com.mcs.mcsproject.swagger;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.Iterator;
//import java.util.Map;
//
//public class ApiMapping {
//
//    public void createMapping(String swaggerJsonFilePath) throws IOException {
////        parseJavaFile(javaFilePath);
//        parseSwaggerFile(swaggerJsonFilePath);
//    }
//
//    private void parseSwaggerFile(String swaggerJsonFilePath) throws IOException {
//        ObjectMapper objectMapper = new ObjectMapper();
//        // 指定JSON文件路径
//        File file = new File("src/main/resources/swagger.json");
//        // 读取JSON文件并解析为JsonNode对象
//        JsonNode rootNode = objectMapper.readTree(file);
//        // 读取paths节点，这个节点包含了所有API的路径和方法
//        JsonNode pathsNode = rootNode.path("paths");
//        Iterator<Map.Entry<String, JsonNode>> fields = pathsNode.fields();
//        while (fields.hasNext()) {
//            Map.Entry<String, JsonNode> entry = fields.next();
////            System.out.println("Key: " + entry.getKey() + "\nValue: " + entry.getValue());
//        }
//
//        Iterable<String> apiEndpoints = pathsNode::fieldNames;
//
//        for (String apiEndpoint : apiEndpoints) {
//            // 对应的API端点节点
//            JsonNode apiEndpointNode = pathsNode.path(apiEndpoint);
//            // 获取所有的HTTP方法节点（如get、post等）
//            Iterable<String> methods = apiEndpointNode::fieldNames;
////            System.out.println(apiEndpointNode);
//
//            for (String method : methods) {
//                JsonNode methodNode = apiEndpointNode.path(method);
//                JsonNode parametersNode = methodNode.path("parameters");
//
//                for (JsonNode parameterNode : parametersNode) {
//                    if (parameterNode.has("in") && parameterNode.get("in").asText().equals(location)) {
//                        if (location.equals("body")) {
//
//                        }
//                    }
//                        // 如果当前位置是'body'
//                }
//
//            }
//
//
////            methods.forEach(System.out::println);
//
//        }
//    }
//}
