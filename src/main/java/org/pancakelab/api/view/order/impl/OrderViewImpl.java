package org.pancakelab.api.view.order.impl;

import org.pancakelab.api.view.order.OrderView;
import org.pancakelab.model.order.Order;
import org.pancakelab.model.order.OrderStatus;

import java.util.UUID;

public class OrderViewImpl implements OrderView {

    private final UUID id;
    private final int building;
    private final int room;
    private final OrderStatus status;

    public OrderViewImpl(Order order) {
        this.id = order.getId();
        this.building = order.getBuilding();
        this.room = order.getRoom();
        this.status = order.getStatus();
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public int getBuilding() {
        return building;
    }

    @Override
    public int getRoom() {
        return room;
    }

    @Override
    public OrderStatus getStatus() {
        return status;
    }
}
