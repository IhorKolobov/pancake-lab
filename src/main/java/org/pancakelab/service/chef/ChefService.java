package org.pancakelab.service.chef;

import org.pancakelab.api.view.pancake.PancakeView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChefService {

    void prepareOrder(UUID orderId);

    List<UUID> listPreparedOrders();

    Optional<List<PancakeView>> takePreparedOrder(UUID orderId);
}
