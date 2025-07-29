package org.pancakelab.exception;

import java.util.UUID;

import static org.pancakelab.common.ExceptionMessages.PANCAKE_NOT_FOUND;

public class PancakeNotFoundException extends RuntimeException {

    public PancakeNotFoundException(UUID pancakeId) {
        super(PANCAKE_NOT_FOUND.formatted(pancakeId));
    }
}

