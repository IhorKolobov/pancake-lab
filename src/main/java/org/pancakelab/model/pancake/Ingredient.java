package org.pancakelab.model.pancake;

public enum Ingredient {

    DARK_CHOCOLATE("dark chocolate"),
    MILK_CHOCOLATE("milk chocolate"),
    WHIPPED_CREAM("whipped cream"),
    HAZELNUTS("hazelnuts"),
    MUSTARD("mustard");

    private final String displayName;

    Ingredient(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
