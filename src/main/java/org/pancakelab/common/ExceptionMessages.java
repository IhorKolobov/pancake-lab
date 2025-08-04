package org.pancakelab.common;

public final class ExceptionMessages {

    private ExceptionMessages() {
    }

    public static final String ORDER_NOT_FOUND = "Order not found: %s";
    public static final String ORDER_NOT_READY_FOR_DELIVERY = "Order is not ready for delivery: %s";
    public static final String PANCAKE_NOT_FOUND = "Pancake with ID %s not found or already finalized";
    public static final String NO_PANCAKES_FOUND = "No pancakes found for order: %s";
    public static final String TOO_MANY_INGREDIENTS = "Too many ingredients: max %d allowed";
    public static final String INCOMPATIBLE_INGREDIENTS = "Ingredient %s cannot be combined with %s";
    public static final String UNKNOWN_VALIDATION_FIELD = "Unknown validation field: %s";
    public static final String INVALID_FIELD_VALUE = "Invalid %s value: %d (expected between %d and %d)";
    public static final String ORDER_NOT_MODIFIABLE = "Only CREATED orders can be modified.";
    public static final String ORDER_NOT_READY_FOR_PREPARATION = "Order must be COMPLETED before it can be prepared.";
    public static final String THREAD_SAFE_ERROR_MSG = "Order store must be a thread-safe map (e.g. ConcurrentHashMap)";
    public static final String INVALID_COMPLETED_LIST_TYPE = "Expected CopyOnWriteArrayList for completedPancakes[%s], but got: %s";
    public static final String ORDER_FACTORY_SHOULD_NOT_BE_INSTANTIATED = "OrderFactory should not be instantiated.";
}
