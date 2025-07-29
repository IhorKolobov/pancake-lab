package org.pancakelab.api.view.pancake;

import org.pancakelab.model.pancake.Ingredient;

import java.util.List;
import java.util.UUID;

public interface PancakeView {

    UUID getId();

    UUID getOrderId();

    List<Ingredient> getIngredients();
}
