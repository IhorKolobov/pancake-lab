package org.pancakelab.service.logging;

import org.pancakelab.model.order.Order;

import java.util.List;


public interface OrderLogService {

    void logCreateOrder(Order order);

    void logCancelOrder(Order order, int pancakeCount);

    void logCompleteOrder(Order order);

    void logAddPancake(Order order, String description, int totalCount);

    void logRemovePancake(Order order, String label, int removedCount, int remaining);

    void logPrepareOrder(Order order, int pancakeCount);

    void logDeliverOrder(Order order, int pancakeCount);

    List<String> getLogs();
}
