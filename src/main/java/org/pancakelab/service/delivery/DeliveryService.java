package org.pancakelab.service.delivery;

import java.util.List;
import java.util.UUID;

public interface DeliveryService {

    List<UUID> listReadyForDelivery();

    void deliverOrder(UUID orderId);
}
