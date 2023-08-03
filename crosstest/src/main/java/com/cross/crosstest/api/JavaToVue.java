package com.cross.crosstest.api;


import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.rename.PsiElementRenameHandler;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.ProjectUtil;

import java.util.concurrent.atomic.AtomicBoolean;


public class JavaToVue {
//    public void Java(PsiElement selectedElement, Project project, Editor editor) {
//        PsiElementRenameHandler.rename(selectedElement, project, null, editor);
//    }

    public void toVue(PsiElement selectedElement, PsiElement newElement, Project project, String url,String type) {
        AtomicBoolean result = new AtomicBoolean(false);
        // 获取旧键和新键
        String oldKey = selectedElement.getText();
        String newKey = newElement.getText();

        // 删除旧键和新键的双引号
        oldKey = oldKey.substring(1, oldKey.length()-1);
        newKey = newKey.substring(1, newKey.length()-1);

        String frontendProjectPath = null;
        VirtualFile parentDir = project.getBaseDir().getParent();
        String backendProjectName = project.getName();
        for (VirtualFile childDir : parentDir.getChildren()) {
            // Skip the backend project directory
            if (childDir.getName().equals(backendProjectName)) {
                continue;
            }

            if (childDir.isDirectory()) {
                frontendProjectPath = childDir.getPath();
//                System.out.println("frontendProjectPath: " + frontendProjectPath);
                break; // Assuming there are only two directories, we can break after finding the first non-backend directory.
            }
        }

        if (frontendProjectPath != null) {
            ProjectManager projectManager = ProjectManager.getInstance();
            for (Project openProject : projectManager.getOpenProjects()) {
//                System.out.println("frontendProjectPath: " + frontendProjectPath);
//                System.out.println("open frontend path: " + openProject.getBasePath());

                if (frontendProjectPath.equals(openProject.getBasePath())) {
                    // This is the frontend project.
                    Project frontendProject = openProject;
                    // 遍历父目录中的所有子目录
//                    System.out.println("font :" + frontendProject);
                    for (VirtualFile dir : frontendProject.getBaseDir().getChildren()) {
                        // 确保找到的是目录
//                        System.out.println(dir.getPath());
                        if (!dir.isDirectory()) {
                            continue;
                        }

                        // 遍历目录的所有文件
                        String finalOldKey = oldKey;
                        String finalNewKey = newKey;
//                        System.out.println("final: " + finalOldKey);

                        VfsUtilCore.visitChildrenRecursively(dir, new VirtualFileVisitor() {
                            @Override
                            public boolean visitFile(@NotNull VirtualFile file) {
                                if (file.getName().endsWith(".vue")) {
                                    if(type == "url"){
                                        String regex = "axios\\.(get|post)\\('.*?" + url + ".*";

//                                        System.out.println("find url in front end: " + regex);
                                        PsiFile psiFile = PsiManager.getInstance(frontendProject).findFile(file);
                                        if (psiFile != null) {
//                                            System.out.println("psifile is not null");
                                            psiFile.accept(new PsiRecursiveElementVisitor() {
                                                @Override
                                                public void visitElement(@NotNull PsiElement element) {
                                                    if (element.getText().matches(regex)) {
//                                                        System.out.println("find element: " + element.getText());
                                                        String oldUrl = element.getText();
                                                        String newUrl = oldUrl.replace(finalOldKey, finalNewKey);
                                                        System.out.println("newUrl: " + newUrl);

                                                        WriteCommandAction.runWriteCommandAction(frontendProject, () -> {
                                                            PsiElement newElement = element.replace(JavaPsiFacade.getElementFactory(frontendProject).createExpressionFromText(newUrl, null));
                                                            CodeStyleManager.getInstance(frontendProject).reformat(newElement);
                                                        });
                                                    }

                                                    // Continue the recursion
                                                    super.visitElement(element);
                                                }
                                            });
                                        }
                                    }
                                    if(type == "map"){
                                        String regex = "axios\\.(get|post)\\('.*?" + url + ".*";

                                        PsiFile psiFile = PsiManager.getInstance(frontendProject).findFile(file);
                                        if (psiFile != null) {
                                            psiFile.accept(new PsiRecursiveElementVisitor() {
                                                @Override
                                                public void visitElement(@NotNull PsiElement element) {
                                                    if (element.getText().matches(regex)) {
                                                        System.out.println("find element: " + element);
                                                        System.out.println("find element text : " + element.getText());
//                                                        PsiElement parent = element;
//                                                        while (!(parent instanceof JSCallExpression)) {
//                                                            parent = parent.getParent();
//                                                        }
                                                        PsiElement parent = element.getParent().getParent();

                                                        System.out.println("parent: " + parent.getClass().getSimpleName());

                                                        if (parent instanceof JSCallExpression) {
                                                            JSCallExpression callExpression = (JSCallExpression) parent;
                                                            JSArgumentList argumentList = callExpression.getArgumentList();
//                                                            System.out.println("argumentlsit: " + argumentList.getText());
                                                            if (argumentList != null) {
                                                                JSExpression[] expressions = argumentList.getArguments();
                                                                for (JSExpression expression : expressions) {
                                                                    System.out.println("expression: " + expression);
                                                                    if (expression instanceof JSFunctionExpression) {
                                                                        JSFunctionExpression functionExpression = (JSFunctionExpression) expression;
                                                                        JSParameterListElement[] parameters = functionExpression.getParameterList().getParameters();
                                                                        for (JSParameterListElement parameter : parameters) {
                                                                            if (parameter.getText().equals("resp")) {
                                                                                System.out.println("Parameter: " + parameter.getText());


                                                                                PsiElement parentElement = parameter.getParent();

                                                                                while (!(parentElement instanceof JSBlockStatement)) {
                                                                                    parentElement = parentElement.getParent();
                                                                                }

//                                                                                System.out.println("parent for recuresive:" + parentElement);

                                                                                parentElement.accept(new JSRecursiveElementVisitor() {
                                                                                    @Override
                                                                                    public void visitJSReferenceExpression(JSReferenceExpression node) {
                                                                                        super.visitJSReferenceExpression(node);

                                                                                        String refName = node.getReferencedName();
                                                                                        String oldName = finalOldKey; // 你想要修改的旧名称
                                                                                        String newName = finalNewKey; // 新的名称

                                                                                        JSExpression qualifier = node.getQualifier();

                                                                                        if (qualifier instanceof JSReferenceExpression) {
                                                                                            JSReferenceExpression qualifierRef = (JSReferenceExpression) qualifier;
                                                                                            JSExpression qualifierRefQualifier = qualifierRef.getQualifier();

                                                                                            if (qualifierRefQualifier instanceof JSReferenceExpression) {
                                                                                                JSReferenceExpression qualifierRefQualifierRef = (JSReferenceExpression) qualifierRefQualifier;
                                                                                                // 当代码中出现resp.data.oldName（如resp.data.test1）这样的结构时，会进行重命名操作
                                                                                                if (qualifierRefQualifierRef.getReferencedName().equals("resp")
                                                                                                        && qualifierRef.getReferencedName().equals("data")
                                                                                                        && refName.equals(oldName)) {

                                                                                                    System.out.println("already to rename");
                                                                                                    WriteCommandAction.runWriteCommandAction(project, () -> {
                                                                                                        PsiElement newElement = JSChangeUtil.createStatementFromText(project, newName).getPsi();
                                                                                                        node.getReferenceNameElement().replace(newElement);
                                                                                                    });
                                                                                                }
                                                                                            }


                                                                                        }


                                                                                        if (isQualifierResp(node) && refName.equals(oldName)) {
                                                                                            System.out.println("newName is :" + newName);
                                                                                            // 新名称

                                                                                            // 执行重命名操作
//                                                                                            WriteCommandAction.runWriteCommandAction(project, () -> {
//                                                                                                JSReferenceElement referenceElement = JSChangeUtil.createReferenceFromText(project, newName);
//                                                                                                node.getReferenceNameElement().replace(referenceElement);
//                                                                                            });
                                                                                        }
                                                                                    }
                                                                                });

// 新增方法，检查qualifier是否是"resp

                                                                            }
                                                                            if (parameter.getText().equals("test1")){
                                                                                System.out.println("Parameter test : " + parameter.getText());
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }


//                                                        WriteCommandAction.runWriteCommandAction(frontendProject, () -> {
//                                                            PsiElement newElement = element.replace(JavaPsiFacade.getElementFactory(frontendProject).createExpressionFromText(newUrl, null));
//                                                            CodeStyleManager.getInstance(frontendProject).reformat(newElement);
//                                                        });
                                                    }

                                                    // Continue the recursion
                                                    super.visitElement(element);
                                                }
                                            });
                                        }
//


                                        // 打开文件，获取document对象
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
//
//
//                                        // 执行写入操作
//                                        WriteCommandAction.runWriteCommandAction(frontendProject, () -> {
//                                            // 用新键替换旧键
//                                            String newContent = fileContent.replaceAll(finalOldKey, finalNewKey);
//                                            // 将新内容写回文件
//                                            document.setText(newContent);
////                                        result.set(true);
//                                        });
                                    }


                                }
                                return true;
                            }
                        });

                    }
                }
            }
        }
//        return result.get();
    }

    private boolean isQualifierResp(JSReferenceExpression node) {
        JSExpression qualifier = node.getQualifier();
        return qualifier != null && qualifier.getText().equals("resp");
    }

}
//        String oldKey = selectedElement.getText();
//        String newKey = newElement.getText();
//
//        String frontendProjectPath = null;
//        VirtualFile parentDir = project.getBaseDir().getParent();
//        System.out.println("parentDir: + " + parentDir);
//        String backendProjectName = project.getName();
//        for (VirtualFile childDir : parentDir.getChildren()) {
//            // Skip the backend project directory
//            if (childDir.getName().equals(backendProjectName)) {
//                continue;
//            }
//
//            if (childDir.isDirectory()) {
//                frontendProjectPath = childDir.getPath();
////                System.out.println("frontendProjectPath: " + frontendProjectPath);
//                break; // Assuming there are only two directories, we can break after finding the first non-backend directory.
//            }
//        }
//
//        if (frontendProjectPath != null) {
//            ProjectManager projectManager = ProjectManager.getInstance();
//            for (Project openProject : projectManager.getOpenProjects()) {
//                System.out.println("open:" + openProject.getBasePath());
//                System.out.println("front:" + frontendProjectPath);
//                if (frontendProjectPath.equals(openProject.getBasePath())) {
//                    // This is the frontend project.
//                    System.out.println("get front end project");
//                    Project frontendProject = openProject;
//                    // 获得前端文件夹下的所有文件
//                    Collection<VirtualFile> allFiles = FileTypeIndex.getFiles(PlainTextFileType.INSTANCE, GlobalSearchScope.allScope(frontendProject));
//                    Collection<VirtualFile> vueFiles = new ArrayList<>();
//                    //获得所有.vue文件
//                    for (VirtualFile file : allFiles) {
//                        if (file.getName().endsWith(".vue")) {
//                            vueFiles.add(file);
//                        }
//                    }
//                    for (VirtualFile vueFile : vueFiles) {
//                        PsiFile psiVueFile = PsiManager.getInstance(frontendProject).findFile(vueFile);
//                        if (psiVueFile != null) {
//                            //获得所有含有axios的文件
//                            Collection<JSCallExpression> axiosCalls = PsiTreeUtil.findChildrenOfType(psiVueFile, JSCallExpression.class);
//                            for (JSCallExpression axiosCall : axiosCalls) {
//                                System.out.println("axioscall:" + axiosCall.getText());
//                                System.out.println("axios.get('" + url + "')");
//                                if (axiosCall.getText().contains("axios.get('" + url + "')")) {
////                                    System.out.println("get axios/url");
//                                    Collection<JSProperty> properties = PsiTreeUtil.findChildrenOfType(axiosCall, JSProperty.class);
//                                    for (JSProperty property : properties) {
//                                        if (property.getName().equals("data")) {
//                                            System.out.println("get data");
//                                            JSObjectLiteralExpression objectLiteral = (JSObjectLiteralExpression) property.getValue();
//                                            if (objectLiteral != null) {
//                                                System.out.println("get value");
//                                                JSProperty oldProperty = objectLiteral.findProperty(oldKey);
//                                                if (oldProperty != null) {
//                                                    ApplicationManager.getApplication().runWriteAction(() -> {
//                                                        oldProperty.setName(newKey);
//                                                    });
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    break;
//                }
//            }
//        } else{
//            if (frontendProjectPath == null) {
//                // If we didn't find the frontend project, show an error message and return
//                Messages.showErrorDialog(project, "Could not find frontend project.", "Error");
//
//            }
//        }
