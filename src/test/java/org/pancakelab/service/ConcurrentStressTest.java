package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.pancakelab.model.order.Order;
import org.pancakelab.model.pancake.Ingredient;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("Concurrent Stress Test for Pancake Flow")
public class ConcurrentStressTest {

    private OrderService orderService;
    private PancakeAssemblyService pancakeAssemblyService;
    private ChefService chefService;
    private DeliveryService deliveryService;
    private OrderLogService orderLogService;

    private static final int THREAD_COUNT = 10;
    private static final int REPETITIONS = 20;

    @BeforeEach
    void setUp() {
        ConcurrentMap<UUID, Order> orderStore = new ConcurrentHashMap<>();
        var orderLookup = new OrderLookupServiceImpl(orderStore);
        var orderValidation = new OrderValidationServiceImpl();
        var ingredientValidation = new IngredientValidationServiceImpl(new IngredientCompatibilityRulesImpl());

        orderLogService = new OrderLogServiceImpl();
        pancakeAssemblyService = new PancakeAssemblyServiceImpl(
                ingredientValidation, orderLogService, orderLookup, orderValidation
        );
        orderService = new OrderServiceImpl(
                new LocationValidationServiceImpl(), orderValidation,
                orderLogService, pancakeAssemblyService, orderStore
        );
        chefService = new ChefServiceImpl(orderLookup, pancakeAssemblyService, orderValidation, orderLogService);
        deliveryService = new DeliveryServiceImpl(chefService, orderService, pancakeAssemblyService, orderLogService, orderLookup);
    }

    @RepeatedTest(10)
    void stressTestUnderHeavyConcurrency() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < THREAD_COUNT * REPETITIONS; i++) {
            tasks.add(createOrderTask());
        }

        List<Future<Void>> results = executor.invokeAll(tasks);
        executor.shutdown();
        boolean finished = executor.awaitTermination(1, TimeUnit.MINUTES);
        assertTrue(finished, "Executor did not terminate in time");

        for (Future<Void> result : results) {
            try {
                result.get(); // check for exceptions inside threads
            } catch (ExecutionException e) {
                fail("Thread failed with: " + e.getCause().getClass().getSimpleName() + " - " + e.getCause().getMessage());
            }
        }

        int expectedMinimumLogs = THREAD_COUNT * REPETITIONS * 5; // 5 log entries per full flow
        assertTrue(orderLogService.getLogs().size() >= expectedMinimumLogs,
                "Expected at least " + expectedMinimumLogs + " log entries");
    }

    private Callable<Void> createOrderTask() {
        return () -> {
            UUID orderId = orderService.createOrder(1, 1 + ThreadLocalRandom.current().nextInt(1000));
            UUID pancakeId = pancakeAssemblyService.startNewPancake(orderId);
            pancakeAssemblyService.addIngredient(pancakeId, Ingredient.MILK_CHOCOLATE);
            pancakeAssemblyService.finalizePancake(pancakeId);
            orderService.completeOrder(orderId);
            chefService.prepareOrder(orderId);
            deliveryService.deliverOrder(orderId);
            return null;
        };
    }
}