package org.pancakelab.model.order;

import java.util.UUID;

public final class Order {

    private final UUID id;
    private final int building;
    private final int room;
    private final OrderStatus status;

    Order(UUID id, int building, int room, OrderStatus status) {
        this.id = id;
        this.building = building;
        this.room = room;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public int getBuilding() {
        return building;
    }

    public int getRoom() {
        return room;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Order withStatus(OrderStatus newStatus) {
        return new Order(id, building, room, newStatus);
    }
}
