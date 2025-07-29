package org.pancakelab.service.validation.impl;

import org.pancakelab.exception.InvalidOrderStateException;
import org.pancakelab.model.order.Order;
import org.pancakelab.model.order.OrderStatus;
import org.pancakelab.service.validation.OrderValidationService;

import static org.pancakelab.common.ExceptionMessages.ORDER_NOT_MODIFIABLE;
import static org.pancakelab.common.ExceptionMessages.ORDER_NOT_READY_FOR_PREPARATION;

public final class OrderValidationServiceImpl implements OrderValidationService {

    @Override
    public void ensureModifiable(Order order) {
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new InvalidOrderStateException(ORDER_NOT_MODIFIABLE);
        }
    }

    @Override
    public void ensureReadyForPreparation(Order order) {
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new InvalidOrderStateException(ORDER_NOT_READY_FOR_PREPARATION);
        }
    }
}
