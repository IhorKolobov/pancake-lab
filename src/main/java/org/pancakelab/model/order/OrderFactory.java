package org.pancakelab.model.order;

import java.util.UUID;

import static org.pancakelab.common.ExceptionMessages.ORDER_FACTORY_SHOULD_NOT_BE_INSTANTIATED;

public final class OrderFactory {

    private OrderFactory() {
        throw new UnsupportedOperationException(ORDER_FACTORY_SHOULD_NOT_BE_INSTANTIATED);
    }

    public static Order create(int building, int room) {
        return new Order(UUID.randomUUID(), building, room, OrderStatus.CREATED);
    }
}