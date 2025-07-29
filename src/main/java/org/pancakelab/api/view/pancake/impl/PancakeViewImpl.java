package org.pancakelab.api.view.pancake.impl;

import org.pancakelab.api.view.pancake.PancakeView;
import org.pancakelab.model.pancake.Ingredient;
import org.pancakelab.model.pancake.Pancake;

import java.util.List;
import java.util.UUID;

public class PancakeViewImpl implements PancakeView {
    private final UUID id;
    private final UUID orderId;
    private final List<Ingredient> ingredients;

    public PancakeViewImpl(Pancake pancake) {
        this.id = pancake.getId();
        this.orderId = pancake.getOrderId();
        this.ingredients = List.copyOf(pancake.getIngredients());
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public UUID getOrderId() {
        return orderId;
    }

    @Override
    public List<Ingredient> getIngredients() {
        return ingredients;
    }
}
