package com.realworld.webfluxfn.exception;

import org.apache.http.HttpStatus;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    private static final ErrorAttributeOptions INCLUDING_OPTIONS = ErrorAttributeOptions.of(
            ErrorAttributeOptions.Include.EXCEPTION,
//            ErrorAttributeOptions.Include.STACK_TRACE,
            ErrorAttributeOptions.Include.MESSAGE,
            ErrorAttributeOptions.Include.BINDING_ERRORS);

    @Override
    public Map<String, Object> getErrorAttributes(final ServerRequest request, final ErrorAttributeOptions options) {
        final Map<String, Object> map = super.getErrorAttributes(request, INCLUDING_OPTIONS);
        final String exception = (String) map.get("exception");
        switch(exception) {
            case "com.realworld.webfluxfn.exception.InvalidRequestException":
                map.put("status", HttpStatus.SC_UNPROCESSABLE_ENTITY);
                break;
        }
        return map;
    }
}
