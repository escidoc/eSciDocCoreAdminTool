package de.escidoc.admintool.view.user.password;

import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.PasswordField;

public interface PasswordView extends ComponentContainer {

    void addPasswordField();

    void addRetypePasswordField();

    void setPassword(String emptyPassword);

    void addMinCharValidator();

    void addOkButton(ClickListener updatePasswordOkListener);

    PasswordField getPasswordField();

    PasswordField getRetypePasswordField();

    void addCancelButton(ClickListener cancelListener);

    void resetFields();

    void removeErrorMessages();

}