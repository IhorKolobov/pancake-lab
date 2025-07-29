package org.pancakelab.model.order;

import java.util.UUID;

public final class OrderFactory {

    private OrderFactory() {
        throw new UnsupportedOperationException("OrderFactory should not be instantiated.");
    }

    public static Order create(int building, int room) {
        return new Order(UUID.randomUUID(), building, room, OrderStatus.CREATED);
    }

    public static Order restore(UUID id, int building, int room, OrderStatus status) {
        return new Order(id, building, room, status);
    }
}