package com.cross.crosstest.action;

import com.cross.crosstest.api.JavaToVue;
import com.cross.crosstest.api.VueToJava;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidatorEx;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;


public class upAction extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {

        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
//        String selectedText = selectionModel.getSelectedText();

//        PsiElement selectedElement = e.getData(CommonDataKeys.PSI_ELEMENT);
//        System.out.println("element: " + selectedElement);
//        System.out.println("element text: " + selectedElement.getText());
        Project project = e.getProject();
        String fullUrl = null;
        PsiFile psiFile = e.getRequiredData(CommonDataKeys.PSI_FILE);
        PsiElement selectedElement = e.getData(CommonDataKeys.PSI_ELEMENT);
        SelectionModel selectionModel = editor.getSelectionModel();
        String fileName = psiFile.getName();
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        String type = "";

        if ("java".equals(extension)) {
            int start = selectionModel.getSelectionStart();
            PsiElement selectJavaElement = psiFile.findElementAt(start);
//            System.out.println("Start element: " + selectJavaElement.getText());
            JavaToVue javaToVue = new JavaToVue();
            PsiElement parent = selectJavaElement.getParent(); // 查看选中代码的父元素
//            System.out.println(selectJavaElement instanceof PsiJavaToken);

            // map
            if(selectJavaElement instanceof PsiJavaToken){
                // Traverse the parent element, because its first parent element may not be the type we want
                while (parent != null && !(parent instanceof PsiMethodCallExpression) && !(parent instanceof PsiAnnotation)) {
                    parent = parent.getParent();
                }

                if (parent instanceof PsiMethodCallExpression) {
                    PsiMethodCallExpression methodCall = (PsiMethodCallExpression) parent;
                    PsiReferenceExpression methodExpression = methodCall.getMethodExpression();// get method call expression
                    String methodName = methodExpression.getReferenceName(); // get method name
                    // Check if the method name is "put"
                    if ("put".equals(methodName)) {
                        PsiExpressionList argumentList = methodCall.getArgumentList(); // Get the parameter list of the method
                        PsiExpression[] arguments = argumentList.getExpressions();  // get parameter list

                        // If the number of parameters is 2 and the first parameter is the parent element of the PsiElement at the starting position
                        if (arguments.length == 2 && arguments[0] == selectJavaElement.getParent()) {
//                            System.out.println("The selected element is a key in a Map.put(key, value) call.");
                            // if return map
                            PsiMethod psiMethod = PsiTreeUtil.getParentOfType(parent, PsiMethod.class);
//                            System.out.println("method: "+psiMethod);
                            if (psiMethod != null) {
                                PsiClass psiClass = psiMethod.getContainingClass();

                                if (psiClass != null) {
                                    // Find @RequestMapping annotation in class and method
                                    PsiAnnotation classAnnotation = AnnotationUtil.findAnnotation(psiClass, "org.springframework.web.bind.annotation.RequestMapping");
                                    PsiAnnotation methodAnnotation = AnnotationUtil.findAnnotation(psiMethod, "org.springframework.web.bind.annotation.GetMapping");

                                    if (classAnnotation != null && methodAnnotation != null) {
                                        String classUrl = AnnotationUtil.getDeclaredStringAttributeValue(classAnnotation, "value");
                                        String methodUrl = AnnotationUtil.getDeclaredStringAttributeValue(methodAnnotation, "value");

                                        if (classUrl != null && methodUrl != null) {
                                            fullUrl = classUrl + methodUrl;
                                            System.out.println("fullUrl: " + fullUrl);
                                        }
                                    }
                                }


                                PsiStatement returnStatement = null;
                                PsiCodeBlock methodBody = psiMethod.getBody();
                                if (methodBody != null) {
                                    for (PsiStatement statement : methodBody.getStatements()) {
                                        if (statement instanceof PsiReturnStatement) {
                                            returnStatement = statement;
//                                        System.out.println("returnStatement: " + returnStatement);
                                               break;
                                        }
                                    }
                                }
                                if (returnStatement != null) {
                                    System.out.println("returnStatement: " + returnStatement);
                                    // Get the returned expression from the return statement, that is, the part after the return keyword. For example, in return map; the expression is map
                                    PsiExpression returnedExpression = ((PsiReturnStatement) returnStatement).getReturnValue();
                                    if (returnedExpression instanceof PsiReferenceExpression) {

                                        PsiElement resolvedReturnedExpression = ((PsiReferenceExpression) returnedExpression).resolve();
                                        PsiElement resolvedMethodCall = ((PsiReferenceExpression) methodExpression.getQualifierExpression()).resolve();
                                        // If the returned variable is the same as the methodCall variable, it means that the map is returned
                                        if (resolvedReturnedExpression.equals(resolvedMethodCall)) {
                                            System.out.println("The Map with the selected key is returned to the frontend.");
                                            //rename
//                                            String newName = Messages.showInputDialog(project, "Enter new name", "Rename", null);
                                            String newName = getNewName(project);

                                            // If user has entered a name, rename the selected element
                                            if (newName != null && !newName.isEmpty()) {
                                                type = "map";
                                                String finalFullUrl = fullUrl;
                                                String finalType = type;
                                                WriteCommandAction.runWriteCommandAction(project, () -> {
                                                    PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
                                                    PsiElement newKey = factory.createExpressionFromText("\"" + newName + "\"", null);
                                                    CodeStyleManager.getInstance(project).reformat(newKey);
                                                    selectJavaElement.replace(newKey);
                                                    javaToVue.toVue(selectJavaElement, newKey, project, finalFullUrl, finalType);
                                                });
                                            } else {
                                                Messages.showErrorDialog(project, "The new Name can not be empty", "Error");
                                            }

                                        }else {
                                            System.out.println("The Map is not returned to the frontend or " +
                                                    "Use the ResponseEntity object to encapsulate the return value etc. ");
                                        }
                                    }
                                } else {
                                    System.out.println("ReturnStatement null");
                                }
                            }
                        }
                    }

                    //url
                } else if (parent instanceof PsiAnnotation) {

                    PsiAnnotation annotation = (PsiAnnotation) parent;
                    if (annotation.getQualifiedName().equals("org.springframework.web.bind.annotation.GetMapping")
                    || annotation.getQualifiedName().equals("org.springframework.web.bind.annotation.PostMapping")) {

                        //get requestMapping
                        PsiClass containingClass = PsiTreeUtil.getParentOfType(selectJavaElement, PsiClass.class);

                        PsiAnnotation classAnnotation = AnnotationUtil.findAnnotation(containingClass, "org.springframework.web.bind.annotation.RequestMapping");
                        if (classAnnotation != null && annotation != null) {
                            String classUrl = AnnotationUtil.getDeclaredStringAttributeValue(classAnnotation, "value");
                            String methodUrl = AnnotationUtil.getDeclaredStringAttributeValue(annotation, "value");
                            if (classUrl != null && methodUrl != null) {
                                fullUrl = classUrl + methodUrl;
                                String newName = getNewName(project);

                                // If user has entered a name, rename the selected element
                                if (newName != null && !newName.isEmpty()) {
                                    if (!newName.startsWith("/")) {
                                        newName = "/" + newName;
                                    }

                                    String finalFullUrl = fullUrl;
                                    type = "url";
                                    String finalType = type;
                                    String finalNewName = newName;
                                    WriteCommandAction.runWriteCommandAction(project, () -> {
                                        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
                                        PsiElement newKey = factory.createExpressionFromText("\"" + finalNewName + "\"", null);
                                        CodeStyleManager.getInstance(project).reformat(newKey);
                                        selectJavaElement.replace(newKey);
                                        javaToVue.toVue(selectJavaElement, newKey, project, finalFullUrl, finalType);
                                    });
                                } else {
                                    Messages.showErrorDialog(project, "The new Name can not be empty", "Error");
                                }
                            }
                        }

                    }
                    if (annotation.getQualifiedName().equals("org.springframework.web.bind.annotation.RequestMapping")){


                    }
                } else {

                }
            }

            if (parent instanceof PsiMethodCallExpression) {

            }else {

            }

        } else if ("vue".equals(extension)) {
            VueToJava vueToJava = new VueToJava();

            int start = selectionModel.getSelectionStart();
            PsiElement selectVueElement = psiFile.findElementAt(start);
            PsiElement vueElement = psiFile.findElementAt(start);

//            System.out.println("select element " + selectedElement);
//            System.out.println("select text: " + selectVueElement.getText());
//            System.out.println("select text type: " + selectVueElement.getClass().getSimpleName());
//            System.out.println("parent: " + selectVueElement.getParent().getText());
//
//            System.out.println("editor: " + editor.getSelectionModel().getSelectedText());


            if (selectedElement != null){

                if (selectedElement instanceof JSProperty){
//                    System.out.println("find JSProperty");

                } else if (selectedElement instanceof JSLiteralExpression){
                    String newName = getNewName(project);
                    if (newName != null && !newName.isEmpty()) {
//                        System.out.println("new name: " + newName);
                    }
                }else if (selectedElement.getClass().getSimpleName().equals("UrlTargetInfoFakeElement")){
                    String newName = getNewName(project);
                    String oldName = editor.getSelectionModel().getSelectedText();
                    String oldUrl =  selectVueElement.getText();
                    String newUrl = oldUrl.replace(oldName, newName);

                    if (newName != null && !newName.isEmpty()) {
                        boolean success;
                        try {
                            success = vueToJava.toJava(oldName, newName, project, oldUrl, "url");
                        } catch (MalformedURLException ex) {
                            throw new RuntimeException(ex);
                        }
                        if(success){
                            PsiElement finalSelectVueElement = selectVueElement;
                            WriteCommandAction.runWriteCommandAction(project, () -> {
                                PsiElement newElement = finalSelectVueElement.replace(JavaPsiFacade.getElementFactory(project).createExpressionFromText(newUrl, null));
                                CodeStyleManager.getInstance(project).reformat(newElement);
                            });
                        }
                    }
                }
            }else if (selectedElement == null){
                while (!(selectVueElement instanceof JSReferenceExpression) && selectVueElement != null) {
                    selectVueElement = selectVueElement.getParent();
                }
                JSReferenceExpression refExpr = (JSReferenceExpression) selectVueElement;
                System.out.println("refE:" + refExpr.getText());
                System.out.println("refExpr.getQualifier(): " + refExpr.getQualifier().getText());

                if (refExpr != null) {
                    PsiElement axiosGetCall = findAxiosGetCall(refExpr);

                    String url = findAxios(axiosGetCall);
                    String urlPath = null;
                    try {
                        urlPath = getUrlPath(url);
                    } catch (MalformedURLException ex) {
                        throw new RuntimeException(ex);
                    }

                    if (axiosGetCall != null) {
//                         find then function
                        JSArgumentList thenCall = findThenCallArguments(axiosGetCall);
                        if (thenCall != null) {
                            JSFunction thenCallback = getCallbackFromThenCall(thenCall);
                            if (thenCallback != null) {
                                JSParameterList parameterList = thenCallback.getParameterList();
                                if (parameterList != null) {

                                    JSParameterListElement[] parameters = parameterList.getParameters();

                                    System.out.println("left most: " + getLeftMostQualifierName(refExpr));
                                    System.out.println("right most: " + parameters[0].getName());

                                    if (parameters.length > 0 && parameters[0].getName().equals(getLeftMostQualifierName(refExpr))) {
                                        System.out.println("Selected element comes from the backend response");

                                        String newName = getNewName(project);
                                        String oldName = vueElement.getText();
//                                        String oldUrl =  selectVueElement.getText();


                                        if (newName != null && !newName.isEmpty()) {
                                            boolean success;
                                            try {
                                                success = vueToJava.toJava(oldName, newName, project, url, "map");
                                            } catch (MalformedURLException ex) {
                                                throw new RuntimeException(ex);
                                            }
                                            if(success) {
                                                WriteCommandAction.runWriteCommandAction(project, () -> {
                                                    PsiElement newElement = vueElement.replace(JavaPsiFacade.getElementFactory(project).createExpressionFromText(newName, null));
                                                    CodeStyleManager.getInstance(project).reformat(newElement);
                                                });
                                            }
//

//                                            PsiElement[] children = selectVueElement.getChildren();
//                                            for (PsiElement child : children) {
//                                                System.out.println(child.getText());
//                                                System.out.println("oldName is " + oldName);
//                                                System.out.println("get text: " + child.getText());
//                                                if (child.getText().equals(oldName)) {
//                                                    System.out.println("rename key in frontend");
//
//                                                }
//                                            }
//                                            break;
                                        }

                                    } else {
                                        System.out.println("Selected element does not come from the backend response");
                                    }
                                }
                            }
                        }
                    }

                }
            }


        } else if ("js".equals(extension)) {
            // This is a JavaScript or Vue.js file.


        } else if ("html".equals(extension)) {
            // This is a HTML file.


        } else if("py".equals(extension)){

        }

    }

    @Nullable
    private static String getNewName(Project project) {
        String input = Messages.showInputDialog(project, "Input new name", "Rename", Messages.getQuestionIcon(), "", new InputValidatorEx() {
            private String errorText = null;

            @Override
            public boolean checkInput(String inputString) {
                errorText = null;
                // Check if input contains numbers
                if (inputString.matches("^\\d.*")) {
//                    errorText = "'"+inputString+"' cannot contain numbers";
                    errorText = "not a valid identifier";
                    return false;
                }
                // Check input length
                if(inputString.length() > 10){
                    errorText = "cannot exceed 10 characters";
                    return false;
                }
                // Check if input contains special characters
                if(inputString.matches(".*[^a-zA-Z0-9_].*")){
                    errorText = "cannot contain special characters";
                    return false;
                }
                // Check if input is a reserved word
                String[] reservedWords = {"new", "class", "public", "private", "protected", "final", "void",
                        "extends", "implements", "static", "try", "catch", "finally", "throw", "throws", "return",
                        "this", "super", "null", "true", "false", "instanceof", "enum", "int", "long", "double", "char",
                        "boolean", "byte", "short", "float", "synchronized", "volatile", "transient", "interface", "abstract",
                        "strictfp", "package", "import", "assert", "default", "continue", "break", "do", "while", "switch", "case",
                        "for", "if", "else"};
                for(String word : reservedWords){
                    if(word.equals(inputString)){
                        errorText = "not a valid identifier";
                        return false;
                    }
                }
                return true;
            }

            @Override
            public boolean canClose(String inputString) {
                return this.checkInput(inputString);
            }

            @Override
            public String getErrorText(String inputString) {
                return errorText;
            }
        });
        return input;
    }

    // vue to java find axios call
    private PsiElement findAxiosGetCall(PsiElement element) {
        while (!(element instanceof JSCallExpression) || !isAxiosGetCall((JSCallExpression)element)) {
            element = element.getParent();
            if (element == null) {
                return null;
            }
        }
        return element;
    }

    //
    private boolean isAxiosGetCall(JSCallExpression call) {

        JSExpression methodExpression = call.getMethodExpression();
        System.out.println("methodExpression: " + methodExpression);
        if (methodExpression instanceof JSReferenceExpression) {
            JSReferenceExpression referenceExpression = (JSReferenceExpression) methodExpression;
            return true;
        }
        return false;
    }


    // vue to java find axios "then"
    private JSArgumentList findThenCallArguments(PsiElement element) {
        while (!(element instanceof JSCallExpression) || !isThenCall((JSCallExpression)element)) {
            element = element.getParent();
            if (element == null) {
                return null;
            }
        }
        JSCallExpression callExpression = (JSCallExpression) element;
        return callExpression.getArgumentList();
    }

    private boolean isThenCall(JSCallExpression call) {
        JSExpression methodExpression = call.getMethodExpression();
//        System.out.println("methodExpression: " + methodExpression.getText());

        return methodExpression instanceof JSReferenceExpression &&
                "then".equals(((JSReferenceExpression) methodExpression).getReferencedName());
    }

    // vue to java find axios call back function
    private JSFunction getCallbackFromThenCall(JSArgumentList argumentList) {
        if (argumentList != null) {
            JSExpression[] arguments = argumentList.getArguments();
            if (arguments.length > 0 && arguments[0] instanceof JSFunction) {
                return (JSFunction) arguments[0];
            }
        }
        return null;
    }


    //return Left Most QualifierName res.data.xxx return: res
    private String getLeftMostQualifierName(JSReferenceExpression refExpr) {
        JSExpression qualifier = refExpr.getQualifier();
        if (qualifier instanceof JSReferenceExpression) {
            return getLeftMostQualifierName((JSReferenceExpression)qualifier);
        } else {
            return refExpr.getReferencedName();
        }
    }

    private String findAxios(PsiElement element) {
        while (!(element instanceof JSCallExpression)) {
            element = element.getParent();
            if (element == null) {
                return null;
            }
        }
        JSCallExpression callExpression = (JSCallExpression) element;;
        String url = getURLFromAxiosGetCall(callExpression);
        return url;
    }

    private String getURLFromAxiosGetCall(JSCallExpression call) {
        JSExpression methodExpression = call.getMethodExpression();
        if (methodExpression instanceof JSReferenceExpression) {
            JSExpression qualifier = ((JSReferenceExpression)methodExpression).getQualifier();
            if (qualifier instanceof JSCallExpression) {
                JSCallExpression axiosGetCall = (JSCallExpression)qualifier;
                JSArgumentList argumentList = axiosGetCall.getArgumentList();
                if (argumentList != null) {
                    JSExpression[] arguments = argumentList.getArguments();
                    if (arguments.length > 0 && arguments[0] instanceof JSLiteralExpression) {
                        JSLiteralExpression urlLiteral = (JSLiteralExpression) arguments[0];
                        if (urlLiteral.isQuotedLiteral()) {
                            return urlLiteral.getStringValue();
                        }
                    }
                }
            }
        }
        return null;
    }
    //test

    private String getUrlPath(String url) throws MalformedURLException {
        URL backendUrl = new URL(url);

        String protocolUrl = backendUrl.getProtocol();  //http
        String hostUrl = backendUrl.getHost(); // localhost
        int portUrl = backendUrl.getPort(); // 8081
        String pathUrl = backendUrl.getPath(); // url path

        return pathUrl;
    }
}
