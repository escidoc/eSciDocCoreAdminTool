package de.escidoc.admintool.view.orgunit;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Button.ClickEvent;

import de.escidoc.admintool.app.AdminToolApplication;
import de.escidoc.admintool.app.PropertyId;
import de.escidoc.admintool.domain.MetadataExtractor;
import de.escidoc.admintool.domain.OrgUnitFactory;
import de.escidoc.admintool.service.OrgUnitService;
import de.escidoc.admintool.view.ResourceRefDisplay;
import de.escidoc.admintool.view.ViewConstants;
import de.escidoc.core.client.exceptions.EscidocException;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.client.exceptions.TransportException;
import de.escidoc.core.resources.oum.OrganizationalUnit;
import de.escidoc.vaadin.utilities.Converter;
import de.escidoc.vaadin.utilities.LayoutHelper;

public class OrgUnitEditView extends AbstractOrgUnitView {
    private static final long serialVersionUID = -1488130998058019932L;

    private static final Logger log =
        LoggerFactory.getLogger(OrgUnitEditView.class);

    private final Button save = new Button("Save", this);

    private final Button cancel = new Button("Cancel", this);

    private final Button edit = new Button("Edit", this);

    private final HorizontalLayout footer = new HorizontalLayout();

    private final Label objIdField = new Label();

    private final Label modifiedOn = new Label();

    private final Label modifiedBy = new Label();

    private final Label createdOn = new Label();

    private final Label createdBy = new Label();

    private final Label publicStatus = new Label();

    private final Label publicStatusComment = new Label();

    private Item item;

    private final AdminToolApplication app;

    private OrgUnitService service = null;

    // private final Map<String, OrganizationalUnit> orgUnitById;
    private final int HEIGHT = 15;

    public OrgUnitEditView(final AdminToolApplication app,
        final OrgUnitService service) throws EscidocException,
        InternalClientException, TransportException,
        UnsupportedOperationException {
        super(app, service);
        assert service != null : "Service must not be null.";
        assert app != null : "Aervice must not be null.";
        this.app = app;
        this.service = service;
        middleInit();
        postInit();

    }

    private void middleInit() {
        form.addComponent(LayoutHelper.create(ViewConstants.OBJECT_ID_LABEL,
            objIdField, labelWidth, false));

        form.addComponent(LayoutHelper.create("Modified", "by", modifiedOn,
            modifiedBy, labelWidth, HEIGHT, false));

        form.addComponent(LayoutHelper.create("Created", "by", createdOn,
            createdBy, labelWidth, HEIGHT, false));
        form.addComponent(LayoutHelper.create("Status", publicStatus,
            labelWidth, false));

        if (!publicStatusComment.getValue().equals("")) {
            form.addComponent(LayoutHelper.create("Status Comment",
                publicStatusComment, labelWidth, false));
        }
    }

    private OrganizationalUnit update() {
        titleField.setComponentError(null);
        descriptionField.setComponentError(null);

        final Set<String> parents = getSelectedParents();
        final Set<String> predecessors = null;
        OrganizationalUnit updatedOrgUnit = null;
        try {
            final OrganizationalUnit toBeUpdate =
                service.find((String) objIdField.getValue());
            updatedOrgUnit =
                new OrgUnitFactory()
                    .update(toBeUpdate, (String) titleField.getValue(),
                        (String) descriptionField.getValue()).alternative(
                        (String) alternativeField.getValue()).identifier(
                        (String) identifierField.getValue()).orgType(
                        (String) orgTypeField.getValue()).country(
                        (String) countryField.getValue()).city(
                        (String) cityField.getValue()).coordinates(
                        (String) coordinatesField.getValue()).parents(parents)
                    .build();
            service.update(updatedOrgUnit);
            // commit();
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

        return updatedOrgUnit;
    }

    private Set<String> getSelectedParents() {
        if (parentList.getContainerDataSource() == null
            || parentList.getContainerDataSource().getItemIds() == null
            || parentList.getContainerDataSource().getItemIds().size() == 0
            || !parentList
                .getContainerDataSource().getItemIds().iterator().hasNext()) {
            return Collections.emptySet();
        }

        final ResourceRefDisplay parentRef =
            (ResourceRefDisplay) parentList
                .getContainerDataSource().getItemIds().iterator().next();
        final Set<String> parents = new HashSet<String>() {

            {
                add(parentRef.getObjectId());
            }
        };

        return parents;
    }

    private String getSelectedOrgUnitId() {
        final String objid =
            (String) item.getItemProperty(PropertyId.OBJECT_ID).getValue();
        return objid;
    }

    public void setSelected(final Item item) {
        this.item = item;
        if (item != null) {
            titleField.setPropertyDataSource(item
                .getItemProperty(PropertyId.NAME));

            descriptionField.setPropertyDataSource(item
                .getItemProperty(PropertyId.DESCRIPTION));

            objIdField.setPropertyDataSource(item
                .getItemProperty(PropertyId.OBJECT_ID));

            modifiedOn.setCaption(Converter
                .dateTimeToString((org.joda.time.DateTime) item
                    .getItemProperty(PropertyId.LAST_MODIFICATION_DATE)
                    .getValue()));

            modifiedBy.setPropertyDataSource(item
                .getItemProperty(PropertyId.MODIFIED_BY));

            createdOn.setCaption(Converter
                .dateTimeToString((org.joda.time.DateTime) item
                    .getItemProperty(PropertyId.CREATED_ON).getValue()));

            createdBy.setPropertyDataSource(item
                .getItemProperty(PropertyId.CREATED_BY));

            publicStatus.setPropertyDataSource(item
                .getItemProperty(PropertyId.PUBLIC_STATUS));

            publicStatusComment.setPropertyDataSource(item
                .getItemProperty(PropertyId.PUBLIC_STATUS_COMMENT));

            parentList.setPropertyDataSource(item
                .getItemProperty(PropertyId.PARENTS));
            final OrganizationalUnit orgUnit =
                service.find((String) item
                    .getItemProperty(PropertyId.OBJECT_ID).getValue());

            final MetadataExtractor metadataExtractor =
                new MetadataExtractor(orgUnit);
            final String alternative =
                metadataExtractor.get("dcterms:alternative");
            final String identifier = metadataExtractor.get("dc:identifier");
            final String orgType =
                metadataExtractor.get("eterms:organization-type");
            final String country = metadataExtractor.get("eterms:country");
            final String city = metadataExtractor.get("eterms:city");
            final String coordinate = metadataExtractor.get("kml:coordinates");
            final String startDate = metadataExtractor.get("eterms:start-date");
            final String endDate = metadataExtractor.get("eterms:end-date");
            alternativeField.setValue(alternative);
            identifierField.setValue(identifier);
            orgTypeField.setValue(orgType);
            countryField.setValue(country);
            cityField.setValue(city);
            coordinatesField.setValue(coordinate);
        }
    }

    @Override
    protected String getViewCaption() {
        return "Edit " + ViewConstants.ORGANIZATION_UNITS_LABEL;
    }

    @Override
    protected void onAddPredecessorClicked() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void saveClicked(final ClickEvent event) {
        update();
    }

    // TODO: discard changes
    @Override
    protected void cancelClicked(final ClickEvent event) {
        super.app.showOrganizationalUnitView();
    }

    @Override
    protected Component addToolbar() {
        final OrgUnitToolbar toolbar = new OrgUnitToolbar(app, this);
        return toolbar;
    }

    public void deleteOrgUnit() {
        final OrganizationalUnit selected =
            service.find(getSelectedOrgUnitId());
        try {
            service.delete(selected);
            orgUnitList.removeOrgUnit(selected);
            app.showOrganizationalUnitView();
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

    }

}

// TODO code duplication @see OrgUnitAddForm.java
// final String country = (String) getField(COUNTRY_ID).getValue();
// final String city = (String) getField(CITY_ID).getValue();
// // final Set<String> parents =
// // (Set<String>) this.getField(PARENTS_ID).getValue();
//
// final TwinColSelect twinColSelect =
// (TwinColSelect) getField(PARENTS_ID);
// final Set<String> parents = (Set<String>) twinColSelect.getValue();
//
// System.out.println("Update parents to: ");
// for (final String parent : parents) {
// System.out.println(parent);
// }
//
// final OrganizationalUnit updatedOrgUnit =
// new OrgUnitFactory()
// .update(toBeUpdate,
// (String) getField(ViewConstants.TITLE_ID).getValue(),
// (String) getField(ViewConstants.DESCRIPTION_ID).getValue())
// .alternative(
// (String) getField(ViewConstants.ALTERNATIVE_ID).getValue())
// .identifier(
// (String) getField(ViewConstants.IDENTIFIER_ID).getValue())
// .orgType((String) getField(ORG_TYPE_ID).getValue())
// .country(country)
// .city(city)
// .coordinates(
// (String) getField(ViewConstants.COORDINATES_ID).getValue())
// .parents(parents).build();