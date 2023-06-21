package com.cross.crosstest.dialog;

import com.cross.crosstest.api.RestfulApiParser;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.EditorTextField;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class AddDialog extends DialogWrapper {
    private EditorTextField fileField;
    private EditorTextField apiField;
    private String selectedApi;

    public AddDialog(String selectedApi) {
        super(true);
        this.selectedApi = selectedApi;
        setTitle("select");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        fileField = new EditorTextField("File Location");
        apiField = new EditorTextField(selectedApi);
        fileField.setPreferredSize(new Dimension(200,50));
        apiField.setPreferredSize(new Dimension(200,100));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(fileField, gbc);

        gbc.gridy = 1;
        panel.add(apiField, gbc);

        return panel;
    }

    @Override
    protected JComponent createSouthPanel() {
        JPanel pan = new JPanel();
        JButton button = new JButton("Next");

        button.addActionListener(e -> {
            String location = fileField.getText();
            String text = apiField.getText();
            RestfulApiParser.parseApi(location);

//            System.out.println(text);
        });
        pan.add(button);
        return pan;
    }
}
