package org.pancakelab.service.validation;

import org.pancakelab.model.pancake.Ingredient;
import org.pancakelab.model.pancake.Pancake;

public interface IngredientValidationService {

    void validateIngredientCombination(Pancake.Builder builder, Ingredient newIngredient);
}
