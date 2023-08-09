package com.cross.crosstest.api;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.Message;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class VueToJava {


//    public void toJava(PsiElement selectedElement, PsiElement newElement, Project frontProject, String url,String type) {
    public boolean toJava(String oldKey, String newKey, Project frontProject, String url, String type) throws MalformedURLException {

    AtomicBoolean result = new AtomicBoolean(false);
        // 获取旧键和新键
//        String oldKey = selectedElement.getText();
//        String newKey = newElement.getText();

        // 删除旧键和新键的双引号
//        oldKey = oldKey.substring(1, oldKey.length()-1);
//        newKey = newKey.substring(1, newKey.length()-1);
//        url = url.replaceAll("^'|'$", "");
        url = url.substring(1, url.length() - 1);
        System.out.println("old: " + oldKey + " ;new: " + newKey + " ;url: " + url);

        URL backendUrl = new URL(url);

        // 获取URL的各部分
        String protocolUrl = backendUrl.getProtocol();  // 输出: http
        String hostUrl = backendUrl.getHost(); // 输出: localhost
        int portUrl = backendUrl.getPort(); // 输出: 8081
        String pathUrl = backendUrl.getPath(); // 输出: url path


        String backendProjectPath = null;
        VirtualFile parentDir = frontProject.getBaseDir().getParent();
        String frontendProjectName = frontProject.getName();
        for (VirtualFile childDir : parentDir.getChildren()) {
            // Skip the backend project directory
            if (childDir.getName().equals(frontendProjectName)) {
                continue;
            }

            if (childDir.isDirectory()) {
                backendProjectPath = childDir.getPath();
                break; // Assuming there are only two directories, we can break after finding the first non-backend directory.
            }
        }
        if (backendProjectPath != null){
            ProjectManager projectManager = ProjectManager.getInstance();
            for (Project openProject : projectManager.getOpenProjects()) {
                System.out.println("open path: " + openProject.getBasePath());

                if (backendProjectPath.equals(openProject.getBasePath())) {
                    // This is the frontend project.
                    Project backendProject = openProject;

                    GlobalSearchScope scope = GlobalSearchScope.allScope(backendProject);
                    PsiClass annotationClass = JavaPsiFacade.getInstance(backendProject).findClass("org.springframework.web.bind.annotation.RequestMapping", scope);
                    if (annotationClass != null) {
                        Query<PsiMember> query = AnnotatedElementsSearch.searchPsiMembers(annotationClass, scope);
                        for (PsiMember member : query) {
                            PsiAnnotation[] classAnnotations = member.getAnnotations();

                            if (member instanceof PsiClass) {
                                PsiClass psiClass = (PsiClass) member;
                                String className = psiClass.getQualifiedName();
//                                System.out.println("ClassName: " + className);

                                List<PsiAnnotationMemberValue> mappingValues = new ArrayList<>();
                                for (PsiMethod psiMethod : psiClass.getMethods()) {
                                    PsiAnnotation getMapping = AnnotationUtil.findAnnotation(psiMethod, "org.springframework.web.bind.annotation.GetMapping");

                                    // 检查是否找到了 @GetMapping 注解
                                    if (getMapping != null) {
                                        // 找到了 @GetMapping 注解，现在我们可以得到它的值，即 URL 路径
                                        PsiAnnotationMemberValue getMappingValue = getMapping.findAttributeValue("value");
//                                        System.out.println("get MAP: " + getMappingValue.getText() + " " + getMappingValue.getClass().getSimpleName());
                                        if (getMappingValue instanceof PsiLiteralExpression) {
                                            mappingValues.add(getMappingValue);

//                                            System.out.println("methodUrl: " + methodUrl.equals(getMappingValue.getText()));
//                                            if (newUrl.equals(getMappingValue.getText())){
//                                                System.out.println("rename no success");
//                                                Messages.showErrorDialog(frontProject, "url exist in backend", "Error");
//                                                return false;
//                                            }
//
//                                            else if(oldUrl.equals(getMappingValue.getText())){
//                                                PsiElement parent = getMappingValue.getParent();
//                                                while (!(parent instanceof PsiMethod)) {
//                                                    parent = parent.getParent();
//                                                }
//                                                PsiMethod method = (PsiMethod) parent;
//                                                System.out.println("Method name: " + method.getName());
//
//                                                String finalMethodUrl = newUrl;
//                                                WriteCommandAction.runWriteCommandAction(backendProject, () -> {
//                                                    PsiElementFactory factory = JavaPsiFacade.getInstance(backendProject).getElementFactory();
//                                                    PsiExpression newLiteralExpression = factory.createExpressionFromText(finalMethodUrl, null);
//                                                    getMappingValue.replace(newLiteralExpression);
//
//                                                });
//                                                return true;
//                                            }

                                        }
                                    }
                                }

                                boolean newUrlExists = false;
                                boolean oldUrlExists = false;

                                String oldUrl = oldKey;
                                String newUrl = newKey;

                                oldUrl = "\"" + "/" + oldKey + "\"";
                                newUrl = "\"" + "/" + newKey + "\"";

                                PsiAnnotationMemberValue oldGetMappingValue = null;
                                for (PsiAnnotationMemberValue getMappingValue : mappingValues) {
//                                    System.out.println(getMappingValue.getText());
//                                    System.out.println(getMappingValue.getText().equals(newUrl));
                                    if (newUrl.equals(getMappingValue.getText())) {
                                        newUrlExists = true;
                                    }

                                    if (oldUrl.equals(getMappingValue.getText())) {
                                        oldGetMappingValue = getMappingValue;
                                        oldUrlExists = true;
                                    }

                                    if (newUrlExists && oldUrlExists) {
                                        break;
                                    }
                                }

                                if (newUrlExists) {
                                    System.out.println("rename no success");
                                    Messages.showErrorDialog(frontProject, "url exist in backend", "Error");
                                    return false;
                                }

                                if (oldUrlExists) {
                                    // 进行其他操作
                                    PsiElement parent = oldGetMappingValue.getParent();
                                    while (!(parent instanceof PsiMethod)) {
                                        parent = parent.getParent();
                                    }
                                    PsiMethod method = (PsiMethod) parent;
//                                    System.out.println("Method name: " + method.getName());

                                    String finalMethodUrl = newUrl;
                                    PsiAnnotationMemberValue finalOldGetMappingValue = oldGetMappingValue;
                                    WriteCommandAction.runWriteCommandAction(backendProject, () -> {
                                        PsiElementFactory factory = JavaPsiFacade.getInstance(backendProject).getElementFactory();
                                        PsiExpression newLiteralExpression = factory.createExpressionFromText(finalMethodUrl, null);
                                        finalOldGetMappingValue.replace(newLiteralExpression);

                                    });
                                    return true;

                                }
                            }

                        }
                    }

//                    String operationId = null;
//                    String methodName = null;
//                    String className = null;
//
//                    try {
//                        URL openApiUrl = getOpenApiUrlFromBackendUrl(url);
//                        String responseContent = getOpenApiResponse(openApiUrl);
//                        Map<String, String> details = getOperationId(responseContent, url);
//                        operationId = details.get("operationId");
//                        methodName = details.get("methodName");
//                        className = details.get("controllerName");
//                        System.out.println("operationId: " + operationId);
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//
//                    if (operationId != null){
//                        System.out.println("methodName: " + methodName);
//                        System.out.println("className: " + className);
//                        String convertClassName = convertToClassName(className);
//                        System.out.println("className after replace: " + convertClassName);
//                        System.out.println("backend: " + backendProject.getName());
//
//                        GlobalSearchScope searchScope = GlobalSearchScope.allScope(backendProject); // Define the search scope
//                        PsiShortNamesCache nameCache = PsiShortNamesCache.getInstance(backendProject); // Get the PsiShortNamesCache instance
//
//                        PsiClass[] psiClasses = nameCache.getClassesByName(className, searchScope);
//                    }

                }
            }
        }


//
//                if (frontendProjectPath.equals(openProject.getBasePath())) {
//                    // This is the frontend project.
//                    Project frontendProject = openProject;
//                    // 遍历父目录中的所有子目录
////                    System.out.println("font :" + frontendProject);
//                    for (VirtualFile dir : frontendProject.getBaseDir().getChildren()) {
//                        // 确保找到的是目录
//                        System.out.println(dir.getPath());
//                        if (!dir.isDirectory()) {
//                            continue;
//                        }
//
//                        // 遍历目录的所有文件
//                        String finalOldKey = oldKey;
//                        String finalNewKey = newKey;
//                        System.out.println("final: " + finalOldKey);
//
//                        VfsUtilCore.visitChildrenRecursively(dir, new VirtualFileVisitor() {
//                            @Override
//                            public boolean visitFile(@NotNull VirtualFile file) {
//                                if (file.getName().endsWith(".vue")) {
//                                    if(type == "url"){
//                                        String regex = "axios\\.(get|post)\\('.*?" + url + ".*";
//
////                                        System.out.println("find url in front end: " + regex);
//                                        PsiFile psiFile = PsiManager.getInstance(frontendProject).findFile(file);
//                                        if (psiFile != null) {
////                                            System.out.println("psifile is not null");
//                                            psiFile.accept(new PsiRecursiveElementVisitor() {
//                                                @Override
//                                                public void visitElement(@NotNull PsiElement element) {
//                                                    if (element.getText().matches(regex)) {
//                                                        System.out.println("find element: " + element.getText());
//                                                        String oldUrl = element.getText();
//                                                        String newUrl = oldUrl.replace(finalOldKey, finalNewKey);
//                                                        System.out.println("newUrl: " + newUrl);
//
//                                                        WriteCommandAction.runWriteCommandAction(frontendProject, () -> {
//                                                            PsiElement newElement = element.replace(JavaPsiFacade.getElementFactory(frontendProject).createExpressionFromText(newUrl, null));
//                                                            CodeStyleManager.getInstance(frontendProject).reformat(newElement);
//                                                        });
//                                                    }
//
//                                                    // Continue the recursion
//                                                    super.visitElement(element);
//                                                }
//                                            });
//                                        }
//                                    }
//                                    if(type == "map"){
////                                        System.out.println(file.getName());
//                                        // 打开文件，获取document对象
//                                        FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
//                                        Document document = fileDocumentManager.getDocument(file);
//                                        // 获取文件的内容
//                                        String fileContent = document.getText();
//
//                                        // 检查文件中是否包含URL
//                                        System.out.println("url: " + url);
//                                        if (!fileContent.contains(url)) {
//                                            return true;
//                                        }
//                                        // 执行写入操作
//                                        WriteCommandAction.runWriteCommandAction(frontendProject, () -> {
//                                            // 用新键替换旧键
//                                            String newContent = fileContent.replaceAll(finalOldKey, finalNewKey);
//                                            // 将新内容写回文件
//                                            document.setText(newContent);
////                                        result.set(true);
//                                        });
//                                    }
//
//
//                                }
//                                return true;
//                            }
//                        });
//
//                    }
//                }
//            }
//        }
//        return result.get();
        return true;
    }


    private URL getOpenApiUrlFromBackendUrl(String backendUrl) throws MalformedURLException {
        URL originalUrl = new URL(backendUrl);
        String openApiUrl = originalUrl.getProtocol() + "://" + originalUrl.getHost();

        if (originalUrl.getPort() != -1) {
            openApiUrl += ":" + originalUrl.getPort();
        }

        openApiUrl += "/v2/api-docs";

        return new URL(openApiUrl);
    }

    private String getOpenApiResponse(URL openApiUrl) throws IOException {
        URL url = openApiUrl;
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            return response.toString();
        } else {
            return null;
        }
    }

    private Map<String, String> getOperationId(String jsonStr,String url) throws MalformedURLException {

        Map<String, String> swaggerDetail = new HashMap<>();

        String operationId = null;
        String methodName = null;
        String controllerName = null;
        URL backendUrl = new URL(url);

        // 获取URL的各部分
        System.out.println("Protocol: " + backendUrl.getProtocol()); // 输出: http
        System.out.println("Host: " + backendUrl.getHost()); // 输出: localhost
        System.out.println("Port: " + backendUrl.getPort()); // 输出: 8081
        System.out.println("Path: " + backendUrl.getPath()); // 输出: /users/newTest

        // 如果你想去掉URL的前缀（协议、主机和端口）
        String newPath = backendUrl.getPath();
        System.out.println("New Path: " + newPath);

        JsonObject jsonObject = new JsonParser().parse(jsonStr).getAsJsonObject();

        // 先获取 "paths" 这个 JsonObject
        JsonObject paths = jsonObject.getAsJsonObject("paths");

        // 检查 url 是否存在
        if (paths.has(newPath)) {
            JsonObject endpoint = paths.getAsJsonObject(newPath);
            for (Map.Entry<String, JsonElement> entry : endpoint.entrySet()) {
                String httpMethod = entry.getKey();
                JsonObject methodObj = endpoint.getAsJsonObject(httpMethod);
                JsonArray tags = methodObj.getAsJsonArray("tags");
                controllerName = tags.get(0).getAsString();
                methodName = methodObj.get("summary").getAsString();
                operationId = methodObj.get("operationId").getAsString();
                // 找到第一个方法后就跳出循环
                break;
            }
            swaggerDetail.put("operationId", operationId);
            swaggerDetail.put("methodName", methodName);
            swaggerDetail.put("controllerName", controllerName);
            return swaggerDetail;

        } else {
            // "/users/newTest" 不存在，返回一个错误信息或者默认值
            return null;
        }
    }

    //将class name转为后端代码的格式 比如 users-controller" -- "UsersController
    public static String convertToClassName(String className) {
        StringBuilder newClassName = new StringBuilder();
        for (String word : className.split("-")) {
            newClassName.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return newClassName.toString();
    }

    private void getBackendMethod(Project project, String url){
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        PsiShortNamesCache cache = PsiShortNamesCache.getInstance(project);
        String[] classNames = cache.getAllClassNames();

        for (String className : classNames) {
            PsiClass[] psiClasses = cache.getClassesByName(className, scope);
            for (PsiClass psiClass : psiClasses) {
                PsiAnnotation classAnnotation = psiClass.getAnnotation("org.springframework.web.bind.annotation.RequestMapping");
                if (classAnnotation != null) {
                    PsiAnnotationParameterList parameterList = classAnnotation.getParameterList();
                    PsiNameValuePair[] attributes = parameterList.getAttributes();
                    for (PsiNameValuePair attribute : attributes) {
                        if (attribute.getName().equals("value")) {
                            String classUrl = attribute.getValue().getText();
                            if (url.startsWith(classUrl)) {
                                PsiMethod[] psiMethods = psiClass.getMethods();
                                for (PsiMethod psiMethod : psiMethods) {
                                    PsiAnnotation methodAnnotation = psiMethod.getAnnotation("org.springframework.web.bind.annotation.GetMapping");
                                    if (methodAnnotation != null) {
                                        parameterList = methodAnnotation.getParameterList();
                                        attributes = parameterList.getAttributes();
                                        for (PsiNameValuePair valuePair : attributes) {
                                            if (valuePair.getName().equals("value")) {
                                                String methodUrl = valuePair.getValue().getText();
                                                if (url.equals(classUrl + methodUrl)) {
                                                    System.out.println("Found class: " + psiClass.getName());
                                                    System.out.println("Found method: " + psiMethod.getName());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}




