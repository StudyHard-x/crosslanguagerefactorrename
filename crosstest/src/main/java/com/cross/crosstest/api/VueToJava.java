package com.cross.crosstest.api;


import java.io.BufferedReader;
import java.io.IOException;;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.util.Query;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class VueToJava {


//    public void toJava(PsiElement selectedElement, PsiElement newElement, Project frontProject, String url,String type) {
    public boolean toJava(String oldKey, String newKey, Project frontProject, String url, String type) throws MalformedURLException {

    AtomicBoolean result = new AtomicBoolean(false);
//        String oldKey = selectedElement.getText();
//        String newKey = newElement.getText();

//        oldKey = oldKey.substring(1, oldKey.length()-1);
//        newKey = newKey.substring(1, newKey.length()-1);
//        url = url.replaceAll("^'|'$", "");
        if(type == "url"){
            url = url.substring(1, url.length() - 1);
        }

//        System.out.println("old: " + oldKey + " ;new: " + newKey + " ;url: " + url);
        String lastBackendUrl = url.substring(url.lastIndexOf('/') + 1);



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
//                System.out.println("open path: " + openProject.getBasePath());

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

                                    if (getMapping != null) {
                                        PsiAnnotationMemberValue getMappingValue = getMapping.findAttributeValue("value");
//                                        System.out.println("get MAP: " + getMappingValue.getText() + " " + getMappingValue.getClass().getSimpleName());
                                        if (getMappingValue instanceof PsiLiteralExpression) {
                                            mappingValues.add(getMappingValue);
                                        }
                                    }
                                }
                                if(type == "map"){
                                    boolean oldUrlExists = false;
                                    String oldUrl = null;
                                    oldUrl = "\"" + "/" + lastBackendUrl + "\"";

                                    PsiAnnotationMemberValue oldGetMappingValue = null;
                                    for (PsiAnnotationMemberValue getMappingValue : mappingValues) {

                                        if (oldUrl.equals(getMappingValue.getText())) {
                                            oldGetMappingValue = getMappingValue;
                                            oldUrlExists = true;
                                        }
                                    }
                                    if (oldUrlExists){
                                        PsiElement parent = oldGetMappingValue.getParent();
                                        while (!(parent instanceof PsiMethod)) {
                                            parent = parent.getParent();
                                        }
                                        PsiMethod method = (PsiMethod) parent;
                                        System.out.println("find method in backend: " + method.getName());
                                        if (method != null) {
                                            method.accept(new JavaRecursiveElementVisitor() {
                                                @Override
                                                public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                                                    super.visitMethodCallExpression(expression);
                                                    PsiReferenceExpression methodExpression = expression.getMethodExpression();
                                                    if ("put".equals(methodExpression.getReferenceName())) {
                                                        PsiExpression[] arguments = expression.getArgumentList().getExpressions();

//                                                        System.out.println("oldKey is: " + oldKey);
                                                        String quotedOldKey = "\"" + oldKey + "\"";
//                                                        System.out.println("arguments is: " + arguments[0].getText());

                                                        if (arguments.length > 0 && quotedOldKey.equals(arguments[0].getText())) {
                                                            // 找到了map.put("test01",1);
                                                            System.out.println("find old key");
                                                            WriteCommandAction.runWriteCommandAction(backendProject, () -> {
                                                                PsiElementFactory factory = JavaPsiFacade.getInstance(backendProject).getElementFactory();
                                                                PsiExpression oldExpression = arguments[0]; // 获取旧的表达式
                                                                PsiExpression newLiteralExpression = factory.createExpressionFromText("\"" + newKey + "\"", null);
                                                                oldExpression.replace(newLiteralExpression); // 替换旧的表达式
                                                            });
                                                        }
                                                    }
                                                }
                                            });
                                            return true;
                                        }
                                    }else {

                                    }



                                }

                                if(type == "url"){
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
                    }
                }
            }
        }

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


