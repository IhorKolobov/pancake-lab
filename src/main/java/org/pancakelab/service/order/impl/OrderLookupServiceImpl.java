package org.pancakelab.service.order.impl;

import org.pancakelab.exception.OrderStoreNotThreadSafeException;
import org.pancakelab.model.order.Order;
import org.pancakelab.service.order.OrderLookupService;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public final class OrderLookupServiceImpl implements OrderLookupService {

    private final Map<UUID, Order> orderStore;

    public OrderLookupServiceImpl(Map<UUID, Order> orderStore) {
        if (!(orderStore instanceof ConcurrentMap)) {
            throw new OrderStoreNotThreadSafeException();
        }
        this.orderStore = orderStore;
    }

    @Override
    public Optional<Order> findOrder(UUID orderId) {
        return Optional.ofNullable(orderStore.get(orderId));
    }
}

