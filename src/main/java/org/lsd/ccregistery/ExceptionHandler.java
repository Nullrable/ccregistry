package org.lsd.ccregistery;

import org.lsd.ccregistery.exception.CcRegistryException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author nhsoft.lsd
 */
@RestControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(value = CcRegistryException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public final String handlerException(final CcRegistryException ex) {
        return ex.getMessage();
    }
}
