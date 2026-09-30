package com.walkmission.global.external.kakao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.walkmission.global.error.BusinessException;
import com.walkmission.global.error.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/** 카카오 로컬 API (장소 검색) 클라이언트 */
@Component
public class KakaoLocalClient {
    private static final Logger log = LoggerFactory.getLogger(KakaoLocalClient.class);
    private static final int MAX_RADIUS_METERS = 20_000;
    private static final int PAGE_SIZE = 15;

    private final RestClient restClient;
    private final String apiKey;

    public KakaoLocalClient(RestClient.Builder builder, @Value("${kakao.rest-api-key:}") String apiKey) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3_000);
        requestFactory.setReadTimeout(3_000);
        this.restClient = builder.baseUrl("https://dapi.kakao.com").requestFactory(requestFactory).build();
        this.apiKey = apiKey;
    }

    /** 카테고리 그룹 코드(CE7 카페 등)로 중심점 주변을 가까운 순으로 검색한다. */
    public List<KakaoPlace> searchByCategory(String categoryGroupCode, double latitude, double longitude, int radiusMeters) {
        return search("/v2/local/search/category.json", "category_group_code", categoryGroupCode,
                latitude, longitude, radiusMeters);
    }

    /** 키워드로 중심점 주변을 가까운 순으로 검색한다. */
    public List<KakaoPlace> searchByKeyword(String keyword, double latitude, double longitude, int radiusMeters) {
        return search("/v2/local/search/keyword.json", "query", keyword, latitude, longitude, radiusMeters);
    }

    private List<KakaoPlace> search(String path, String paramName, String paramValue,
                                    double latitude, double longitude, int radiusMeters) {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("kakao.rest-api-key is not configured");
            throw new BusinessException(ErrorCode.PLACE_SEARCH_FAILED);
        }
        try {
            SearchResponse response = restClient.get()
                    .uri(uri -> uri.path(path)
                            .queryParam(paramName, paramValue)
                            .queryParam("x", longitude)
                            .queryParam("y", latitude)
                            .queryParam("radius", Math.max(1, Math.min(radiusMeters, MAX_RADIUS_METERS)))
                            .queryParam("sort", "distance")
                            .queryParam("size", PAGE_SIZE)
                            .build())
                    .header("Authorization", "KakaoAK " + apiKey)
                    .retrieve()
                    .body(SearchResponse.class);
            return response == null || response.documents() == null ? List.of() : response.documents();
        } catch (RestClientException e) {
            log.warn("Kakao local search failed: {}", e.getMessage());
            throw new BusinessException(ErrorCode.PLACE_SEARCH_FAILED);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SearchResponse(List<KakaoPlace> documents) {}
}
