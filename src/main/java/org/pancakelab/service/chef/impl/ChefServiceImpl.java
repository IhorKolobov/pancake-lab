package org.pancakelab.service.chef.impl;

import org.pancakelab.api.view.pancake.PancakeView;
import org.pancakelab.exception.NoPancakesFoundException;
import org.pancakelab.exception.OrderNotFoundException;
import org.pancakelab.model.order.Order;
import org.pancakelab.model.pancake.Pancake;
import org.pancakelab.service.assembly.PancakeAssemblyService;
import org.pancakelab.service.chef.ChefService;
import org.pancakelab.service.logging.OrderLogService;
import org.pancakelab.service.order.OrderLookupService;
import org.pancakelab.service.validation.OrderValidationService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ChefServiceImpl implements ChefService {

    private final OrderLookupService orderLookupService;
    private final PancakeAssemblyService pancakeAssemblyService;
    private final OrderValidationService orderValidationService;
    private final OrderLogService orderLogService;

    private final Map<UUID, List<PancakeView>> preparedOrders = new ConcurrentHashMap<>();

    public ChefServiceImpl(OrderLookupService orderLookupService,
                           PancakeAssemblyService pancakeAssemblyService,
                           OrderValidationService orderValidationService,
                           OrderLogService orderLogService) {
        this.orderLookupService = orderLookupService;
        this.pancakeAssemblyService = pancakeAssemblyService;
        this.orderValidationService = orderValidationService;
        this.orderLogService = orderLogService;
    }

    @Override
    public void prepareOrder(UUID orderId) {
        Order order = orderLookupService.findOrder(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        orderValidationService.ensureReadyForPreparation(order);

        List<PancakeView> pancakes = pancakeAssemblyService.listPancakes(orderId);
        if (pancakes.isEmpty()) {
            throw new NoPancakesFoundException(orderId);
        }

        List<PancakeView> immutablePancakes = List.copyOf(pancakes);

        preparedOrders.put(orderId, immutablePancakes);
        orderLogService.logPrepareOrder(order, immutablePancakes.size());
    }

    @Override
    public List<UUID> listPreparedOrders() {
        return List.copyOf(preparedOrders.keySet());
    }

    @Override
    public Optional<List<PancakeView>> takePreparedOrder(UUID orderId) {
        List<PancakeView> pancakes = preparedOrders.remove(orderId);
        return pancakes != null ? Optional.of(pancakes) : Optional.empty();
    }
}
