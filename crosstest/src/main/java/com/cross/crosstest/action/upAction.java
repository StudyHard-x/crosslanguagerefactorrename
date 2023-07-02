package com.cross.crosstest.action;

import com.intellij.lang.javascript.psi.JSArgumentList;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;


public class upAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
//        SelectionModel selectionModel = editor.getSelectionModel();
//        String selectedText = selectionModel.getSelectedText();
//        AddDialog addDialog = new AddDialog(selectedText);
//        addDialog.show();

        PsiElement selectedElement = e.getData(CommonDataKeys.PSI_ELEMENT);
        System.out.println("element: " + selectedElement);
        System.out.println("element text: " + selectedElement.getText());

        Query<PsiReference> search = ReferencesSearch.search(selectedElement);
        for (PsiReference reference : search) {
            PsiElement element = reference.getElement();
            System.out.println("reference： " + element);
            // Now 'element' is a place where 'target' is used
        }

        // ============================================
        JSObjectLiteralExpression objectLiteral = PsiTreeUtil.getParentOfType(selectedElement, JSObjectLiteralExpression.class);

        if (objectLiteral != null) {
            // 3. Find the enclosing argument list
            JSArgumentList argumentList = PsiTreeUtil.getParentOfType(objectLiteral, JSArgumentList.class);

            if (argumentList != null) {
                // 4. Access the URL argument
                PsiElement urlArgument = argumentList.getArguments()[0];
                System.out.println("The URL is: " + urlArgument.getText());

                // 5. Access the enclosing function
                JSFunction function = PsiTreeUtil.getParentOfType(argumentList, JSFunction.class);
                if (function != null) {
                    System.out.println("The enclosing function is: " + function.getName());
                }
            }
        }



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


    }
}
