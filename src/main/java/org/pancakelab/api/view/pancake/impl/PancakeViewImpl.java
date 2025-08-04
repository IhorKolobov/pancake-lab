package org.pancakelab.api.view.pancake.impl;

import org.pancakelab.api.view.pancake.PancakeView;
import org.pancakelab.model.pancake.Pancake;

import java.util.UUID;

public class PancakeViewImpl implements PancakeView {
    private final UUID id;
    private final UUID orderId;

    public PancakeViewImpl(Pancake pancake) {
        this.id = pancake.getId();
        this.orderId = pancake.getOrderId();
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public UUID getOrderId() {
        return orderId;
    }
}
