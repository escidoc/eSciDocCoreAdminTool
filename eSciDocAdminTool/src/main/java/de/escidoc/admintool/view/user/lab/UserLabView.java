package de.escidoc.admintool.view.user.lab;

import com.vaadin.data.Item;
import com.vaadin.ui.SplitPanel;

import de.escidoc.admintool.app.AdminToolApplication;
import de.escidoc.admintool.view.ViewConstants;
import de.escidoc.core.resources.aa.useraccount.UserAccount;

@SuppressWarnings("serial")
public class UserLabView extends SplitPanel {

    private final AdminToolApplication app;

    private final UserLabListView userLabList;

    private final UserLabEditView userLabEditView;

    public UserLabView(final AdminToolApplication app,
        final UserLabListView userLabList, final UserLabEditView userLabEditView) {
        this.app = app;
        this.userLabList = userLabList;
        this.userLabEditView = userLabEditView;
        buildUI();
    }

    private void buildUI() {
        setSplitPosition(ViewConstants.SPLIT_POSITION_IN_PERCENT);
        setOrientation(ORIENTATION_HORIZONTAL);
        setFirstComponent(userLabList);
    }

    public UserLabListView getUserList() {
        return userLabList;
    }

    public Item getSelectedItem() {
        return userLabList.getItem(userLabList.getValue());
    }

    public UserLabView showAddView() {
        setSecondComponent(app.newUserLabAddView());
        return this;
    }

    public void showEditView(final Item item) {
        setSecondComponent(userLabEditView);
        userLabEditView.setSelected(item);
    }

    public void remove(final UserAccount deletedUser) {
        userLabList.remove(deletedUser);
        showAddView();
    }

}