/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.simplejdbcmapper.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple cache implementation
 * 
 * @author Antony Joseph
 */
class SimpleCache<K, V> {
	private static final int UNLIMITED = -1;
	private Map<K, V> cache = new ConcurrentHashMap<>();

	private int capacity = UNLIMITED;

	public SimpleCache() {
	}

	public SimpleCache(int capacity) {
		this.capacity = capacity;
	}

	public V get(K key) {
		return cache.get(key);
	}

	public void put(K key, V value) {
		if (capacity == UNLIMITED || cache.size() < capacity) {
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
