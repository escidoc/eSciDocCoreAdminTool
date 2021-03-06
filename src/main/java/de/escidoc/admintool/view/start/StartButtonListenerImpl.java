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
package de.escidoc.admintool.view.start;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.AbstractField;

import de.escidoc.admintool.app.AdminToolApplication;

public class StartButtonListenerImpl extends AbstractStartButtonListener {

    private static final Logger LOG = LoggerFactory.getLogger(StartButtonListenerImpl.class);

    private static final long serialVersionUID = 2949659635673188343L;

    public StartButtonListenerImpl(final AbstractField escidocComboBox, final AdminToolApplication app) {
        super(escidocComboBox, app);
    }

    @Override
    protected void redirectToMainView() {
        final String redirectUri = super.getApplication().getURL() + "?escidocurl=" + getEscidocUri();
        LOG.info("redirect to: " + redirectUri);
        redirectTo(redirectUri);
    }

    private void redirectTo(final String url) {
        super.getMainWindow().open(new ExternalResource(url));
    }
}