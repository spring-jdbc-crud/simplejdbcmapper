package io.github.simplejdbcmapper.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class SimpleCache<K, V> {
	private Map<K, V> cache = new ConcurrentHashMap<>();

	private int capacity = -1; // default capacity - no limit

	public SimpleCache() {
	}

	public SimpleCache(int capacity) {
		this.capacity = capacity;
	}

	public V get(K key) {
		return cache.get(key);
	}

	public void put(K key, V value) {
		if (capacity == -1 || cache.size() < capacity) {
			cache.putIfAbsent(key, value);
		}
	}

	public V remove(K key) {
		return cache.remove(key);
	}

	public boolean containsKey(K key) {
		return cache.containsKey(key);
	}

	public int getSize() {
		return cache.size();
	}

	public void clear() {
		cache.clear();
	}

}
