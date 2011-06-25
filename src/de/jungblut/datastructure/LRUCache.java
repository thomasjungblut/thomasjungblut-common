package de.jungblut.datastructure;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> {

	private LinkedHashMap<K, V> map;
	private int cacheSize;

	@SuppressWarnings("serial")
	public LRUCache(int cacheSize) {
		this.cacheSize = cacheSize;
		map = new LinkedHashMap<K, V>() {
			@Override
			protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
				return size() > LRUCache.this.cacheSize;
			}
		};
	}

	public V get(K key) {
		return map.get(key);
	}

	public void put(K key, V value) {
		map.put(key, value);
	}
}
