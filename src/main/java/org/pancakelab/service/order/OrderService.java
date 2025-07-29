package org.pancakelab.service.order;


import org.pancakelab.api.view.order.OrderView;

import java.util.Optional;
import java.util.UUID;

public interface OrderService {

    UUID createOrder(int building, int room);

    void cancelOrder(UUID orderId);

    void completeOrder(UUID orderId);

    Optional<OrderView> findOrder(UUID orderId);

    void deleteOrder(UUID orderId);
}
