package org.pancakelab.service.assembly;

import org.pancakelab.api.view.pancake.PancakeView;
import org.pancakelab.model.pancake.Ingredient;

import java.util.List;
import java.util.UUID;

public interface PancakeAssemblyService {

    UUID startNewPancake(UUID orderId);

    void addIngredient(UUID pancakeId, Ingredient ingredient);

    void finalizePancake(UUID pancakeId);

    List<PancakeView> listPancakes(UUID orderId);

    void removePancake(UUID pancakeId);

    void removePancakesByOrderId(UUID orderId);
}

