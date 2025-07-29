package org.pancakelab.exception;

import java.util.UUID;

import static org.pancakelab.common.ExceptionMessages.ORDER_NOT_READY_FOR_DELIVERY;

public class OrderNotReadyForDeliveryException extends RuntimeException {

    public OrderNotReadyForDeliveryException(UUID orderId) {
        super(ORDER_NOT_READY_FOR_DELIVERY.formatted(orderId));
    }
}
