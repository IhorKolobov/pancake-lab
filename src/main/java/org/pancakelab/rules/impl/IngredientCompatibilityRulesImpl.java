package org.pancakelab.rules.impl;

import org.pancakelab.model.pancake.Ingredient;
import org.pancakelab.rules.IngredientCompatibilityRules;

import java.util.Map;
import java.util.Set;

public final class IngredientCompatibilityRulesImpl implements IngredientCompatibilityRules {

    private static final Map<Ingredient, Set<Ingredient>> RULES = Map.of(
            Ingredient.MUSTARD, Set.of(Ingredient.WHIPPED_CREAM),
            Ingredient.WHIPPED_CREAM, Set.of(Ingredient.MUSTARD)
    );

    @Override
    public Set<Ingredient> getIncompatibleWith(Ingredient ingredient) {
        return RULES.getOrDefault(ingredient, Set.of());
    }
}
