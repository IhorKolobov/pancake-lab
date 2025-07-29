package org.pancakelab.model.pancake;

import org.pancakelab.util.format.PancakeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Pancake {

    private final UUID id;
    private final UUID orderId;
    private final List<Ingredient> ingredients;

    private Pancake(UUID id, UUID orderId, List<Ingredient> ingredients) {
        this.id = id;
        this.orderId = orderId;
        this.ingredients = new ArrayList<>(ingredients);
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public List<Ingredient> getIngredients() {
        return List.copyOf(ingredients);
    }

    public String description() {
        return PancakeFormatter.getDescription(this);
    }

    public static final class Builder {

        private final UUID id = UUID.randomUUID();
        private final UUID orderId;
        private final List<Ingredient> ingredients = new ArrayList<>();

        public Builder(UUID orderId) {
            this.orderId = orderId;
        }

        public Builder addIngredient(Ingredient ingredient) {
            synchronized (ingredients) {
                ingredients.add(ingredient);
            }
            return this;
        }

        public Pancake build() {
            synchronized (ingredients) {
                return new Pancake(id, orderId, new ArrayList<>(ingredients));
            }
        }

        public UUID getId() {
            return id;
        }

        public UUID getOrderId() {
            return orderId;
        }

        public List<Ingredient> getIngredients() {
            synchronized (ingredients) {
                return List.copyOf(ingredients);
            }
        }
    }
}

