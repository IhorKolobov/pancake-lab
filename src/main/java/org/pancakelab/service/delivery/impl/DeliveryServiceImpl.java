package org.pancakelab.service.delivery.impl;

import org.pancakelab.api.view.pancake.PancakeView;
import org.pancakelab.exception.OrderNotFoundException;
import org.pancakelab.exception.OrderNotReadyForDeliveryException;
import org.pancakelab.model.order.Order;
import org.pancakelab.service.assembly.PancakeAssemblyService;
import org.pancakelab.service.chef.ChefService;
import org.pancakelab.service.delivery.DeliveryService;
import org.pancakelab.service.logging.OrderLogService;
import org.pancakelab.service.order.OrderLookupService;
import org.pancakelab.service.order.OrderService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class DeliveryServiceImpl implements DeliveryService {

    private final ChefService chefService;
    private final OrderService orderService;
    private final PancakeAssemblyService pancakeAssemblyService;
    private final OrderLogService orderLogService;
    private final OrderLookupService orderLookupService;

    public DeliveryServiceImpl(ChefService chefService,
                               OrderService orderService,
                               PancakeAssemblyService pancakeAssemblyService,
                               OrderLogService orderLogService, OrderLookupService orderLookupService) {
        this.chefService = chefService;
        this.orderService = orderService;
        this.pancakeAssemblyService = pancakeAssemblyService;
        this.orderLogService = orderLogService;
        this.orderLookupService = orderLookupService;
    }

    @Override
    public List<UUID> listReadyForDelivery() {
        return chefService.listPreparedOrders();
    }

    @Override
    public void deliverOrder(UUID orderId) {
        Optional<List<PancakeView>> maybePancakes = chefService.takePreparedOrder(orderId);

        if (maybePancakes.isEmpty()) {
            throw new OrderNotReadyForDeliveryException(orderId);
        }

        List<PancakeView> pancakes = maybePancakes.get();

        Order order = orderLookupService.findOrder(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        orderLogService.logDeliverOrder(order, pancakes.size());
        removeAllPancakes(pancakes);
        orderService.deleteOrder(orderId);
    }

    private void removeAllPancakes(List<PancakeView> pancakes) {
        for (PancakeView pancake : pancakes) {
            pancakeAssemblyService.removePancake(pancake.getId());
        }
    }
}
