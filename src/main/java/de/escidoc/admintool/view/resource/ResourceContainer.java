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

import java.util.Collection;

import com.vaadin.data.Container;
import com.vaadin.data.Item;

import de.escidoc.core.resources.Resource;
import de.escidoc.core.resources.oum.OrganizationalUnit;

public interface ResourceContainer {

    void addChildren(Resource parent, Collection<OrganizationalUnit> children);

    void addChild(final Resource parent, final Resource child);

    int size();

    Container getContainer();

    void updateParent(OrganizationalUnit child, OrganizationalUnit parent);

    void removeParent(OrganizationalUnit child);

    void remove(Resource resource);

    void add(Resource created);

    Item firstResourceAsItem();

    Object firstResource();

    boolean isEmpty();

    Item getItem(Resource resource);

}
