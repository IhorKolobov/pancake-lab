package org.pancakelab.service.validation.impl;

import org.pancakelab.exception.InvalidPancakeOperationException;
import org.pancakelab.model.pancake.Ingredient;
import org.pancakelab.model.pancake.Pancake;
import org.pancakelab.rules.IngredientCompatibilityRules;
import org.pancakelab.service.validation.IngredientValidationService;

import java.util.List;
import java.util.Set;

import static org.pancakelab.common.ExceptionMessages.INCOMPATIBLE_INGREDIENTS;
import static org.pancakelab.common.ExceptionMessages.TOO_MANY_INGREDIENTS;
import static org.pancakelab.common.PancakeConstants.MAX_INGREDIENTS;

public final class IngredientValidationServiceImpl implements IngredientValidationService {

    private final IngredientCompatibilityRules compatibilityRules;

    public IngredientValidationServiceImpl(IngredientCompatibilityRules compatibilityRules) {
        this.compatibilityRules = compatibilityRules;
    }

    @Override
    public void validateIngredientCombination(Pancake.Builder builder, Ingredient newIngredient) {
        List<Ingredient> current = builder.getIngredients();

        if (current.size() >= MAX_INGREDIENTS) {
            throw new InvalidPancakeOperationException(TOO_MANY_INGREDIENTS.formatted(MAX_INGREDIENTS));
        }

        Set<Ingredient> incompatibleWithNew = compatibilityRules.getIncompatibleWith(newIngredient);
        if (current.stream().anyMatch(incompatibleWithNew::contains)) {
            throw new InvalidPancakeOperationException(INCOMPATIBLE_INGREDIENTS
                    .formatted(newIngredient.name(), incompatibleWithNew));
        }
    }
}