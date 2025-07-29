package org.pancakelab.exception;

import static org.pancakelab.common.ExceptionMessages.THREAD_SAFE_ERROR_MSG;

public class OrderStoreNotThreadSafeException extends IllegalArgumentException {

    public OrderStoreNotThreadSafeException() {
        super(THREAD_SAFE_ERROR_MSG);
    }
}

