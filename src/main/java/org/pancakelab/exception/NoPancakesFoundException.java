package org.pancakelab.exception;

import org.pancakelab.common.ExceptionMessages;

import java.util.UUID;

public class NoPancakesFoundException extends RuntimeException {

    public NoPancakesFoundException(UUID orderId) {
        super(ExceptionMessages.NO_PANCAKES_FOUND.formatted(orderId));
    }
}
