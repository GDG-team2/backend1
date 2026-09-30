package com.walkmission.domain.mission.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/** Set<PlaceCategory> ↔ "CAFE,WALK" 문자열 */
@Converter
public class PlaceCategorySetConverter implements AttributeConverter<Set<PlaceCategory>, String> {

    @Override
    public String convertToDatabaseColumn(Set<PlaceCategory> categories) {
        if (categories == null || categories.isEmpty()) return null;
        return categories.stream().map(Enum::name).sorted().collect(Collectors.joining(","));
    }

    @Override
    public Set<PlaceCategory> convertToEntityAttribute(String value) {
        EnumSet<PlaceCategory> categories = EnumSet.noneOf(PlaceCategory.class);
        if (value == null || value.isBlank()) return categories;
        for (String name : value.split(",")) {
            Arrays.stream(PlaceCategory.values())
                    .filter(c -> c.name().equals(name.trim()))
                    .findFirst()
                    .ifPresent(categories::add);
        }
        return categories;
    }
}
