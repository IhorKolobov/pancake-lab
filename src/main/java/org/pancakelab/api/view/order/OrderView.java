package org.pancakelab.api.view.order;

import org.pancakelab.model.order.OrderStatus;

import java.util.UUID;

public interface OrderView {

    UUID getId();

    int getBuilding();

    int getRoom();

    OrderStatus getStatus();
}
