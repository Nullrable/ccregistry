package org.lsd.ccregistery.exception;

/**
 * @author nhsoft.lsd
 */
public class CcRegistryException extends RuntimeException{

    public CcRegistryException() {
    }

    public CcRegistryException(final String message) {
        super(message);
    }

    public CcRegistryException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
