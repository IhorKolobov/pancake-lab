package org.pancakelab.common;

public final class LogMessages {

    private LogMessages() {
    }

    public static final String CREATED = "Created order %s (building %d, room %d)";
    public static final String CANCELLED = "Cancelled order %s with %d pancakes (building %d, room %d)";
    public static final String COMPLETED = "Completed order %s (building %d, room %d)";
    public static final String ADDED_PANCAKE = "Added pancake '%s' to order %s (%d total, building %d, room %d)";
    public static final String REMOVED_PANCAKE = "Removed %d '%s' from order %s (%d remaining, building %d, room %d)";
    public static final String PREPARED = "Prepared order %s with %d pancakes (building %d, room %d)";
    public static final String DELIVERED = "Delivered order %s with %d pancakes (building %d, room %d)";
}
