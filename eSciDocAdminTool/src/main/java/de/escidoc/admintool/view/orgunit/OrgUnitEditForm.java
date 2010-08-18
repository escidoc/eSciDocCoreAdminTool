package de.escidoc.admintool.view.orgunit;

import static de.escidoc.admintool.view.ViewConstants.CITY_ID;
import static de.escidoc.admintool.view.ViewConstants.COUNTRY_ID;
import static de.escidoc.admintool.view.ViewConstants.ORG_TYPE_ID;
import static de.escidoc.admintool.view.ViewConstants.PARENTS_ID;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.vaadin.data.Item;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

import de.escidoc.admintool.app.AdminToolApplication;
import de.escidoc.admintool.domain.OrgUnitFactory;
import de.escidoc.admintool.service.OrgUnitService;
import de.escidoc.admintool.view.ViewConstants;
import de.escidoc.core.client.exceptions.EscidocException;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.client.exceptions.TransportException;
import de.escidoc.core.resources.oum.OrganizationalUnit;
import de.escidoc.vaadin.utilities.Converter;

@SuppressWarnings("serial")
public class OrgUnitEditForm extends Form implements ClickListener {
    private static final Logger log =
        LoggerFactory.getLogger(OrgUnitEditForm.class);

    private final Button save = new Button("Save", (ClickListener) this);

    private final Button cancel = new Button("Cancel", (ClickListener) this);

    private final Button edit = new Button("Edit", (ClickListener) this);

    private final HorizontalLayout footer = new HorizontalLayout();

    private final AbstractField nameField = new TextField();

    private final Label objIdField = new Label("OBJ");

    private final Label modifiedOn = new Label();

    private final Label modifiedBy = new Label();

    private final Label createdOn = new Label();

    private final Label createdBy = new Label();

    private Item item;

    private final AdminToolApplication app;

    private boolean newContactMode = false;

    private final OrganizationalUnit newOrganizationalUnit = null;

    private OrgUnitService service = null;

    private final Map<String, OrganizationalUnit> orgUnitById;

    public OrgUnitEditForm(final AdminToolApplication app,
        final OrgUnitService service) throws EscidocException,
        InternalClientException, TransportException,
        UnsupportedOperationException {
        assert service != null : "Service must not be null.";
        assert app != null : "Aervice must not be null.";

        this.app = app;
        this.service = service;

        buildUI();
        setWriteThrough(false);

        orgUnitById = service.getOrgUnitById();

        for (final OrganizationalUnit orgUnit : service
            .getOrganizationalUnits()) {
            parents.addItem(orgUnit.getObjid());
        }
    }

    // TODO duplicate, create an abstract class
    private void buildUI() {
        addFooter();

        // setWriteThrough(false);
        // setInvalidCommitted(false);
        // // TODO test if it works.
        // setImmediate(true);
        // setValidationVisible(true);
    }

    private final TwinColSelect parents =
        new TwinColSelect(ViewConstants.PARENTS_LABEL);

    private void addFooter() {
        footer.setSpacing(true);
        footer.addComponent(save);
        footer.addComponent(cancel);
        footer.addComponent(edit);
        footer.setVisible(false);
        setFooter(footer);
    }

    public void buttonClick(final ClickEvent event) {
        final Button source = event.getButton();

        if (source == save) {
            /* If the given input is not valid there is no point in continuing */
            if (!isValid()) {
                return;
            }

            // update new data to the repository
            // TODO the GUI is blocked if update takes too long.
            try {
                final OrganizationalUnit updatedOU = update();
                service.update(updatedOU);
                commit();
            }
            catch (final EscidocException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (final InternalClientException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (final TransportException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (final ParserConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (final SAXException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            setReadOnly(true);
        }
        else if (source == cancel) {
            if (newContactMode) {
                newContactMode = false;
                /* Clear the form and make it invisible */
                this.setItemDataSource(null);
            }
            else {
                discard();
            }
            setReadOnly(true);
        }
        else if (source == edit) {

            setReadOnly(false);

        }
    }

    private OrganizationalUnit update() throws ParserConfigurationException,
        SAXException, IOException, EscidocException, InternalClientException,
        TransportException {
        // TODO slow operation, refactor this. We already have collection of org
        // units
        final OrganizationalUnit toBeUpdate =
            service.retrieve(getSelectedOrgUnitId());

        // TODO code duplication @see OrgUnitAddForm.java
        final String country = (String) getField(COUNTRY_ID).getValue();
        final String city = (String) getField(CITY_ID).getValue();
        // final Set<String> parents =
        // (Set<String>) this.getField(PARENTS_ID).getValue();

        final TwinColSelect twinColSelect =
            (TwinColSelect) getField(PARENTS_ID);
        final Set<String> parents = (Set<String>) twinColSelect.getValue();

        System.out.println("Update parents to: ");
        for (final String parent : parents) {
            System.out.println(parent);
        }

        final OrganizationalUnit updatedOrgUnit =
            new OrgUnitFactory()
                .update(toBeUpdate,
                    (String) getField(ViewConstants.TITLE_ID).getValue(),
                    (String) getField(ViewConstants.DESCRIPTION_ID).getValue())
                .alternative(
                    (String) getField(ViewConstants.ALTERNATIVE_ID).getValue())
                .identifier(
                    (String) getField(ViewConstants.IDENTIFIER_ID).getValue())
                .orgType((String) getField(ORG_TYPE_ID).getValue()).country(
                    country).city(city).coordinates(
                    (String) getField(ViewConstants.COORDINATES_ID).getValue())
                .parents(parents).build();

        return updatedOrgUnit;
    }

    private String getSelectedOrgUnitId() {
        final String objid =
            (String) getItemDataSource().getItemProperty(
                ViewConstants.OBJECT_ID).getValue();
        return objid;
    }

    // @Override
    // public void setItemDataSource(final Item newDataSource) {
    // newContactMode = false;
    // if (newDataSource != null) {
    // super.setItemDataSource(newDataSource);
    //
    // setReadOnly(true);
    // getFooter().setVisible(true);
    // }
    // else {
    // super.setItemDataSource(null);
    // getFooter().setVisible(false);
    // }
    // }

    @Override
    public void setReadOnly(final boolean readOnly) {
        super.setReadOnly(readOnly);
        // getField(ViewConstants.OBJECT_ID).setReadOnly(true);
        // getField(CREATED_ON_ID).setReadOnly(true);
        // getField(ViewConstants.CREATED_BY_ID).setReadOnly(true);
        // getField(ViewConstants.MODIFIED_ON_ID).setReadOnly(true);
        // getField(ViewConstants.MODIFIED_BY_ID).setReadOnly(true);

        save.setVisible(!readOnly);
        cancel.setVisible(!readOnly);
        edit.setVisible(readOnly);
    }

    public void setSelected(final Item item) {
        this.item = item;
        if (item != null) {
            nameField.setPropertyDataSource(item
                .getItemProperty(ViewConstants.NAME_ID));
            objIdField.setPropertyDataSource(item.getItemProperty("objid"));
            modifiedOn.setCaption(Converter
                .dateTimeToString((org.joda.time.DateTime) item
                    .getItemProperty("lastModificationDate").getValue()));
            modifiedBy.setPropertyDataSource(item
                .getItemProperty("properties.modifiedBy.objid"));
            createdOn.setCaption(Converter
                .dateTimeToString((org.joda.time.DateTime) item
                    .getItemProperty("properties.creationDate").getValue()));
            createdBy.setPropertyDataSource(item
                .getItemProperty("properties.createdBy.objid"));
        }
    }
}