package com.walkmission.global.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 법정동 코드(10자리) 도우미.
 * 코드 구조: 시도 2 + 시군구 3 + 읍면동 3 + 리 2. 랭킹은 앞 5자리(시·군·구) 단위로 묶는다.
 * 이름은 resources/region/legal-dong.tsv ("코드\t이름") 에서 읽고, 없으면 기본 문구를 준다.
 * 데이터 출처: 공공데이터포털 "국토교통부_전국 법정동" (2026-07-29 기준). 행정구역이 바뀌면 새 파일로 갱신한다.
 */
public final class RegionUtils {
    public static final String REGEX = "\\d{10}";
    public static final String UNKNOWN_NAME = "지역 정보 없음";

    private static final Pattern CODE = Pattern.compile(REGEX);
    private static final Map<String, String> NAMES = loadNames();

    private RegionUtils() {}

    public static boolean isValid(String regionCode) {
        return regionCode != null && CODE.matcher(regionCode).matches();
    }

    /** 랭킹 단위 코드: 법정동 코드 앞 5자리 (예: 1174010800 → 11740, 서울 강동구) */
    public static String districtOf(String regionCode) {
        return regionCode != null && regionCode.length() >= 5 ? regionCode.substring(0, 5) : regionCode;
    }

    /** 법정동 이름 (예: 1174010800 → 서울특별시 강동구 성내동) */
    public static String nameOf(String regionCode) {
        return regionCode == null ? UNKNOWN_NAME : NAMES.getOrDefault(regionCode, UNKNOWN_NAME);
    }

    /** 시·군·구 이름 (예: 11740 → 서울특별시 강동구) */
    public static String districtNameOf(String districtCode) {
        return districtCode == null ? UNKNOWN_NAME : nameOf(districtCode + "00000");
    }

    private static Map<String, String> loadNames() {
        Map<String, String> names = new HashMap<>();
        InputStream in = RegionUtils.class.getResourceAsStream("/region/legal-dong.tsv");
        if (in == null) return names;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int tab = line.indexOf('\t');
                if (tab > 0) names.put(line.substring(0, tab), line.substring(tab + 1).trim());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load legal dong names", e);
        }
        return names;
    }
}
