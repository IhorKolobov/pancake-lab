package org.pancakelab.rules;

import org.pancakelab.model.pancake.Ingredient;

import java.util.Set;

public interface IngredientCompatibilityRules {

    Set<Ingredient> getIncompatibleWith(Ingredient ingredient);
}
