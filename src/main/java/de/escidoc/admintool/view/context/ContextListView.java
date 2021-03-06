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

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.vaadin.data.util.POJOContainer;
import com.vaadin.data.util.POJOItem;
import com.vaadin.ui.Table;

import de.escidoc.admintool.app.AdminToolApplication;
import de.escidoc.admintool.app.AppConstants;
import de.escidoc.admintool.app.PropertyId;
import de.escidoc.admintool.service.internal.ContextService;
import de.escidoc.admintool.view.EscidocPagedTable;
import de.escidoc.admintool.view.ViewConstants;
import de.escidoc.admintool.view.context.listener.ContextSelectListener;
import de.escidoc.admintool.view.util.dialog.ErrorDialog;
import de.escidoc.core.client.exceptions.EscidocClientException;
import de.escidoc.core.client.exceptions.EscidocException;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.client.exceptions.TransportException;
import de.escidoc.core.resources.om.context.Context;

@SuppressWarnings("serial")
public class ContextListView extends EscidocPagedTable {

    private final Logger LOG = LoggerFactory.getLogger(ContextListView.class);

    private final AdminToolApplication app;

    private final ContextService contextService;

    private POJOContainer<Context> contextContainer;

    private Collection<Context> allContexts;

    public ContextListView(final AdminToolApplication app, final ContextService contextService)
        throws EscidocException, InternalClientException, TransportException {
        checkForNull(app, contextService);

        this.app = app;
        this.contextService = contextService;
        buildView();
        findAllContexts();
        bindDataSource();
        setPageLength(50);
    }

    private void checkForNull(final AdminToolApplication app, final ContextService contextService) {
        Preconditions.checkNotNull(app, " app can not be null: %s", app);
        Preconditions.checkNotNull(contextService, " contextService can not be null: %s", contextService);
    }

    private void buildView() {
        setSizeFull();
        setSelectable(true);
        setImmediate(true);
        addListener(new ContextSelectListener(app));
        setNullSelectionAllowed(false);
    }

    private void bindDataSource() {
        if (isContextExist()) {
            initContextContainer();
        }
    }

    private boolean isContextExist() {
        return allContexts != null && !allContexts.isEmpty();
    }

    private void initContextContainer() {
        initContainer();
        setContainerDataSource(contextContainer);
        sortByModificationDate();
        setVisibleColumns(new Object[] { PropertyId.NAME });
        setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
    }

    private void sortByModificationDate() {
        sort(new Object[] { PropertyId.LAST_MODIFICATION_DATE }, new boolean[] { false });
    }

    private void initContainer() {
        if (allContexts.isEmpty()) {
            contextContainer = new POJOContainer<Context>(Context.class, AppConstants.CONTEXT_PROPERTY_NAMES);
        }
        else {
            contextContainer = new POJOContainer<Context>(allContexts, AppConstants.CONTEXT_PROPERTY_NAMES);
        }
    }

    private void findAllContexts() {
        try {
            allContexts = contextService.findAll();
        }
        catch (final EscidocClientException e) {
            app.getMainWindow().addWindow(
                new ErrorDialog(app.getMainWindow(), "Error", "An unexpected error occured! See LOG for details."));
            LOG.error("An unexpected error occured! See LOG for details.", e);
        }
    }

    public POJOItem<Context> addContext(final Context context) {
        assert context != null : "context must not be null.";
        if (contextContainer == null) {
            findAllContexts();
            initContextContainer();
        }
        final POJOItem<Context> addedItem = contextContainer.addItem(context);
        sort();
        return addedItem;
    }

    @Override
    public void sort() {
        sort(new Object[] { ViewConstants.MODIFIED_ON_ID }, new boolean[] { false });
    }

    public void removeContext(final Context selected) {
        Preconditions.checkNotNull(selected, "selected is null: %s", selected);
        assert contextContainer.containsId(selected) : "Context not in the list view";

        @SuppressWarnings("boxing")
        final Object itemId = contextContainer.removeItem(selected);

        assert itemId != null : "Removing context to the list failed.";
    }

    public void updateContext(final Context oldContext, final Context newContext) {
        removeContext(oldContext);
        addContext(newContext);
        sort(new Object[] { ViewConstants.MODIFIED_ON_ID }, new boolean[] { false });
        setValue(newContext);
    }
}