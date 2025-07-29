package org.pancakelab.service.order.impl;

import org.pancakelab.api.view.order.OrderView;
import org.pancakelab.api.view.order.impl.OrderViewImpl;
import org.pancakelab.api.view.pancake.PancakeView;
import org.pancakelab.exception.OrderNotFoundException;
import org.pancakelab.exception.OrderStoreNotThreadSafeException;
import org.pancakelab.model.order.Order;
import org.pancakelab.model.order.OrderFactory;
import org.pancakelab.model.order.OrderStatus;
import org.pancakelab.service.assembly.PancakeAssemblyService;
import org.pancakelab.service.logging.OrderLogService;
import org.pancakelab.service.order.OrderService;
import org.pancakelab.service.validation.LocationValidationService;
import org.pancakelab.service.validation.OrderValidationService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import static org.pancakelab.common.OrderFieldNames.BUILDING;
import static org.pancakelab.common.OrderFieldNames.ROOM;

public final class OrderServiceImpl implements OrderService {

    private final Map<UUID, Order> orders;

    private final LocationValidationService locationValidationService;
    private final OrderValidationService orderValidationService;
    private final OrderLogService orderLogService;
    private final PancakeAssemblyService pancakeAssemblyService;

    public OrderServiceImpl(LocationValidationService locationValidationService,
                            OrderValidationService orderValidationService,
                            OrderLogService orderLogService,
                            PancakeAssemblyService pancakeAssemblyService,
                            Map<UUID, Order> orders) {
        if (!(orders instanceof ConcurrentMap)) {
            throw new OrderStoreNotThreadSafeException();
        }
        this.locationValidationService = locationValidationService;
        this.orderValidationService = orderValidationService;
        this.orderLogService = orderLogService;
        this.pancakeAssemblyService = pancakeAssemblyService;
        this.orders = orders;
    }

    @Override
    public UUID createOrder(int building, int room) {
        locationValidationService.validateLocationInput(BUILDING, building);
        locationValidationService.validateLocationInput(ROOM, room);

        Order order = OrderFactory.create(building, room);
        orders.put(order.getId(), order);

        orderLogService.logCreateOrder(order);
        return order.getId();
    }

    @Override
    public void cancelOrder(UUID orderId) {
        Order current = getExistingOrder(orderId);
        orderValidationService.ensureModifiable(current);

        List<PancakeView> pancakes = pancakeAssemblyService.listPancakes(orderId);
        int pancakeCount = pancakes.size();

        orderLogService.logCancelOrder(current.withStatus(OrderStatus.CANCELLED), pancakeCount);

        orders.remove(orderId);
        pancakeAssemblyService.removePancakesByOrderId(orderId);
    }

    @Override
    public void completeOrder(UUID orderId) {
        updateOrderStatus(orderId, orderLogService::logCompleteOrder);
    }

    @Override
    public Optional<OrderView> findOrder(UUID orderId) {
        return Optional.ofNullable(orders.get(orderId))
                .map(OrderViewImpl::new);
    }

    @Override
    public void deleteOrder(UUID orderId) {
        orders.remove(orderId);
    }

    private void updateOrderStatus(UUID orderId, Consumer<Order> afterUpdateLog) {
        Order current = getExistingOrder(orderId);
        orderValidationService.ensureModifiable(current);

        Order updated = current.withStatus(OrderStatus.COMPLETED);
        orders.put(orderId, updated);

        afterUpdateLog.accept(updated);
    }

    private Order getExistingOrder(UUID orderId) {
        return Optional.ofNullable(orders.get(orderId))
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}



