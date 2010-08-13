package de.escidoc.admintool.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.escidoc.core.client.OrganizationalUnitHandlerClient;
import de.escidoc.core.client.exceptions.EscidocException;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.client.exceptions.TransportException;
import de.escidoc.core.resources.common.Filter;
import de.escidoc.core.resources.common.TaskParam;
import de.escidoc.core.resources.oum.OrganizationalUnit;
import de.escidoc.core.resources.oum.Predecessor;

public class OrgUnitService {
    private static final String ESCIDOC_SERVICE_ROOT_URI =
        "http://localhost:8080";

    // TODO re-factor Ou- and ContextService: @See ContextService.java
    private final OrganizationalUnitHandlerClient client;

    public OrgUnitService(final OrganizationalUnitHandlerClient client) {
        this.client = client;
    }

    public OrgUnitService(final String handle) throws InternalClientException {
        client = createOuClient(handle);
    }

    private OrganizationalUnitHandlerClient createOuClient(final String handle)
        throws InternalClientException {
        final OrganizationalUnitHandlerClient client =
            new OrganizationalUnitHandlerClient();
        client.setHandle(handle);
        client.setServiceAddress(ESCIDOC_SERVICE_ROOT_URI);
        return client;
    }

    private final Map<String, OrganizationalUnit> orgUnitById =
        new ConcurrentHashMap<String, OrganizationalUnit>();

    public Map<String, OrganizationalUnit> getOrgUnitById() {
        return orgUnitById;
    }

    private Collection<OrganizationalUnit> organizationalUnits;

    public Collection<OrganizationalUnit> all() throws EscidocException,
        InternalClientException, TransportException {

        organizationalUnits =
            client
                .retrieveOrganizationalUnits(emptyFilter())
                .getOrganizationalUnits();

        for (final OrganizationalUnit orgUnit : organizationalUnits) {
            orgUnitById.put(orgUnit.getObjid(), orgUnit);
        }

        return organizationalUnits;
    }

    // FIXME duplicate method in ContextService
    private TaskParam emptyFilter() {
        final Collection<Filter> filters = TaskParam.filtersFactory();
        filters.add(getFilter("", "", null));

        final TaskParam filterParam = new TaskParam();
        filterParam.setFilters(filters);
        return filterParam;
    }

    // FIXME duplicate method in ContextService
    private Filter getFilter(
        final String name, final String value, final Collection<String> ids) {

        final Filter filter = new Filter();
        filter.setName(name);
        filter.setValue(value);
        filter.setIds(ids);
        return filter;
    }

    public OrganizationalUnit create(final OrganizationalUnit ou)
        throws EscidocException, InternalClientException, TransportException {
        final OrganizationalUnit createdOrgUnit = client.create(ou);
        System.out
            .println("Succesfully stored a new Organizational Unit with the Object ID: "
                + createdOrgUnit.getObjid());

        assert createdOrgUnit != null : "Got null reference from the server.";
        assert createdOrgUnit.getObjid() != null : "ObjectID can not be null.";
        assert orgUnitById != null : "orgUnitById is null";
        final int sizeBefore = orgUnitById.size();
        orgUnitById.put(createdOrgUnit.getObjid(), createdOrgUnit);
        final int sizeAfter = orgUnitById.size();
        assert sizeAfter > sizeBefore : "user account is not added to map.";

        return createdOrgUnit;
    }

    public OrganizationalUnit retrieve(final String objid)
        throws EscidocException, InternalClientException, TransportException {
        return client.retrieve(objid);
    }

    public void update(final OrganizationalUnit ou) throws EscidocException,
        InternalClientException, TransportException {
        final OrganizationalUnit update = client.update(ou);
    }

    public void delete(final OrganizationalUnit ou) throws EscidocException,
        InternalClientException, TransportException {
        client.delete(ou.getObjid());
    }

    public Collection<OrganizationalUnit> getOrganizationalUnits()
        throws EscidocException, InternalClientException, TransportException {
        if (organizationalUnits == null) {
            return all();
        }
        return organizationalUnits;
    }

    public Collection<String> getPredecessorsObjectId(
        final OrganizationalUnit ou) {
        assert ou != null : "Org Unit can not be null";
        if (ou.getPredecessors() == null) {
            return Collections.emptyList();
        }
        return getPredecessorsByObjectId(ou).keySet();
    }

    public Map<String, Predecessor> getPredecessorsByObjectId(
        final OrganizationalUnit ou) {

        final Map<String, Predecessor> predecessorByObjectId =
            new ConcurrentHashMap<String, Predecessor>();

        final Iterator<Predecessor> iterator = ou.getPredecessors().iterator();

        assert iterator != null : "iterator can not be null.";
        while (iterator.hasNext()) {
            final Predecessor predecessor = iterator.next();
            predecessorByObjectId.put(predecessor.getObjid(), predecessor);
        }
        return predecessorByObjectId;
    }

    public Collection<OrganizationalUnit> getOrgUnitsByIds(
        final List<String> objectIds) {
        if (objectIds == null || objectIds.size() == 0) {
            return Collections.emptyList();
        }

        final List<OrganizationalUnit> collected =
            new ArrayList<OrganizationalUnit>(objectIds.size());
        for (final String objectId : objectIds) {
            collected.add(orgUnitById.get(objectId));
        }
        return collected;
    }
}