package de.escidoc.admintool.view.role;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.POJOContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;
import com.vaadin.ui.themes.Runo;

import de.escidoc.admintool.app.AdminToolApplication;
import de.escidoc.admintool.app.PropertyId;
import de.escidoc.admintool.service.ContextService;
import de.escidoc.admintool.service.RoleService;
import de.escidoc.admintool.service.ServiceContainer;
import de.escidoc.admintool.service.UserService;
import de.escidoc.admintool.view.ViewConstants;
import de.escidoc.admintool.view.admintask.ResourceType;
import de.escidoc.admintool.view.util.dialog.ErrorDialog;
import de.escidoc.core.client.exceptions.EscidocClientException;
import de.escidoc.core.client.exceptions.application.notfound.RoleNotFoundException;
import de.escidoc.core.resources.aa.role.Role;
import de.escidoc.core.resources.aa.useraccount.UserAccount;
import de.escidoc.core.resources.common.reference.ContextRef;
import de.escidoc.core.resources.om.context.Context;

public class RoleView extends CustomComponent {

    private static final long serialVersionUID = -1590899235898433438L;

    private static final Logger LOG = LoggerFactory.getLogger(RoleView.class);

    private static final String SEARCH_LABEL = "Search";

    private static final int RESOURCE_SELECTION_HEIGHT_IN_INTEGER = 400;

    private static final String RESOURCE_SELECTION_HEIGHT =
        RESOURCE_SELECTION_HEIGHT_IN_INTEGER + "px";

    private static final int COMPONENT_WIDTH_IN_INTEGER = 300;

    private static final String COMPONENT_WIDTH = COMPONENT_WIDTH_IN_INTEGER
        + "px";

    private static final String CAPTION = "Role Management";

    private final Panel panel = new Panel();

    private final VerticalLayout verticalLayout = new VerticalLayout();

    private final ComboBox userComboBox = new ComboBox("User Name:");

    private final ComboBox roleComboBox = new ComboBox("Role:");

    private final ComboBox resourceTypeComboBox = new ComboBox("Resouce Type:");

    final ListSelect resouceResult = new ListSelect();

    private final HorizontalLayout footer = new HorizontalLayout();

    private final Button saveBtn = new Button(ViewConstants.SAVE_LABEL,
        new SaveBtnListener());

    final Window mainWindow;

    private final ComponentContainer mainLayout = new FormLayout();

    final VerticalLayout resourceContainer = new VerticalLayout();

    final TextField searchBox = new TextField("Resource Title: ");

    private final Button searchButton = new Button(SEARCH_LABEL);

    private final ContextService contextService;

    private final AdminToolApplication app;

    private final RoleService roleService;

    private final UserService userService;

    private UserAccount selectedUser;

    private POJOContainer<UserAccount> userContainer;

    final ServiceContainer serviceContainer;

    // TODO: add logged in user;
    public RoleView(final AdminToolApplication app,
        final RoleService roleService, final UserService userService,
        final ContextService contextService,
        final ServiceContainer serviceContainer) {
        Preconditions.checkNotNull(app, "app is null: %s", app);
        Preconditions.checkNotNull(roleService, "roleService is null: %s",
            roleService);
        Preconditions.checkNotNull(userService, "userService is null: %s",
            userService);
        Preconditions.checkNotNull(contextService,
            "contextService is null: %s", contextService);
        Preconditions.checkNotNull(serviceContainer,
            "serviceContainer is null: %s", serviceContainer);
        this.app = app;
        this.roleService = roleService;
        this.userService = userService;
        this.contextService = contextService;
        this.serviceContainer = serviceContainer;
        mainWindow = app.getMainWindow();
        bindData();
    }

    public void init() {
        initLayout();
        addUserField();
        addRoleField();
        addResourceType();
        addResourceSearchBox();
        addResourceSelection();
        addFooter();
    }

    private void initLayout() {
        setCompositionRoot(panel);
        panel.setStyleName(Runo.PANEL_LIGHT);
        panel.setSizeFull();
        setSizeFull();
        verticalLayout.setWidth("100%");
        mainLayout.setWidth(400, UNITS_PIXELS);

        panel.setContent(verticalLayout);
        panel.setCaption(CAPTION);

        // TODO how to make panel take the whole vertical screen, if it does not
        // contain any child component;
        verticalLayout.setSpacing(true);
        verticalLayout.setMargin(true, false, false, true);
        verticalLayout.addComponent(mainLayout);
    }

    private void addUserField() {
        userComboBox.setWidth(COMPONENT_WIDTH);
        userComboBox.setNullSelectionAllowed(false);
        userComboBox.setMultiSelect(false);
        userComboBox.setRequired(true);
        mainLayout.addComponent(userComboBox);
    }

    private void addRoleField() {
        roleComboBox.setWidth(COMPONENT_WIDTH);
        roleComboBox.setNullSelectionAllowed(false);
        roleComboBox.setImmediate(true);
        roleComboBox.setRequired(true);
        roleComboBox.addListener(new RoleSelectListener());
        mainLayout.addComponent(roleComboBox);
    }

    private void addResourceType() {
        resourceTypeComboBox.setEnabled(false);
        resourceTypeComboBox.setWidth(COMPONENT_WIDTH);
        resourceTypeComboBox.setImmediate(true);
        mainLayout.addComponent(resourceTypeComboBox);
    }

    private void addResourceSearchBox() {
        searchBox.setWidth(Integer.toString(3 / 2 * COMPONENT_WIDTH_IN_INTEGER)
            + "px");
        searchBox.setEnabled(false);
        searchButton.setEnabled(false);
        mainLayout.addComponent(searchBox);
        searchButton.addListener(new SearchBtnListener());
    }

    private void addResourceSelection() {
        resouceResult.setSizeFull();
        resouceResult.setHeight(RESOURCE_SELECTION_HEIGHT);
        resouceResult.setImmediate(true);

        resouceResult.addListener(new ResourceSelectionListener(this));
        resourceContainer.setStyleName(Reindeer.PANEL_LIGHT);
        resourceContainer.setWidth(Integer
            .toString(3 / 2 * COMPONENT_WIDTH_IN_INTEGER) + "px");
        resourceContainer.setHeight(RESOURCE_SELECTION_HEIGHT);
        mainLayout.addComponent(resourceContainer);
    }

    private void addFooter() {
        footer.addComponent(saveBtn);

        final VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.addComponent(footer);
        verticalLayout.setComponentAlignment(footer, Alignment.MIDDLE_RIGHT);
        mainLayout.addComponent(verticalLayout);
    }

    private void bindData() {
        bindUserAccountData();
        bindRoleData();
        bindResourceTypeData();
    }

    private void bindUserAccountData() {
        userContainer =
            new POJOContainer<UserAccount>(UserAccount.class, PropertyId.NAME);
        for (final UserAccount user : getAllUserAccounts()) {
            userContainer.addPOJO(user);
        }
        userComboBox.setContainerDataSource(userContainer);
        userComboBox.setItemCaptionPropertyId(PropertyId.NAME);
    }

    private void bindRoleData() {
        final POJOContainer<Role> roleContainer =
            new POJOContainer<Role>(Role.class, PropertyId.OBJECT_ID,
                PropertyId.NAME);
        for (final Role role : getAllRoles()) {
            roleContainer.addPOJO(role);
        }
        roleComboBox.setContainerDataSource(roleContainer);
        roleComboBox.setItemCaptionPropertyId(PropertyId.NAME);
    }

    private void bindResourceTypeData() {
        final BeanItemContainer<ResourceType> resourceTypeContainer =
            new BeanItemContainer<ResourceType>(ResourceType.class,
                Arrays.asList(ResourceType.values()));
        resourceTypeComboBox.setContainerDataSource(resourceTypeContainer);
        resourceTypeComboBox.addListener(new ResourceTypeListener(this));
    }

    Collection<Context> getAllContexts() {
        try {
            return contextService.getCache();
        }
        catch (final EscidocClientException e) {
            mainWindow.addWindow(new ErrorDialog(mainWindow,
                ViewConstants.ERROR_DIALOG_CAPTION, e.getMessage()));
        }
        return Collections.emptyList();
    }

    private List<Role> getAllRoles() {
        try {
            return (List<Role>) roleService.findAll();
        }
        catch (final EscidocClientException e) {
            mainWindow.addWindow(new ErrorDialog(mainWindow,
                ViewConstants.ERROR_DIALOG_CAPTION, e.getMessage()));
        }
        return Collections.emptyList();
    }

    private Collection<UserAccount> getAllUserAccounts() {
        try {
            return userService.findAll();
        }
        catch (final EscidocClientException e) {
            mainWindow.addWindow(new ErrorDialog(mainWindow,
                ViewConstants.ERROR_DIALOG_CAPTION, e.getMessage()));
        }
        return Collections.emptyList();
    }

    public void selectUser(final UserAccount userAccount) {
        selectedUser = userAccount;
        userComboBox.select(userAccount);
    }

    private class SaveBtnListener implements Button.ClickListener {

        private static final long serialVersionUID = -7128599340989436927L;

        @Override
        public void buttonClick(final ClickEvent event) {
            onSaveClick();
        }

        private void onSaveClick() {
            assignRole();
        }

        private void assignRole() {
            try {
                userService
                    .assign(getSelectedUser()).withRole(getSelectedRole())
                    .onResources(getSelectedResources()).execute();
                app.showUser(getSelectedUser());
            }
            catch (final RoleNotFoundException e) {
                app.getMainWindow().addWindow(
                    new ErrorDialog(app.getMainWindow(),
                        ViewConstants.ERROR_DIALOG_CAPTION,
                        ViewConstants.REQUESTED_ROLE_HAS_NO_SCOPE_DEFINITIONS));
                LOG.error("An unexpected error occured! See LOG for details.",
                    e);
            }
            catch (final EscidocClientException e) {
                app.getMainWindow().addWindow(
                    new ErrorDialog(app.getMainWindow(),
                        ViewConstants.ERROR_DIALOG_CAPTION, e.getMessage()));
                LOG.error("An unexpected error occured! See LOG for details.",
                    e);
            }
        }

        private UserAccount getSelectedUser() {
            if (selectedUser == null) {
                return (UserAccount) userComboBox.getValue();
            }
            return selectedUser;
        }

        private Role getSelectedRole() {
            final Object value = roleComboBox.getValue();
            if (value instanceof Role) {
                return (Role) value;
            }
            return new Role();
        }

        private Set<ContextRef> getSelectedResources() {
            final Object value = resouceResult.getValue();
            if (value instanceof Context) {
                return Collections.singleton(new ContextRef(((Context) value)
                    .getObjid()));
            }
            return Collections.emptySet();
        }
    }

    private static class CancelBtnListener implements Button.ClickListener {

        private static final long serialVersionUID = -5938771331937438272L;

        @Override
        public void buttonClick(final ClickEvent event) {
            onCancelClick();
        }

        private void onCancelClick() {
            // TODO implement cancel behaviour
        }
    }

    private class RoleSelectListener implements ValueChangeListener {

        private static final long serialVersionUID = -4595870805889611817L;

        @Override
        public void valueChange(final ValueChangeEvent event) {
            onSelectedRole(event);
        }

        private void onSelectedRole(final ValueChangeEvent event) {
            final Object value = event.getProperty().getValue();
            if (value instanceof Role) {
                final Role r = (Role) value;

                enableScoping(isScopingEnable(r));
            }
        }

        private boolean isScopingEnable(final Role role) {
            if (role.getObjid().equals(
                RoleType.SYSTEM_ADMINISTRATOR.getObjectId())
                || role.getObjid().equals(
                    RoleType.SYSTEM_INSPECTOR.getObjectId())) {
                return false;
            }
            else {
                return true;
            }
        }

        private void enableScoping(final boolean isScopingEnabled) {
            resourceTypeComboBox.setEnabled(isScopingEnabled);
            searchBox.setEnabled(isScopingEnabled);
            searchButton.setEnabled(isScopingEnabled);
        }
    }

    private class SearchBtnListener implements Button.ClickListener {

        private static final long serialVersionUID = -2520068834542312077L;

        private Collection<Context> foundContexts;

        @Override
        public void buttonClick(final ClickEvent event) {
            onSearchClick(event);
        }

        private void onSearchClick(final ClickEvent event) {
            final Object value = searchBox.getValue();

            if (value instanceof String) {
                // TODO search resource with type:[resourceType] and
                // title:[userInput] OR objectID:[userInput]
                final String userInput = (String) value;
                foundContexts = seachContextByName(userInput);
                if (isContextFound()) {
                    mainWindow.showNotification(foundContexts
                        .iterator().next().getObjid());
                }
                mainWindow.showNotification("Not found");
            }
        }

        private boolean isContextFound() {
            return !foundContexts.isEmpty();
        }

        private Collection<Context> seachContextByName(final String userInput) {
            try {
                return contextService.findByTitle(userInput);
            }
            catch (final EscidocClientException e) {
                app.getMainWindow().addWindow(
                    new ErrorDialog(app.getMainWindow(),
                        ViewConstants.ERROR_DIALOG_CAPTION,
                        "An unexpected error occured! See LOG for details."));
                LOG.error("An unexpected error occured! See LOG for details.",
                    e);
            }
            return Collections.emptyList();
        }
    }
}