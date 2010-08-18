package de.escidoc.admintool.messages;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Messages {
    private static final Logger log = LoggerFactory.getLogger(Messages.class);

    private static final String BUNDLE_NAME =
        "de.escidoc.admintool.messages.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
        .getBundle(BUNDLE_NAME);

    private Messages() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        }
        catch (MissingResourceException e) {
            log.error("Label for " + key + " could not be found", e);
            return '!' + key + '!';
        }
    }
}