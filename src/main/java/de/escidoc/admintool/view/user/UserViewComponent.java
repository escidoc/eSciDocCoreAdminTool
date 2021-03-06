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
package de.escidoc.admintool.view.user;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.vaadin.data.Item;

import de.escidoc.admintool.app.AdminToolApplication;
import de.escidoc.admintool.domain.PdpRequest;
import de.escidoc.admintool.service.internal.OrgUnitServiceLab;
import de.escidoc.admintool.service.internal.UserService;
import de.escidoc.admintool.view.resource.OrgUnitTreeView;
import de.escidoc.core.resources.aa.useraccount.UserAccount;

public class UserViewComponent {

    private static final Logger LOG = LoggerFactory.getLogger(UserViewComponent.class);

    private UserEditForm userEditForm;

    private UserView userView;

    private UserEditView userEditView;

    private final AdminToolApplication app;

    private final UserService userService;

    private UserListView listView;

    private UserEditForm editForm;

    private final OrgUnitServiceLab orgUnitService;

    private final OrgUnitTreeView orgUnitTreeView;

    private final PdpRequest pdpRequest;

    public UserViewComponent(final AdminToolApplication app, final UserService userService,
        final OrgUnitServiceLab orgUnitService, final OrgUnitTreeView orgUnitTreeView, final PdpRequest pdpRequest) {
        Preconditions.checkNotNull(app, "AdminToolApplication is null.");
        Preconditions.checkNotNull(userService, "UserService is null.");
        Preconditions.checkNotNull(orgUnitService, "orgUnitService is null: %s", orgUnitService);
        Preconditions.checkNotNull(pdpRequest, "pdpRequest is null: %s", pdpRequest);
        this.app = app;
        this.userService = userService;
        this.orgUnitService = orgUnitService;
        this.orgUnitTreeView = orgUnitTreeView;
        this.pdpRequest = pdpRequest;
    }

    public void init() {
        createListView();
        createEditForm();
        createUserView();
        setUserView(userView);
    }

    private void createUserView() {
        userView = new UserView(app, listView, getUserEditView());
        userView.init();
    }

    private void createEditForm() {
        editForm = new UserEditForm(app, userService, orgUnitService, orgUnitTreeView, pdpRequest);
        editForm.init();
        setUserEditForm(editForm);
        setUserEditView(new UserEditView(getUserEditForm()));
    }

    private void createListView() {
        listView = new UserListView(app, userService);
    }

    public UserView getUserView() {
        return userView;
    }

    public void showFirstItemInEditView() {
        if (listView.firstItemId() == null) {
            return;
        }
        listView.select(listView.firstItemId());
        userView.showEditView(getFirstItem());
    }

    private Item getFirstItem() {
        return listView.getContainerDataSource().getItem(listView.firstItemId());
    }

    /**
     * @param userView
     *            the userView to set
     */
    public void setUserView(final UserView userView) {
        this.userView = userView;
    }

    public void setUserEditForm(final UserEditForm userEditForm) {
        this.userEditForm = userEditForm;
    }

    public UserEditForm getUserEditForm() {
        return userEditForm;
    }

    public void setUserEditView(final UserEditView userEditView) {
        this.userEditView = userEditView;
    }

    /**
     * @return the userEditView
     */
    public UserEditView getUserEditView() {
        return userEditView;
    }

    public void showAddView() {
        userView.showAddView();
    }

    // FIXME: this is a hack, an won't perform well.
    // To improve: Implement equals() and hashCode in DTO or Domain Object i.e.
    // UserAccount or MyUserAccount
    public void showUserInEditView(final UserAccount user) {
        for (final UserAccount userAccount : getAllUserAccountsFromContainer()) {
            if (hasEqualsId(user, userAccount)) {
                selectFoundUserInListView(userAccount);
                showUserFoundUserInEditView(userAccount);
            }
        }
    }

    private void selectFoundUserInListView(final UserAccount userAccount) {
        listView.select(userAccount);
    }

    private void showUserFoundUserInEditView(final UserAccount userAccount) {
        userView.showEditView(listView.getContainerDataSource().getItem(userAccount));
    }

    @SuppressWarnings("unchecked")
    private Collection<UserAccount> getAllUserAccountsFromContainer() {
        return (Collection<UserAccount>) listView.getContainerDataSource().getItemIds();
    }

    private boolean hasEqualsId(final UserAccount user, final UserAccount userAccount) {
        return user.getObjid().equalsIgnoreCase(userAccount.getObjid());
    }
}