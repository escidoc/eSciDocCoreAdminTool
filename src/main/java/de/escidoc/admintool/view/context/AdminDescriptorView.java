package de.escidoc.admintool.view.context;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.vaadin.terminal.SystemError;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

import de.escidoc.core.resources.om.context.AdminDescriptor;
import de.escidoc.vaadin.dialog.ErrorDialog;

@SuppressWarnings("serial")
public abstract class AdminDescriptorView extends Window {
    private final Logger log =
        LoggerFactory.getLogger(AdminDescriptorView.class);

    protected static final String EDIT_ADMIN_DESCRIPTOR =
        "Edit Admin Descriptor";

    protected final TextField adminDescName = new TextField("Name: ");

    protected final TextField adminDescContent = new TextField("Content: ");

    private final FormLayout formLayout = new FormLayout();

    private final HorizontalLayout footer = new HorizontalLayout();

    private final Button saveButton = new Button("Save");

    private final Button cancelButton = new Button("Cancel");

    protected Accordion adminDescriptorAccordion = null;

    protected String name;

    protected String content;

    private Window mainWindow;

    public AdminDescriptorView(final Window mainWindow,
        final Accordion adminDescriptorAccordion) {
        this.mainWindow = mainWindow;
        this.adminDescriptorAccordion = adminDescriptorAccordion;
        buildMainLayout();
    }

    public AdminDescriptorView(final Accordion adminDescriptorAccordion,
        final String name, final String content) {
        this.adminDescriptorAccordion = adminDescriptorAccordion;
        this.name = name;
        this.content = content;
        buildMainLayout();
    }

    protected void buildMainLayout() {
        setWindowCaption();
        setModal(true);
        setWidth("600px");
        setHeight("400px");

        adminDescName.setWidth("400px");
        adminDescContent.setRows(5);
        adminDescContent.setWidth("400px");

        if (name != null) {
            adminDescName.setValue(name);
        }

        if (content != null) {
            adminDescContent.setValue(content);
        }
        formLayout.addComponent(adminDescName);
        formLayout.addComponent(adminDescContent);

        addFooter();
        addComponent(formLayout);
    }

    protected abstract void setWindowCaption();

    private void addFooter() {
        footer.addComponent(saveButton);
        footer.addComponent(cancelButton);

        getSaveButton().addListener(new Button.ClickListener() {
            public void buttonClick(final ClickEvent event) {
                doSave();
            }
        });

        cancelButton.addListener(new Button.ClickListener() {
            public void buttonClick(final ClickEvent event) {
                closeWindow();
            }
        });

        formLayout.addComponent(footer);
    }

    protected abstract void doSave();

    protected class SaveButtonListener implements Button.ClickListener {

        public void buttonClick(final ClickEvent event) {
            final String content = (String) adminDescContent.getValue();
            if (validate(content)) {
                adminDescriptorAccordion.addTab(new Label(content,
                    Label.CONTENT_PREFORMATTED), (String) adminDescName
                    .getValue(), null);
                closeWindow();
            }
        }
    }

    protected boolean validate(final String value) {
        return enteredAdminDescriptors(value);
    }

    protected void closeWindow() {
        getApplication().getMainWindow().removeWindow(this);
    }

    public Button getSaveButton() {
        return saveButton;
    }

    private boolean enteredAdminDescriptors(final String value) {
        final AdminDescriptor adminDescriptor = new AdminDescriptor();
        adminDescriptor.setName((String) adminDescName.getValue());
        try {
            adminDescriptor.setContent(value);
            return true;
        }
        catch (final ParserConfigurationException e) {
            log.error("An unexpected error occured! See log for details.", e);
            mainWindow.addWindow(new ErrorDialog(mainWindow, "Error", e
                .getMessage()));
            setComponentError(new SystemError(e.getMessage()));
              
            return false;
        }
        catch (final SAXException e) {
            final ErrorDialog errorDialog =
                new ErrorDialog(mainWindow, "Error", "XML is not well formed.");
            errorDialog.setWidth("400px");
            errorDialog.setWidth("300px");
            mainWindow.addWindow(errorDialog);
            log.error("An unexpected error occured! See log for details.", e);
            setComponentError(new SystemError(e.getMessage()));
              
            return false;

        }
        catch (final IOException e) {
            log.error("An unexpected error occured! See log for details.", e);
            mainWindow.addWindow(new ErrorDialog(mainWindow, "Error", e
                .getMessage()));
            setComponentError(new SystemError(e.getMessage()));
              
            return false;
        }
    }
}