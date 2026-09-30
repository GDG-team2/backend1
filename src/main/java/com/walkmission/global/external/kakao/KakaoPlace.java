package com.walkmission.global.external.kakao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** 카카오 로컬 검색 결과의 장소 한 건. x = 경도, y = 위도 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoPlace(
        String id,
        @JsonProperty("place_name") String placeName,
        @JsonProperty("category_name") String categoryName,
        @JsonProperty("road_address_name") String roadAddressName,
        @JsonProperty("address_name") String addressName,
        @JsonProperty("place_url") String placeUrl,
        String x,
        String y
) {
    /** "음식점 > 카페 > 커피전문점" → "커피전문점" */
    public String leafCategory() {
        if (categoryName == null) return null;
        String[] parts = categoryName.split(" > ");
        return parts[parts.length - 1];
    }

    public String address() {
        return roadAddressName != null && !roadAddressName.isBlank() ? roadAddressName : addressName;
    }
}
