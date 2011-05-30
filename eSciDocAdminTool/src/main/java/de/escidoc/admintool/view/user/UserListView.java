package de.escidoc.admintool.view.user;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.jensjansson.pagedtable.PagedTable;
import com.vaadin.data.util.POJOContainer;
import com.vaadin.data.util.POJOItem;
import com.vaadin.ui.Table;

import de.escidoc.admintool.app.AdminToolApplication;
import de.escidoc.admintool.app.PropertyId;
import de.escidoc.admintool.service.UserService;
import de.escidoc.admintool.view.ViewConstants;
import de.escidoc.admintool.view.util.dialog.ErrorDialog;
import de.escidoc.core.client.exceptions.EscidocClientException;
import de.escidoc.core.resources.aa.useraccount.UserAccount;

@SuppressWarnings("serial")
public class UserListView extends PagedTable {

    private static final Logger LOG = LoggerFactory.getLogger(UserListView.class);

    private final AdminToolApplication app;

    private final UserService userService;

    private final Collection<UserAccount> allUserAccounts = new ArrayList<UserAccount>();

    private POJOContainer<UserAccount> userContainer;

    public UserListView(final AdminToolApplication app, final UserService userService) {
        Preconditions.checkNotNull(app, "app is null: %s", app);
        Preconditions.checkNotNull(userService, "userService is null: %s", userService);
        this.app = app;
        this.userService = userService;
        buildView();
        findAllUsers();
        bindDataSource();
        setPageLength(50);
    }

    private void buildView() {
        setSizeFull();
        setSelectable(true);
        setImmediate(true);
        addListener(new UserSelectListener(app));
        setNullSelectionAllowed(false);
        setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
    }

    private void findAllUsers() {
        try {
            allUserAccounts.addAll(userService.findAll());
        }
        catch (final EscidocClientException e) {
            app.getMainWindow().addWindow(
                new ErrorDialog(app.getMainWindow(), "Error", "An unexpected error occured! See LOG for details."));
            LOG.error("An unexpected error occured! See LOG for details.", e);
        }
    }

    private void bindDataSource() {
        if (isUserExists()) {
            initUserContainer();
        }
    }

    private boolean isUserExists() {
        return allUserAccounts != null && !allUserAccounts.isEmpty();
    }

    private void initUserContainer() {
        userContainer =
            new POJOContainer<UserAccount>(allUserAccounts, PropertyId.OBJECT_ID, PropertyId.NAME,
                PropertyId.CREATED_ON, PropertyId.CREATED_BY, PropertyId.LAST_MODIFICATION_DATE,
                PropertyId.MODIFIED_BY, PropertyId.LOGIN_NAME, PropertyId.ACTIVE);
        setContainerDataSource(userContainer);
        setVisibleColumns(new Object[] { PropertyId.NAME });
        setColumnHeader(PropertyId.NAME, ViewConstants.TITLE_LABEL);
        sort(new Object[] { PropertyId.LAST_MODIFICATION_DATE }, new boolean[] { false });
    }

    public void remove(final UserAccount deletedUser) {
        final boolean removeItem = userContainer.removeItem(deletedUser);
        assert removeItem == true : "Failed to remove user account from the container";
    }

    public POJOItem<UserAccount> addUser(final UserAccount createdUserAccount) {
        final POJOItem<UserAccount> item = userContainer.addItem(createdUserAccount);
        sort(new Object[] { PropertyId.LAST_MODIFICATION_DATE }, new boolean[] { true });
        return item;
    }
}