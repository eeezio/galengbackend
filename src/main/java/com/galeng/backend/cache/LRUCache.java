package com.galeng.backend.cache;

import java.util.LinkedHashMap;

public class LRUCache<K, V> {
    public LinkedHashMap<K, V> map;
    int capacity;

    public LRUCache(int capacity) {
        map = new LinkedHashMap<>();
        this.capacity = capacity;
    }

    public V get(K key) {
        if (map.containsKey(key)) {
            V value = map.get(key);
            map.remove(key);
            map.put(key, value);
            return value;
        } else {
            return null;
        }
    }

    public void put(K key, V value) {
        if (map.containsKey(key)) {
            map.remove(key);
        }
        while (map.size() >= capacity) {
            map.remove(map.entrySet().iterator().next().getKey());
        }
        map.put(key, value);
    }
}
