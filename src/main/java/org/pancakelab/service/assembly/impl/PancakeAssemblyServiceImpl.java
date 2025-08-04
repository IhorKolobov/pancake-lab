package org.pancakelab.service.assembly.impl;

import org.pancakelab.api.view.pancake.PancakeView;
import org.pancakelab.api.view.pancake.impl.PancakeViewImpl;
import org.pancakelab.exception.OrderNotFoundException;
import org.pancakelab.exception.PancakeNotFoundException;
import org.pancakelab.model.order.Order;
import org.pancakelab.model.pancake.Ingredient;
import org.pancakelab.model.pancake.Pancake;
import org.pancakelab.service.assembly.PancakeAssemblyService;
import org.pancakelab.service.logging.OrderLogService;
import org.pancakelab.service.order.OrderLookupService;
import org.pancakelab.service.validation.IngredientValidationService;
import org.pancakelab.service.validation.OrderValidationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.pancakelab.common.ExceptionMessages.INVALID_COMPLETED_LIST_TYPE;
import static org.pancakelab.common.PancakeAssemblyConstants.COMPLETED_PANCAKE_LABEL;
import static org.pancakelab.common.PancakeAssemblyConstants.DRAFT_PANCAKE_LABEL;
import static org.pancakelab.common.PancakeAssemblyConstants.INGREDIENT_CANNOT_BE_NULL;
import static org.pancakelab.common.PancakeAssemblyConstants.REMOVED_SINGLE_COUNT;
import static org.pancakelab.common.PancakeAssemblyConstants.UNKNOWN_REMAINING_COUNT;

public final class PancakeAssemblyServiceImpl implements PancakeAssemblyService {

    private final Map<UUID, Pancake.Builder> draftPancakes = new ConcurrentHashMap<>();
    private final Map<UUID, List<Pancake>> completedPancakes = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> pancakeToOrderMap = new ConcurrentHashMap<>();

    private final IngredientValidationService ingredientValidationService;
    private final OrderLogService orderLogService;
    private final OrderLookupService orderLookupService;
    private final OrderValidationService orderValidationService;

    public PancakeAssemblyServiceImpl(IngredientValidationService ingredientValidationService,
                                      OrderLogService orderLogService, OrderLookupService orderLookupService,
                                      OrderValidationService orderValidationService) {
        this.ingredientValidationService = ingredientValidationService;
        this.orderLogService = orderLogService;
        this.orderLookupService = orderLookupService;
        this.orderValidationService = orderValidationService;
    }

    @Override
    public UUID startNewPancake(UUID orderId) {
        Order order = orderLookupService.findOrder(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        orderValidationService.ensureModifiable(order);

        Pancake.Builder builder = new Pancake.Builder(orderId);
        draftPancakes.put(builder.getId(), builder);
        return builder.getId();
    }

    @Override
    public void addIngredient(UUID pancakeId, Ingredient ingredient) {
        Objects.requireNonNull(ingredient, INGREDIENT_CANNOT_BE_NULL);

        draftPancakes.compute(pancakeId, (id, builder) -> {
            ensureBuilderExists(pancakeId, builder);

            ingredientValidationService.validateIngredientCombination(builder, ingredient);

            return builder.addIngredient(ingredient);
        });
    }

    @Override
    public void finalizePancake(UUID pancakeId) {
        Pancake.Builder builder = draftPancakes.remove(pancakeId);
        ensureBuilderExists(pancakeId, builder);

        Pancake pancake = builder.build();
        UUID orderId = pancake.getOrderId();

        List<Pancake> completed = completedPancakes.computeIfAbsent(orderId, id -> new CopyOnWriteArrayList<>());

        completed.add(pancake);
        pancakeToOrderMap.put(pancake.getId(), orderId);

        Order order = getExistingOrder(orderId);
        orderLogService.logAddPancake(order, pancake.description(), completed.size());
    }

    @Override
    public List<PancakeView> listPancakes(UUID orderId) {
        List<Pancake> original = completedPancakes.getOrDefault(orderId, List.of());
        return List.copyOf(original).stream()
                .map(p -> (PancakeView) new PancakeViewImpl(p))
                .toList();
    }

    @Override
    public void removePancake(UUID pancakeId) {
        if (tryRemoveFromDrafts(pancakeId)) {
            return;
        }
        tryRemoveFromCompleted(pancakeId);
    }

    @Override
    public void removePancakesByOrderId(UUID orderId) {
        completedPancakes.remove(orderId);

        List<UUID> toRemove = new ArrayList<>();
        for (Map.Entry<UUID, Pancake.Builder> entry : draftPancakes.entrySet()) {
            if (entry.getValue().getOrderId().equals(orderId)) {
                toRemove.add(entry.getKey());
            }
        }

        for (UUID id : toRemove) {
            draftPancakes.remove(id);
        }
    }

    private boolean tryRemoveFromDrafts(UUID pancakeId) {
        Pancake.Builder draft = draftPancakes.remove(pancakeId);
        if (draft != null) {
            UUID orderId = draft.getOrderId();
            Order order = getExistingOrder(orderId);
            orderLogService.logRemovePancake(order, DRAFT_PANCAKE_LABEL, REMOVED_SINGLE_COUNT, UNKNOWN_REMAINING_COUNT);
            return true;
        }
        return false;
    }

    private void tryRemoveFromCompleted(UUID pancakeId) {
        UUID orderId = pancakeToOrderMap.remove(pancakeId);
        if (orderId == null) return;

        List<Pancake> pancakes = completedPancakes.get(orderId);
        if (pancakes == null) return;

        assertIsCopyOnWriteList(pancakes, orderId);

        int before = pancakes.size();
        boolean removed = pancakes.removeIf(p -> p.getId().equals(pancakeId));
        int after = pancakes.size();

        if (removed) {
            Order order = getExistingOrder(orderId);
            orderLogService.logRemovePancake(order, COMPLETED_PANCAKE_LABEL, before - after, after);
        }
    }

    private void assertIsCopyOnWriteList(List<?> list, UUID orderId) {
        if (!(list instanceof CopyOnWriteArrayList<?>)) {
            throw new IllegalStateException(
                    String.format(INVALID_COMPLETED_LIST_TYPE, orderId, list.getClass())
            );
        }
    }

    private void ensureBuilderExists(UUID pancakeId, Pancake.Builder builder) {
        if (builder == null) {
            throw new PancakeNotFoundException(pancakeId);
        }
    }

    private Order getExistingOrder(UUID orderId) {
        return orderLookupService.findOrder(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
