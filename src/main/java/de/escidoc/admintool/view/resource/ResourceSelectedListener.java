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
package de.escidoc.admintool.view.resource;

import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;

import de.escidoc.admintool.app.PropertyId;

final class ResourceSelectedListener implements ItemClickListener {

    private static final long serialVersionUID = 7022982222058387053L;

    private String selectedParent;

    AddOrEditParentModalWindow addOrEditParentModalWindow;

    ResourceSelectedListener(final AddOrEditParentModalWindow addOrEditParentModalWindow) {
        this.addOrEditParentModalWindow = addOrEditParentModalWindow;
    }

    public void itemClick(final ItemClickEvent event) {
        final Item item = event.getItem();
        if (item == null) {
            return;
        }
        addOrEditParentModalWindow.setSelected(getSelectedParent(item));
    }

    private String getSelectedParent(final Item item) {
        final String selectedParent = (String) item.getItemProperty(PropertyId.OBJECT_ID).getValue();
        return selectedParent;
    }
}