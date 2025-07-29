package org.pancakelab.exception;

import java.util.UUID;

import static org.pancakelab.common.ExceptionMessages.ORDER_NOT_FOUND;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(UUID id) {
        super(ORDER_NOT_FOUND.formatted(id));
    }
}
