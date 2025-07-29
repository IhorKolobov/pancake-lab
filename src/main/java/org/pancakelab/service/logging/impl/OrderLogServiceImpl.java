package org.pancakelab.service.logging.impl;

import org.pancakelab.model.order.Order;
import org.pancakelab.service.logging.OrderLogService;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.pancakelab.common.LogMessages.ADDED_PANCAKE;
import static org.pancakelab.common.LogMessages.CANCELLED;
import static org.pancakelab.common.LogMessages.COMPLETED;
import static org.pancakelab.common.LogMessages.CREATED;
import static org.pancakelab.common.LogMessages.DELIVERED;
import static org.pancakelab.common.LogMessages.PREPARED;
import static org.pancakelab.common.LogMessages.REMOVED_PANCAKE;

public final class OrderLogServiceImpl implements OrderLogService {

    private final Queue<String> logs = new ConcurrentLinkedQueue<>();

    @Override
    public void logCreateOrder(Order order) {
        logs.add(CREATED.formatted(order.getId(), order.getBuilding(), order.getRoom()));
    }

    @Override
    public void logCancelOrder(Order order, int pancakeCount) {
        logs.add(CANCELLED.formatted(order.getId(), pancakeCount, order.getBuilding(), order.getRoom()));
    }

    @Override
    public void logCompleteOrder(Order order) {
        logs.add(COMPLETED.formatted(order.getId(), order.getBuilding(), order.getRoom()));
    }

    @Override
    public void logAddPancake(Order order, String description, int totalCount) {
        logs.add(ADDED_PANCAKE.formatted(description, order.getId(), totalCount, order.getBuilding(), order.getRoom()));
    }

    @Override
    public void logRemovePancake(Order order, String label, int removedCount, int remaining) {
        logs.add(REMOVED_PANCAKE.formatted(removedCount, label, order.getId(), remaining, order.getBuilding(), order.getRoom()));
    }

    @Override
    public void logPrepareOrder(Order order, int pancakeCount) {
        logs.add(PREPARED.formatted(order.getId(), pancakeCount, order.getBuilding(), order.getRoom()));
    }

    @Override
    public void logDeliverOrder(Order order, int pancakeCount) {
        logs.add(DELIVERED.formatted(order.getId(), pancakeCount, order.getBuilding(), order.getRoom()));
    }

    @Override
    public java.util.List<String> getLogs() {
        return java.util.List.copyOf(logs);
    }
}