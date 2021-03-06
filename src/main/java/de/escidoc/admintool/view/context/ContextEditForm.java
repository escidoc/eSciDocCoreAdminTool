/**
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at license/ESCIDOC.LICENSE
 * or https://www.escidoc.org/license/ESCIDOC.LICENSE .
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at license/ESCIDOC.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *
 * Copyright 2011 Fachinformationszentrum Karlsruhe Gesellschaft
 * fuer wissenschaftlich-technische Information mbH and Max-Planck-
 * Gesellschaft zur Foerderung der Wissenschaft e.V.
 * All rights reserved.  Use is subject to license terms.
 */
package de.escidoc.admintool.view.context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;
import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.POJOItem;
import com.vaadin.terminal.SystemError;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

import de.escidoc.admintool.app.AdminToolApplication;
import de.escidoc.admintool.app.AppConstants;
import de.escidoc.admintool.app.PropertyId;
import de.escidoc.admintool.domain.PdpRequest;
import de.escidoc.admintool.domain.PublicStatus;
import de.escidoc.admintool.exception.ResourceNotFoundException;
import de.escidoc.admintool.service.internal.ContextService;
import de.escidoc.admintool.service.internal.OrgUnitService;
import de.escidoc.admintool.view.ViewConstants;
import de.escidoc.admintool.view.context.listener.EditAdminDescriptorListener;
import de.escidoc.admintool.view.context.listener.NewAdminDescriptorListener;
import de.escidoc.admintool.view.context.listener.RemoveAdminDescriptorListener;
import de.escidoc.admintool.view.navigation.ActionIdConstants;
import de.escidoc.admintool.view.resource.ResourceRefDisplay;
import de.escidoc.admintool.view.util.Converter;
import de.escidoc.admintool.view.util.LayoutHelper;
import de.escidoc.admintool.view.util.dialog.ErrorDialog;
import de.escidoc.admintool.view.validator.EmptyFieldValidator;
import de.escidoc.core.client.exceptions.EscidocClientException;
import de.escidoc.core.client.exceptions.EscidocException;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.client.exceptions.TransportException;
import de.escidoc.core.resources.common.reference.OrganizationalUnitRef;
import de.escidoc.core.resources.common.reference.UserAccountRef;
import de.escidoc.core.resources.om.context.AdminDescriptor;
import de.escidoc.core.resources.om.context.AdminDescriptors;
import de.escidoc.core.resources.om.context.Context;
import de.escidoc.core.resources.om.context.OrganizationalUnitRefs;

public class ContextEditForm extends CustomComponent implements ClickListener {

    private static final long serialVersionUID = 249276128897788486L;

    private static final Logger LOG = LoggerFactory.getLogger(ContextEditForm.class);

    private static final int LABEL_WIDTH = 111;

    private static final int HEIGHT = 15;

    private final List<Field> fields = new ArrayList<Field>();

    private final Panel panel = new Panel();

    private final FormLayout form = new FormLayout();

    private final TextField nameField = new TextField();

    private final TextField descriptionField = new TextField();

    private final Label objIdField = new Label();

    private final Label modifiedOn = new Label();

    private final Label createdOn = new Label();

    private final Label status = new Label();

    private final TextField typeField = new TextField();

    private ListSelect orgUnitList;

    private final Button saveButton = new Button(ViewConstants.SAVE_LABEL, this);

    private final Button cancelButton = new Button(ViewConstants.CANCEL_LABEL, this);

    private final Button addAdminDescButton = new Button(ViewConstants.ADD_LABEL);

    private final Button editAdminDescButton = new Button(ViewConstants.EDIT_LABEL);

    private final Button delAdminDescButton = new Button(ViewConstants.DELETE);

    private final Button addOrgUnitButton = new Button(ViewConstants.ADD_LABEL);

    private final Button removeOrgUnitButton = new Button(ViewConstants.REMOVE_LABEL);

    private final AdminToolApplication app;

    private final ContextService contextService;

    private final OrgUnitService orgUnitService;

    private HorizontalLayout footer = new HorizontalLayout();

    private Accordion adminDescriptorAccordion;

    private ContextToolbar editToolbar;

    private ContextListView contextList;

    private Item item;

    private final Window mainWindow;

    private final AddOrgUnitToTheList addOrgUnitToTheList;

    private final PdpRequest pdpRequest;

    private Button createdByLink;

    private LinkClickListener modifiedByLinkListener;

    private Button modifiedByLink;

    private LinkClickListener creatorLinkListener;

    public ContextEditForm(final AdminToolApplication app, final Window mainWindow,
        final ContextService contextService, final OrgUnitService orgUnitService,
        final AddOrgUnitToTheList addOrgUnitToTheList, final PdpRequest pdpRequest) {

        checkPreconditions(app, mainWindow, contextService, orgUnitService, addOrgUnitToTheList, pdpRequest);
        this.app = app;
        this.mainWindow = mainWindow;
        this.contextService = contextService;
        this.orgUnitService = orgUnitService;
        this.addOrgUnitToTheList = addOrgUnitToTheList;
        this.pdpRequest = pdpRequest;
    }

    private void checkPreconditions(
        final AdminToolApplication app, final Window mainWindow, final ContextService contextService,
        final OrgUnitService orgUnitService, final AddOrgUnitToTheList addOrgUnitToTheList, final PdpRequest pdpRequest) {
        Preconditions.checkNotNull(mainWindow, "mainWindow can not be null: %s", mainWindow);
        Preconditions.checkNotNull(app, "AdminToolApplication can not be null.");
        Preconditions.checkNotNull(contextService, "ContextService can not be null.");
        Preconditions.checkNotNull(orgUnitService, "OrgUnitService can not be null.");
        Preconditions.checkNotNull(addOrgUnitToTheList, "addOrgUnitToTheList can not be null.");
        Preconditions.checkNotNull(pdpRequest, "pdpRequest can not be null.");
    }

    public void init() {
        configureLayout();
        addFields();
        addSpace();
        addFooter();
    }

    private void addSpace() {
        form.addComponent(new Label("<br/>", Label.CONTENT_XHTML));
        form.addComponent(new Label("<br/>", Label.CONTENT_XHTML));
    }

    private void configureLayout() {
        setCompositionRoot(panel);
        panel.setStyleName(Reindeer.PANEL_LIGHT);
        panel.setCaption(ViewConstants.EDIT_USER_ACCOUNT);
        panel.setContent(form);

        form.setSpacing(false);
        form.setWidth(530, UNITS_PIXELS);

        editToolbar = new ContextToolbar(this, app, pdpRequest);
        editToolbar.init();
        form.addComponent(editToolbar);
    }

    private void addFooter() {
        footer = new HorizontalLayout();
        footer.setWidth(100, UNITS_PERCENTAGE);
        final HorizontalLayout hl = new HorizontalLayout();
        hl.addComponent(saveButton);
        hl.addComponent(cancelButton);

        footer.addComponent(hl);
        footer.setComponentAlignment(hl, Alignment.MIDDLE_RIGHT);

        form.addComponent(footer);
    }

    private void addFields() {
        setFieldsWriteThrough(false);
        addNameField();
        addDescField();
        addObjectId();
        addModified();
        addCreated();
        addStatus();
        addType();
        addOrgUnit();
        addAdminDescriptor();
    }

    private void addType() {
        typeField.setWidth(ViewConstants.FIELD_WIDTH);
        fields.add(typeField);
        form.addComponent(LayoutHelper.create("Type", typeField, LABEL_WIDTH, false));
    }

    private void addStatus() {
        form.addComponent(LayoutHelper.create("Status", status, LABEL_WIDTH, false));
    }

    private void addCreated() {
        createCreatedByLink();
        form.addComponent(LayoutHelper.create("Created", "by", createdOn, createdByLink, LABEL_WIDTH, HEIGHT, false));
    }

    private void createCreatedByLink() {
        createdByLink = new Button();
        createdByLink.setStyleName(BaseTheme.BUTTON_LINK);
        creatorLinkListener = new LinkClickListener(app);
        createdByLink.addListener(creatorLinkListener);
    }

    private void addModified() {
        createModifiedByLink();
        form
            .addComponent(LayoutHelper.create("Modified", "by", modifiedOn, modifiedByLink, LABEL_WIDTH, HEIGHT, false));
    }

    private void createModifiedByLink() {
        modifiedByLink = new Button();
        modifiedByLink.setStyleName(BaseTheme.BUTTON_LINK);
        modifiedByLinkListener = new LinkClickListener(app);
        modifiedByLink.addListener(modifiedByLinkListener);
    }

    private boolean isRetrieveUserPermitted(final String userId) {
        return pdpRequest.isPermitted(ActionIdConstants.RETRIEVE_USER_ACCOUNT, userId);
    }

    private void addObjectId() {
        form.addComponent(LayoutHelper.create(ViewConstants.OBJECT_ID_LABEL, objIdField, LABEL_WIDTH, false));
    }

    private void addOrgUnit() {
        createOrgUnitList();
        addOrgUnitEditor();
        form.addComponent(new Label(" &nbsp; ", Label.CONTENT_XHTML));
    }

    private void createOrgUnitList() {
        orgUnitList = new ListSelect();
        orgUnitList.setRows(5);
        orgUnitList.setWidth(ViewConstants.FIELD_WIDTH);
        orgUnitList.setNullSelectionAllowed(true);
        orgUnitList.setMultiSelect(true);
        orgUnitList.setWriteThrough(false);
    }

    private void addOrgUnitEditor() {
        setButtonsStyleToSmall();
        addOrgUnitButtonListeners();

        final VerticalLayout orgUnitEditor =
            LayoutHelper.create(ViewConstants.ORGANIZATION_UNITS_LABEL, orgUnitList, LABEL_WIDTH, 90, true,
                new Button[] { addOrgUnitButton, removeOrgUnitButton });
        form.addComponent(orgUnitEditor);
    }

    private void addAdminDescriptor() {
        adminDescriptorAccordion = new Accordion();
        adminDescriptorAccordion.setWidth(ViewConstants.FIELD_WIDTH);
        adminDescriptorAccordion.setSizeFull();

        final Panel accordionPanel = new Panel();
        accordionPanel.setContent(adminDescriptorAccordion);
        accordionPanel.setSizeFull();
        accordionPanel.setWidth(ViewConstants.FIELD_WIDTH);

        addAdminDescButton.setStyleName(AppConstants.SMALL_BUTTON);
        editAdminDescButton.setStyleName(AppConstants.SMALL_BUTTON);
        delAdminDescButton.setStyleName(AppConstants.SMALL_BUTTON);

        addAdminDescButton.addListener(new NewAdminDescriptorListener(mainWindow, adminDescriptorAccordion));
        editAdminDescButton.addListener(new EditAdminDescriptorListener(mainWindow, adminDescriptorAccordion));
        delAdminDescButton.addListener(new RemoveAdminDescriptorListener(adminDescriptorAccordion));

        form.addComponent(LayoutHelper.create("Admin Descriptors", accordionPanel, LABEL_WIDTH, 300, false,
            new Button[] { addAdminDescButton, editAdminDescButton, delAdminDescButton }));
    }

    private void addDescField() {
        descriptionField.setWidth(ViewConstants.FIELD_WIDTH);
        descriptionField.setRows(3);
        descriptionField.setMaxLength(ViewConstants.MAX_DESC_LENGTH);
        fields.add(descriptionField);
        form
            .addComponent(LayoutHelper.create(ViewConstants.DESCRIPTION_LABEL, descriptionField, LABEL_WIDTH, 80, true));
    }

    private void addNameField() {
        nameField.setWidth(ViewConstants.FIELD_WIDTH);
        nameField.setWriteThrough(false);
        nameField.setMaxLength(ViewConstants.MAX_TITLE_LENGTH);
        form.addComponent(LayoutHelper.create(ViewConstants.NAME_LABEL, nameField, LABEL_WIDTH, true));
    }

    private void setButtonsStyleToSmall() {
        addOrgUnitButton.setStyleName(AppConstants.SMALL_BUTTON);
        removeOrgUnitButton.setStyleName(AppConstants.SMALL_BUTTON);
    }

    private void addOrgUnitButtonListeners() {
        addOrgUnitButton.addListener(addOrgUnitToTheList);
        removeOrgUnitButton.addListener(new RemoveOrgUnitFromList(orgUnitList));
    }

    @Override
    public void buttonClick(final ClickEvent event) {
        final Button clickedButton = event.getButton();
        if (clickedButton.equals(saveButton)) {
            save();
        }
        else if (clickedButton.equals(cancelButton)) {
            discard();
        }
        else {
            throw new IllegalArgumentException("Unknown Button " + clickedButton);
        }
    }

    private void setFieldsWriteThrough(final boolean isWriteThrough) {
        for (final Field field : fields) {
            field.setWriteThrough(isWriteThrough);
        }
    }

    private void save() {
        if (isValid()) {
            try {
                final AdminDescriptors adminDescriptors = enteredAdminDescriptors();
                final OrganizationalUnitRefs selectedOrgUnitRefs = getEnteredOrgUnitRefs();
                final Context updatedContext =
                    contextService.update((String) objIdField.getValue(), (String) nameField.getValue(),
                        (String) descriptionField.getValue(), (String) typeField.getValue(), selectedOrgUnitRefs,
                        adminDescriptors);
                nameField.commit();
                contextList.updateContext(getContextFromCache(), updatedContext);
                updateView(updatedContext);
                removeAllErrorMessages();
                showMessage();
            }
            catch (final EscidocClientException e) {
                LOG.error(e.getMessage());
                mainWindow.addWindow(new ErrorDialog(mainWindow, ViewConstants.ERROR, e.getMessage()));
            }
        }
    }

    private void updateView(final Context updatedContext) {
        setSelected(contextToItem(updatedContext));
    }

    private Item contextToItem(final Context updatedContext) {
        return new POJOItem<Context>(updatedContext, AppConstants.CONTEXT_PROPERTY_NAMES);
    }

    private boolean isValid() {
        boolean valid = true;
        valid = EmptyFieldValidator.isValid(nameField, "Name can not be empty.");
        valid &= EmptyFieldValidator.isValid(descriptionField, "Description can not be empty.");
        valid &= EmptyFieldValidator.isValid(typeField, ViewConstants.TYPE_LABEL + " can not be empty.");
        valid &= EmptyFieldValidator.isValid(orgUnitList, ViewConstants.ORGANIZATION_UNITS_LABEL + " can not be empty");
        return valid;
    }

    private void removeAllErrorMessages() {
        nameField.setComponentError(null);
        descriptionField.setComponentError(null);
        typeField.setComponentError(null);
        orgUnitList.setComponentError(null);
        adminDescriptorAccordion.setComponentError(null);
    }

    private void showMessage() {
        mainWindow
            .showNotification(new Notification("Info", "Context is updated", Notification.TYPE_TRAY_NOTIFICATION));
    }

    private OrganizationalUnitRefs getEnteredOrgUnitRefs() {
        final OrganizationalUnitRefs organizationalUnitRefs = new OrganizationalUnitRefs();

        for (final String objectId : getEnteredOrgUnits()) {
            organizationalUnitRefs.add(new OrganizationalUnitRef(objectId));
        }

        return organizationalUnitRefs;
    }

    private Set<String> getEnteredOrgUnits() {

        if (orgUnitList.getContainerDataSource() == null || orgUnitList.getContainerDataSource().getItemIds() == null
            || orgUnitList.getContainerDataSource().getItemIds().size() == 0
            || !orgUnitList.getContainerDataSource().getItemIds().iterator().hasNext()) {
            return Collections.emptySet();
        }

        final Set<String> orgUnits = new HashSet<String>();

        final Collection<?> itemIds = orgUnitList.getContainerDataSource().getItemIds();

        for (final Object object : itemIds) {
            final ResourceRefDisplay ou = (ResourceRefDisplay) object;
            orgUnits.add(ou.getObjectId());
        }

        return orgUnits;
    }

    private AdminDescriptors enteredAdminDescriptors() {
        final AdminDescriptors adminDescriptors = new AdminDescriptors();
        final Iterator<Component> it = adminDescriptorAccordion.getComponentIterator();
        while (it != null && it.hasNext()) {
            final Component contentComp = it.next();
            final Tab tab = adminDescriptorAccordion.getTab(contentComp);
            final String adminDescName = tab.getCaption();
            String adminDescContent = "";
            if (contentComp instanceof Label) {
                adminDescContent = ((String) ((Label) contentComp).getValue());
            }
            final AdminDescriptor adminDescriptor = new AdminDescriptor(adminDescName);
            adminDescriptor.setName(adminDescName);
            try {
                adminDescriptor.setContent(adminDescContent);
                adminDescriptors.add(adminDescriptor);
                // TODO: move to appropriate class
            }
            catch (final ParserConfigurationException e) {
                LOG.error("An unexpected error occured! See LOG for details.", e);
                mainWindow.addWindow(new ErrorDialog(mainWindow, ViewConstants.ERROR, e.getMessage()));
                setComponentError(new SystemError(e.getMessage()));
            }
            catch (final SAXException e) {
                LOG.error("An unexpected error occured! See LOG for details.", e);
                mainWindow.addWindow(new ErrorDialog(mainWindow, ViewConstants.ERROR, e.getMessage()));
                setComponentError(new SystemError(e.getMessage()));
            }
            catch (final IOException e) {
                LOG.error("An unexpected error occured! See LOG for details.", e);
                mainWindow.addWindow(new ErrorDialog(mainWindow, ViewConstants.ERROR, e.getMessage()));
                setComponentError(new SystemError(e.getMessage()));
            }
        }

        return adminDescriptors;
    }

    private void discard() {
        nameField.discard();
        nameField.setComponentError(null);
        descriptionField.discard();
        descriptionField.setComponentError(null);
        typeField.discard();
        typeField.setComponentError(null);
        orgUnitList.discard();
        orgUnitList.setComponentError(null);
        adminDescriptorAccordion.removeAllComponents();
    }

    public void setSelected(final Item item) {
        this.item = item;
        if (item == null) {
            return;
        }
        bindData();
    }

    private void bindData() {
        bindPublicStatusWithView();
        bindName();
        bindObjectId();
        bindStatus();
        bindModifiedOn();
        bindModifiedBy();
        bindCreatedOn();
        bindCreatedBy();
        bindType();
        bindDescription();
        bindOrgUnit();
        bindAdminDescriptor();
        bindUserRightWithView();
    }

    private void bindName() {
        nameField.setPropertyDataSource(item.getItemProperty(ViewConstants.NAME_ID));
    }

    private void bindObjectId() {
        objIdField.setPropertyDataSource(item.getItemProperty(PropertyId.OBJECT_ID));
    }

    private void bindStatus() {
        status.setPropertyDataSource(new ObjectProperty<PublicStatus>(PublicStatus
            .from((de.escidoc.core.resources.common.properties.PublicStatus) item.getItemProperty(
                PropertyId.PUBLIC_STATUS).getValue()), PublicStatus.class));
    }

    @SuppressWarnings("unchecked")
    private void bindAdminDescriptor() {
        adminDescriptorAccordion.removeAllComponents();

        final List<AdminDescriptor> adminDescriptors =
            (List<AdminDescriptor>) item.getItemProperty(PropertyId.ADMIN_DESCRIPTORS).getValue();

        for (final AdminDescriptor adminDescriptor : adminDescriptors) {
            try {
                adminDescriptorAccordion.addTab(new Label(adminDescriptor.getContentAsString(),
                    Label.CONTENT_PREFORMATTED), adminDescriptor.getName(), null);
            }
            catch (final TransformerException e) {
                LOG.error("An unexpected error occured! See LOG for details.", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void bindOrgUnit() {
        orgUnitList.removeAllItems();
        final List<OrganizationalUnitRef> refs =
            (List<OrganizationalUnitRef>) item.getItemProperty(PropertyId.ORG_UNIT_REFS).getValue();

        for (final OrganizationalUnitRef resourceRef : refs) {
            final ResourceRefDisplay resourceRefDisplay =
                new ResourceRefDisplay(resourceRef.getObjid(), findOrgUnitTitle(resourceRef));
            orgUnitList.addItem(resourceRefDisplay);
        }
        addOrgUnitToTheList.using(orgUnitList);
    }

    private String findOrgUnitTitle(final OrganizationalUnitRef resourceRef) {
        try {
            return orgUnitService.findOrgUnitTitleById(resourceRef.getObjid());
        }
        catch (final ResourceNotFoundException e) {
            LOG.warn(e.getMessage());
            mainWindow.addWindow(new ErrorDialog(mainWindow, ViewConstants.ERROR,
                "Organizational Unit does not exist anymore." + e.getMessage()));
        }
        catch (final EscidocException e) {
            LOG.error(e.getMessage());
            mainWindow.addWindow(new ErrorDialog(mainWindow, ViewConstants.ERROR, e.getMessage()));
        }
        catch (final InternalClientException e) {
            LOG.error(e.getMessage());
            mainWindow.addWindow(new ErrorDialog(mainWindow, ViewConstants.ERROR, e.getMessage()));
        }
        catch (final TransportException e) {
            LOG.error(e.getMessage());
            mainWindow.addWindow(new ErrorDialog(mainWindow, ViewConstants.ERROR, e.getMessage()));
        }
        return "Organization does exist, please remove.";
    }

    private void bindDescription() {
        descriptionField.setPropertyDataSource(item.getItemProperty(PropertyId.DESCRIPTION));
    }

    private void bindType() {
        typeField.setPropertyDataSource(item.getItemProperty(PropertyId.TYPE));
    }

    private void bindCreatedOn() {
        createdOn.setCaption(Converter.dateTimeToString((org.joda.time.DateTime) item.getItemProperty(
            PropertyId.CREATED_ON).getValue()));
    }

    private void bindCreatedBy() {
        createdByLink.setCaption(getCreatorName());
        if (isRetrieveUserPermitted(getCreatorId())) {
            creatorLinkListener.setUser(getCreatorId());
        }
        else {
            createdByLink.setEnabled(false);
        }
    }

    private void bindModifiedOn() {
        modifiedOn.setCaption(Converter.dateTimeToString((org.joda.time.DateTime) item.getItemProperty(
            PropertyId.LAST_MODIFICATION_DATE).getValue()));
    }

    private String getCreatorId() {
        return getCreator().getObjid();
    }

    private String getCreatorName() {
        return getCreator().getXLinkTitle();
    }

    private UserAccountRef getCreator() {
        return (UserAccountRef) item.getItemProperty(PropertyId.CREATED_BY).getValue();
    }

    private void bindModifiedBy() {
        modifiedByLink.setCaption(getModifierName());

        if (isRetrieveUserPermitted(getModifierId())) {
            modifiedByLinkListener.setUser(getModifierId());
        }
        else {
            modifiedByLink.setEnabled(false);
        }
    }

    private String getModifierId() {
        return getModifier().getObjid();
    }

    private String getModifierName() {
        return getModifier().getXLinkTitle();
    }

    private UserAccountRef getModifier() {
        return (UserAccountRef) item.getItemProperty(PropertyId.MODIFIED_BY).getValue();
    }

    private void bindPublicStatusWithView() {
        final Object value = item.getItemProperty(PropertyId.PUBLIC_STATUS).getValue();
        if (!(value instanceof de.escidoc.core.resources.common.properties.PublicStatus)) {
            throw new RuntimeException(value.getClass() + " is not instance of PublicStatus");
        }
        else {
            final PublicStatus publicStatus =
                PublicStatus.from((de.escidoc.core.resources.common.properties.PublicStatus) value);
            switch (publicStatus) {
                case CREATED:
                case OPENED: {
                    setFormReadOnly(false);
                    footer.setVisible(true);
                    break;
                }
                case CLOSED: {
                    setFormReadOnly(true);
                    footer.setVisible(false);
                    break;
                }
            }
            editToolbar.setSelected(publicStatus);
        }
    }

    private void bindUserRightWithView() {
        editToolbar.bind(getSelectedItemId());
        setFormReadOnly(isUpdateNotAllowed());
        footer.setVisible(!isUpdateNotAllowed());
    }

    private boolean isUpdateNotAllowed() {
        return pdpRequest.isDenied(ActionIdConstants.UPDATE_CONTEXT, getSelectedItemId());
    }

    protected Context open(final String comment) throws EscidocException, InternalClientException, TransportException {
        final Context cachedContext = getContextFromCache();
        final Context openedContext = openContext(comment);
        updateContextList(cachedContext, openedContext);
        updateEditView(PublicStatus.OPENED);
        return openedContext;
    }

    private Context getContextFromCache() {
        return contextService.getSelected(getSelectedItemId());
    }

    private Context openContext(final String comment) throws EscidocException, InternalClientException,
        TransportException {
        return contextService.open(getSelectedItemId(), comment);
    }

    private void updateContextList(final Context before, final Context openedContext) {
        contextList.updateContext(before, openedContext);
    }

    private void updateEditView(final PublicStatus publicStatus) {
        item.getItemProperty(PropertyId.PUBLIC_STATUS).setValue(PublicStatus.to(publicStatus));
        editToolbar.setSelected(publicStatus);
        setSelected(item);
    }

    protected Context close(final String comment) throws EscidocClientException {
        final Context cachedContext = getContextFromCache();
        final Context closedContext = closeContext(comment);
        updateContextList(cachedContext, closedContext);
        updateEditView(PublicStatus.CLOSED);

        setFormReadOnly(true);
        footer.setVisible(false);
        return closedContext;
    }

    private Context closeContext(final String comment) throws EscidocClientException {
        return contextService.close(getSelectedItemId(), comment);
    }

    private void setFormReadOnly(final boolean isReadOnly) {
        nameField.setReadOnly(isReadOnly);
        descriptionField.setReadOnly(isReadOnly);
        typeField.setReadOnly(isReadOnly);
        orgUnitList.setReadOnly(isReadOnly);
        adminDescriptorAccordion.setReadOnly(isReadOnly);

        addOrgUnitButton.setVisible(!isReadOnly);
        removeOrgUnitButton.setVisible(!isReadOnly);
        addAdminDescButton.setVisible(!isReadOnly);
        editAdminDescButton.setVisible(!isReadOnly);
        delAdminDescButton.setVisible(!isReadOnly);
    }

    private String getSelectedItemId() {
        return (String) item.getItemProperty(ViewConstants.OBJECT_ID).getValue();
    }

    public void setContextList(final ContextListView contextList) {
        this.contextList = contextList;
    }

    protected void deleteContext() {
        try {
            final Context selected = contextService.getSelected(getSelectedItemId());
            contextService.delete(getSelectedItemId());
            contextList.removeContext(selected);
            app.showContextView();
        }
        catch (final EscidocClientException e) {
            LOG.error("An unexpected error occured! See LOG for details.", e);
            mainWindow.addWindow(new ErrorDialog(mainWindow, ViewConstants.ERROR, e.getMessage()));
        }
    }
}