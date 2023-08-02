 package com.cross.crosstest.action;

import com.cross.crosstest.api.JavaToVue;
import com.cross.crosstest.api.VueToJava;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression;
import com.intellij.microservices.url.UrlTargetInfo;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.InputValidatorEx;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;
import org.apache.tools.ant.taskdefs.Java;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;


 public class upAction extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {



        // TODO: insert action logic here
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
//        String selectedText = selectionModel.getSelectedText();
//        AddDialog addDialog = new AddDialog(selectedText);
//        addDialog.show();

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
            System.out.println("Start element: " + selectJavaElement.getText());
            JavaToVue javaToVue = new JavaToVue();
            PsiElement parent = selectJavaElement.getParent(); // 查看选中代码的父元素
            System.out.println(selectJavaElement instanceof PsiJavaToken);

            // map
            if(selectJavaElement instanceof PsiJavaToken){
                // 遍历父元素，因为它的首个父元素可能不是我们想要的类型
                while (parent != null && !(parent instanceof PsiMethodCallExpression) && !(parent instanceof PsiAnnotation)) {
                    parent = parent.getParent();
                }

                if (parent instanceof PsiMethodCallExpression) {
//                    System.out.println("parent is map");
                    PsiMethodCallExpression methodCall = (PsiMethodCallExpression) parent;
                    PsiReferenceExpression methodExpression = methodCall.getMethodExpression();// 获取方法调用表达式
                    String methodName = methodExpression.getReferenceName(); // 获取方法名
                    // 检查方法名是否为"put"
                    if ("put".equals(methodName)) {
                        PsiExpressionList argumentList = methodCall.getArgumentList(); // 获取方法的参数列表
                        PsiExpression[] arguments = argumentList.getExpressions();  // 获取参数数组

                        // 如果参数数量是2并且第一个参数就是开始位置的PsiElement的父元素
                        if (arguments.length == 2 && arguments[0] == selectJavaElement.getParent()) {
                            System.out.println("The selected element is a key in a Map.put(key, value) call.");

                            // 检查map是否被返回
                            PsiMethod psiMethod = PsiTreeUtil.getParentOfType(parent, PsiMethod.class);
                            System.out.println("method: "+psiMethod);
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
                                    // 从 return 语句中获取返回的表达式，也就是 return 关键字后面的部分。例如，在 return map; 中，这个表达式就是 map
                                    PsiExpression returnedExpression = ((PsiReturnStatement) returnStatement).getReturnValue();
                                    if (returnedExpression instanceof PsiReferenceExpression) {

                                        PsiElement resolvedReturnedExpression = ((PsiReferenceExpression) returnedExpression).resolve();
                                        PsiElement resolvedMethodCall = ((PsiReferenceExpression) methodExpression.getQualifierExpression()).resolve();
                                        // 如果返回的变量和methodCall的变量是同一个，说明这个map被返回
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

                        //获取requestMapping
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
                    // psiElement 是一个字符串，但不是 URL 或 map 的一部分
                    // 这可能是一个普通的字符串，我们可以进一步检查
                    // 或者在这里进行相关处理
                }
            }


            if (parent instanceof PsiMethodCallExpression) {
                // 将父元素强制转化为PsiMethodCallExpression类型

            }else {

            }

        } else if ("vue".equals(extension)) {
            VueToJava vueToJava = new VueToJava();

            int start = selectionModel.getSelectionStart();
            PsiElement selectVueElement = psiFile.findElementAt(start);
            System.out.println("select: " + selectedElement);
            System.out.println("select text: " + selectVueElement.getText());
            System.out.println("select text type: " + selectVueElement.getClass().getSimpleName());

            System.out.println("editor: " + editor.getSelectionModel().getSelectedText());
            System.out.println("type:" + selectedElement.getClass().getSimpleName());

            if (selectedElement instanceof JSProperty){
                System.out.println("find JSProperty");

            } else if (selectedElement instanceof JSLiteralExpression){
                String newName = getNewName(project);
                if (newName != null && !newName.isEmpty()) {
                    System.out.println("new name: " + newName);
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
                    System.out.println("success bo:" + success);
                    if(success){
                        WriteCommandAction.runWriteCommandAction(project, () -> {
                            PsiElement newElement = selectVueElement.replace(JavaPsiFacade.getElementFactory(project).createExpressionFromText(newUrl, null));
                            CodeStyleManager.getInstance(project).reformat(newElement);
                        });
                    }



//                    WriteCommandAction.runWriteCommandAction(project, () -> {
//                        PsiElement newElement = selectVueElement.replace(JavaPsiFacade.getElementFactory(project).createExpressionFromText(newUrl, null));
//                        CodeStyleManager.getInstance(project).reformat(newElement);
//                    });
//                    try {
//                        vueToJava.toJava(oldName, newName, project, oldUrl, "url");
//                    } catch (IOException ex) {
//                        throw new RuntimeException(ex);
//                    }
                }

            }



//            Query<PsiReference> search = ReferencesSearch.search(selectedElement);
//                for (PsiReference reference : search) {
//                    PsiElement element = reference.getElement();
//                    if (element instanceof JSStringTemplateExpression) {
//                        System.out.println("A string template expression has been found." + element);
//                        System.out.println("The selected URL is: " + element.getText());
//                    } else if (element instanceof JSObjectLiteralExpression) {
//                        System.out.println("A JS object literal expression has been found.");
//                        System.out.println("The selected object is: " + element.getText());
//                    } else {
//                        // Handle other types of PsiElement...
//                        System.out.println("A " + element.getClass().getSimpleName() + " has been found.");
//                        System.out.println("The selected element is: " + element.getText());
//                    }
//                }

//            JSObjectLiteralExpression objectLiteral = PsiTreeUtil.getParentOfType(selectElement, JSObjectLiteralExpression.class);

//            if (objectLiteral != null) {
//                // 3. Find the enclosing argument list
//                JSArgumentList argumentList = PsiTreeUtil.getParentOfType(objectLiteral, JSArgumentList.class);
//                if (argumentList != null) {
//                    PsiElement[] arguments = argumentList.getArguments();
//                    for (PsiElement argument : arguments) {
//                        System.out.println(argument.getText());
//                    }
//                    // 4. Access the URL argument
//                    PsiElement urlArgument = argumentList.getArguments()[0];
//                    System.out.println("The URL is: " + urlArgument.getText());
//
//                    // 5. Access the enclosing function
//                    JSFunction function = PsiTreeUtil.getParentOfType(argumentList, JSFunction.class);
//                    if (function != null) {
//                        System.out.println("The enclosing function is: " + function.getName());
//                    }
//                }
//            }


        } else if ("js".equals(extension)) {
            // This is a JavaScript or Vue.js file.


        } else if ("html".equals(extension)) {
            // This is a HTML file.


        } else if("py".equals(extension)){

        }

//        if (!selectionModel.hasSelection()) return;


//        if (selectedElement != null) {
//            String languageID = selectedElement.getLanguage().getID();
//            if ("JAVA".equals(languageID)) {
//                System.out.println("This is a Java code.");
//                // process Java code
//
//
//            } else if(selectedElement instanceof PsiLiteralExpression){
//                System.out.println("map.put");
//            }
//
//            else if (languageID.contains("ECMAScript") || languageID.contains("JavaScript")) {
//                System.out.println("This is a JavaScript code.");
//                // process JavaScript code
//                Query<PsiReference> search = ReferencesSearch.search(selectedElement);
//                for (PsiReference reference : search) {
//                    PsiElement element = reference.getElement();
//                    if (element instanceof JSStringTemplateExpression) {
//                        System.out.println("A string template expression has been found." + element);
//                        System.out.println("The selected URL is: " + element.getText());
//                    } else if (element instanceof JSObjectLiteralExpression) {
//                        System.out.println("A JS object literal expression has been found.");
//                        System.out.println("The selected object is: " + element.getText());
//                    } else {
//                        // Handle other types of PsiElement...
//                        System.out.println("A " + element.getClass().getSimpleName() + " has been found.");
//                        System.out.println("The selected element is: " + element.getText());
//
//                        JavaToVue refactorRename = new JavaToVue();
//                        refactorRename.ScriptRename(element, e.getProject() ,editor);
//                    }
//                }
//
//            } else if ("Python".equals(languageID)) {
//                System.out.println("This is a Python code.");
//                // process Python code
//
//            }
//        }


//            String className = selectedElement.getClass().getName();
//            String simpleClassName = selectedElement.getClass().getSimpleName();
//            System.out.println("The class name of the selected element is: " + className);
//            System.out.println("The simple class name of the selected element is: " + simpleClassName);


//        IProperty IProperty = PropertiesImplUtil.getProperty(selectedElement);
//        System.out.println("IProperty:" + IProperty);





//        PsiElement elementAtStart = null;
//        if (editor != null) {
//            // 获取选中的文本区域
//            SelectionModel selectionModel = editor.getSelectionModel();
//            int start = selectionModel.getSelectionStart();
//            int end = selectionModel.getSelectionEnd();
//
//            // 获取当前的 PsiFile 对象
//            PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
//
//            if (psiFile != null) {
//                // 使用 PsiDocumentManager 将选中的文本区域转换为 PsiElement
//                elementAtStart = PsiDocumentManager.getInstance(psiFile.getProject()).getPsiFile(editor.getDocument()).findElementAt(start);
//                PsiElement elementAtEnd = PsiDocumentManager.getInstance(psiFile.getProject()).getPsiFile(editor.getDocument()).findElementAt(end);
//
//                // 打印选中的 PsiElement
//                PsiNamedElement namedElement = PsiTreeUtil.getParentOfType(elementAtStart, PsiNamedElement.class);
//                if (namedElement != null) {
//                    System.out.println("Found named element: " + namedElement.getName());
//                } else {
//                    System.out.println("No named element found");
//                }
//
//                System.out.println("parrent : " + namedElement.getParent());
//            }
//        }



//        SwaggerAction swaggerAction = new SwaggerAction();
//        try {
//            swaggerAction.SwaggerAction();
//        } catch (IOException ex) {
//            throw new RuntimeException(ex);
//        }



        //先检测是否运行javatovue，再更改原文件中的代码。
//        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
//        PsiElement newKey = factory.createExpressionFromText("\"" + newName + "\"", null);
//        CodeStyleManager.getInstance(project).reformat(newKey);
//
//        boolean result = javaToVue.toVue(selectElement, newKey, project, finalFullUrl);
//        if (result) {
//            WriteCommandAction.runWriteCommandAction(project, () -> {
//                startElement.replace(newKey);
//            });
//        } else {
//            ApplicationManager.getApplication().invokeLater(() -> {
//                Messages.showMessageDialog(project, "javaToVue.toVue failed to run successfully.",
//                        "Error", Messages.getErrorIcon());
//            });
//        }

    }

    @Nullable
    private static String getNewName(Project project) {
        String input = Messages.showInputDialog(project, "Input new name", "Rename", Messages.getQuestionIcon(), "", new InputValidatorEx() {
            private String errorText = null;

            @Override
            public boolean checkInput(String inputString) {
                // Check if input contains numbers
                if(inputString.matches(".*\\d.*")){
//                    errorText = "'"+inputString+"' cannot contain numbers";
                    errorText = "cannot contain numbers";
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


    //test
}
