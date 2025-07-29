package org.pancakelab.service.order;

import org.pancakelab.model.order.Order;

import java.util.Optional;
import java.util.UUID;

public interface OrderLookupService {

    Optional<Order> findOrder(UUID orderId);
}
