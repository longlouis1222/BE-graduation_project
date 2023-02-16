package com.hau.huylong.graduation_proejct.common.util;

import java.util.*;

public class MapUtil {
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static void main(String[] args) {
        Map<Long, Integer> map = new HashMap<>();
        map.put(1L,3);
        map.put(2L, 3);
        map.put(3L,4);

        map.forEach((k, v) -> {

        });

        System.out.println(sortByValue(map));
    }
}
