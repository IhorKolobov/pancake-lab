package org.pancakelab.service.validation;

import org.pancakelab.model.order.Order;

public interface OrderValidationService {

    void ensureModifiable(Order order);

    void ensureReadyForPreparation(Order order);
}
