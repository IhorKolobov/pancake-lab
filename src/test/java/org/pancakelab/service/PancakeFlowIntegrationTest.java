package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.pancakelab.api.view.order.OrderView;
import org.pancakelab.common.LogMessages;
import org.pancakelab.exception.InvalidOrderStateException;
import org.pancakelab.exception.InvalidPancakeOperationException;
import org.pancakelab.model.order.Order;
import org.pancakelab.model.order.OrderStatus;
import org.pancakelab.model.pancake.Ingredient;
import org.pancakelab.model.pancake.Pancake;
import org.pancakelab.rules.impl.IngredientCompatibilityRulesImpl;
import org.pancakelab.service.assembly.PancakeAssemblyService;
import org.pancakelab.service.assembly.impl.PancakeAssemblyServiceImpl;
import org.pancakelab.service.chef.ChefService;
import org.pancakelab.service.chef.impl.ChefServiceImpl;
import org.pancakelab.service.delivery.DeliveryService;
import org.pancakelab.service.delivery.impl.DeliveryServiceImpl;
import org.pancakelab.service.logging.OrderLogService;
import org.pancakelab.service.logging.impl.OrderLogServiceImpl;
import org.pancakelab.service.order.OrderService;
import org.pancakelab.service.order.impl.OrderLookupServiceImpl;
import org.pancakelab.service.order.impl.OrderServiceImpl;
import org.pancakelab.service.validation.impl.IngredientValidationServiceImpl;
import org.pancakelab.service.validation.impl.LocationValidationServiceImpl;
import org.pancakelab.service.validation.impl.OrderValidationServiceImpl;
import org.pancakelab.util.format.PancakeFormatter;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PancakeFlowIntegrationTest {

    private OrderService orderService;
    private PancakeAssemblyService pancakeAssemblyService;
    private ChefService chefService;
    private DeliveryService deliveryService;
    private OrderLogService orderLogService;

    private final Map<UUID, Order> orderStore = new ConcurrentHashMap<>();

    private static final int BUILDING = 1;
    private static final int ROOM = 99;
    private static final int PANCAKE_COUNT = 1;
    private static final int PANCAKES_REMAINING_AFTER_REMOVAL = 0;
    public static final String COMPLETED_PANCAKE = "completed pancake";

    @BeforeEach
    void setUp() {
        var locationValidation = new LocationValidationServiceImpl();
        var orderValidation = new OrderValidationServiceImpl();
        var ingredientRules = new IngredientCompatibilityRulesImpl();
        var ingredientValidation = new IngredientValidationServiceImpl(ingredientRules);

        orderLogService = new OrderLogServiceImpl();
        var orderLookupService = new OrderLookupServiceImpl(orderStore);

        pancakeAssemblyService = new PancakeAssemblyServiceImpl(
                ingredientValidation,
                orderLogService,
                orderLookupService,
                orderValidation
        );

        orderService = new OrderServiceImpl(
                locationValidation,
                orderValidation,
                orderLogService,
                pancakeAssemblyService,
                orderStore
        );

        chefService = new ChefServiceImpl(orderLookupService, pancakeAssemblyService, orderValidation, orderLogService);
        deliveryService = new DeliveryServiceImpl(chefService, orderService, pancakeAssemblyService, orderLogService, orderLookupService);
    }

    @Test
    void shouldProcessAndLogFullOrderFlowSuccessfully() {
        UUID orderId = orderService.createOrder(BUILDING, ROOM);
        OrderView order = orderService.findOrder(orderId).orElseThrow();
        assertEquals(OrderStatus.CREATED, order.getStatus());

        UUID pancakeId = pancakeAssemblyService.startNewPancake(orderId);
        pancakeAssemblyService.addIngredient(pancakeId, Ingredient.MILK_CHOCOLATE);
        pancakeAssemblyService.finalizePancake(pancakeId);
        assertEquals(PANCAKE_COUNT, pancakeAssemblyService.listPancakes(orderId).size());

        orderService.completeOrder(orderId);
        assertEquals(OrderStatus.COMPLETED, orderService.findOrder(orderId).get().getStatus());

        chefService.prepareOrder(orderId);
        assertTrue(chefService.listPreparedOrders().contains(orderId));

        deliveryService.deliverOrder(orderId);

        assertAll("After delivery, system should be clean",
                () -> assertTrue(orderService.findOrder(orderId).isEmpty(), "Order must be removed"),
                () -> assertTrue(pancakeAssemblyService.listPancakes(orderId).isEmpty(), "Pancakes must be removed"),
                () -> assertFalse(chefService.listPreparedOrders().contains(orderId), "Prepared list must not contain order")
        );

        List<String> logs = orderLogService.getLogs();

        Pancake.Builder builder = new Pancake.Builder(orderId);
        builder.addIngredient(Ingredient.MILK_CHOCOLATE);
        Pancake pancake = builder.build();

        String description = PancakeFormatter.getDescription(pancake);

        String expectedLogOutput = String.join("\n", List.of(
                String.format(LogMessages.CREATED, orderId, BUILDING, ROOM),
                String.format(LogMessages.ADDED_PANCAKE, description, orderId, PANCAKE_COUNT, BUILDING, ROOM),
                String.format(LogMessages.COMPLETED, orderId, BUILDING, ROOM),
                String.format(LogMessages.PREPARED, orderId, PANCAKE_COUNT, BUILDING, ROOM),
                String.format(LogMessages.DELIVERED, orderId, PANCAKE_COUNT, BUILDING, ROOM),
                String.format(LogMessages.REMOVED_PANCAKE, PANCAKE_COUNT, COMPLETED_PANCAKE, orderId, PANCAKES_REMAINING_AFTER_REMOVAL, BUILDING, ROOM)
        ));

        String actualLogOutput = String.join("\n", logs);

        assertEquals(expectedLogOutput, actualLogOutput, "Full log output does not match expected");
    }

    @Test
    void shouldListOnlyPreparedOrdersForDelivery() {
        UUID orderId1 = orderService.createOrder(BUILDING, ROOM);
        UUID pancake1 = pancakeAssemblyService.startNewPancake(orderId1);
        pancakeAssemblyService.addIngredient(pancake1, Ingredient.MILK_CHOCOLATE);
        pancakeAssemblyService.finalizePancake(pancake1);
        orderService.completeOrder(orderId1);
        chefService.prepareOrder(orderId1);

        UUID orderId2 = orderService.createOrder(BUILDING, ROOM);
        UUID pancake2 = pancakeAssemblyService.startNewPancake(orderId2);
        pancakeAssemblyService.addIngredient(pancake2, Ingredient.MILK_CHOCOLATE);
        pancakeAssemblyService.finalizePancake(pancake2);
        orderService.completeOrder(orderId2);

        List<UUID> readyOrders = deliveryService.listReadyForDelivery();

        assertAll("Only prepared orders should be listed",
                () -> assertTrue(readyOrders.contains(orderId1), "Prepared order should be listed"),
                () -> assertFalse(readyOrders.contains(orderId2), "Non-prepared order must not be listed")
        );
    }

    @Test
    void shouldCancelOrderAndRemovePancakesAndLogEvent() {
        UUID orderId = orderService.createOrder(BUILDING, ROOM);
        UUID pancakeId = pancakeAssemblyService.startNewPancake(orderId);
        pancakeAssemblyService.addIngredient(pancakeId, Ingredient.DARK_CHOCOLATE);
        pancakeAssemblyService.finalizePancake(pancakeId);

        orderService.cancelOrder(orderId);

        assertAll("Cancelled order must be removed",
                () -> assertTrue(orderService.findOrder(orderId).isEmpty()),
                () -> assertTrue(pancakeAssemblyService.listPancakes(orderId).isEmpty())
        );

        assertLogContains();
    }

    @Nested
    class NegativeScenarios {

        @Test
        void shouldRejectIncompatibleIngredientsWithException() {
            UUID orderId = orderService.createOrder(BUILDING, ROOM);
            UUID pancakeId = pancakeAssemblyService.startNewPancake(orderId);

            pancakeAssemblyService.addIngredient(pancakeId, Ingredient.MUSTARD);

            Exception ex = assertThrows(
                    InvalidPancakeOperationException.class,
                    () -> pancakeAssemblyService.addIngredient(pancakeId, Ingredient.WHIPPED_CREAM)
            );

            assertEquals("Ingredient WHIPPED_CREAM cannot be combined with [MUSTARD]", ex.getMessage());
        }

        @Test
        void shouldRejectPancakeWithTooManyIngredients() {
            UUID orderId = orderService.createOrder(BUILDING, ROOM);
            UUID pancakeId = pancakeAssemblyService.startNewPancake(orderId);

            for (int i = 0; i < 5; i++) {
                pancakeAssemblyService.addIngredient(pancakeId, Ingredient.MILK_CHOCOLATE);
            }

            Exception ex = assertThrows(
                    InvalidPancakeOperationException.class,
                    () -> pancakeAssemblyService.addIngredient(pancakeId, Ingredient.DARK_CHOCOLATE)
            );

            assertTrue(ex.getMessage().contains("Too many ingredients"));
        }

        @Test
        void shouldRejectModificationOfCompletedOrder() {
            UUID orderId = orderService.createOrder(BUILDING, ROOM);
            UUID pancakeId = pancakeAssemblyService.startNewPancake(orderId);
            pancakeAssemblyService.addIngredient(pancakeId, Ingredient.MILK_CHOCOLATE);
            pancakeAssemblyService.finalizePancake(pancakeId);
            orderService.completeOrder(orderId);

            Exception ex = assertThrows(
                    InvalidOrderStateException.class,
                    () -> pancakeAssemblyService.startNewPancake(orderId)
            );

            assertEquals("Only CREATED orders can be modified.", ex.getMessage());
        }

        @Test
        void shouldRejectPreparationOfIncompleteOrder() {
            UUID orderId = orderService.createOrder(BUILDING, ROOM);

            Exception ex = assertThrows(
                    InvalidOrderStateException.class,
                    () -> chefService.prepareOrder(orderId)
            );

            assertEquals("Order must be COMPLETED before it can be prepared.", ex.getMessage());
        }
    }

    private void assertLogContains() {
        assertTrue(orderLogService.getLogs().stream().anyMatch(l -> l.contains("Cancelled order")),
                "Missing log: " + "Cancelled order");
    }
}