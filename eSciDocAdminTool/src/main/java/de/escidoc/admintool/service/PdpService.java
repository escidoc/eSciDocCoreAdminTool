package de.escidoc.admintool.service;

import java.net.URISyntaxException;

import de.escidoc.core.client.exceptions.EscidocClientException;

public interface PdpService {
    void loginWith(final String token);

    PdpService isAction(String actionId) throws URISyntaxException;

    PdpService forResource(String resourceId) throws URISyntaxException;

    PdpService forUser(String userId) throws URISyntaxException;

    boolean permitted() throws EscidocClientException;

    boolean denied() throws EscidocClientException;

}