package org.pancakelab.util.format;

import org.pancakelab.model.pancake.Ingredient;
import org.pancakelab.model.pancake.Pancake;

import java.util.List;
import java.util.stream.Collectors;

import static org.pancakelab.common.PancakeMessages.DELIMITER;
import static org.pancakelab.common.PancakeMessages.EMPTY_PANCAKE;
import static org.pancakelab.common.PancakeMessages.PANCAKE_DESCRIPTION_PREFIX;

public final class PancakeFormatter {

    private PancakeFormatter() {
    }

    public static String getDescription(Pancake pancake) {
        List<Ingredient> ingredients = pancake.getIngredients();
        if (ingredients.isEmpty()) {
            return EMPTY_PANCAKE;
        }

        String content = ingredients.stream()
                .map(Ingredient::getDisplayName)
                .collect(Collectors.joining(DELIMITER));

        return PANCAKE_DESCRIPTION_PREFIX + content;
    }
}
