package org.pancakelab.service.validation.impl;

import org.pancakelab.exception.InvalidAddressException;
import org.pancakelab.service.validation.LocationValidationService;

import java.util.Map;

import static org.pancakelab.common.ExceptionMessages.INVALID_FIELD_VALUE;
import static org.pancakelab.common.ExceptionMessages.UNKNOWN_VALIDATION_FIELD;
import static org.pancakelab.common.OrderConstants.MAX_BUILDING;
import static org.pancakelab.common.OrderConstants.MAX_ROOM;
import static org.pancakelab.common.OrderConstants.MIN_BUILDING;
import static org.pancakelab.common.OrderConstants.MIN_ROOM;
import static org.pancakelab.common.OrderFieldNames.BUILDING;
import static org.pancakelab.common.OrderFieldNames.ROOM;

public final class LocationValidationServiceImpl implements LocationValidationService {

    private static final Map<String, IntRange> validationRules = Map.of(
            BUILDING, new IntRange(MIN_BUILDING, MAX_BUILDING),
            ROOM, new IntRange(MIN_ROOM, MAX_ROOM)
    );

    @Override
    public void validateLocationInput(String fieldName, int value) {
        IntRange range = validationRules.get(fieldName);
        if (range == null) {
            throw new InvalidAddressException(UNKNOWN_VALIDATION_FIELD.formatted(fieldName));
        }
        if (!range.contains(value)) {
            throw new InvalidAddressException(
                    INVALID_FIELD_VALUE.formatted(fieldName, value, range.min(), range.max())
            );
        }
    }

    private record IntRange(int min, int max) {
        boolean contains(int val) {
            return val >= min && val <= max;
        }
    }
}
