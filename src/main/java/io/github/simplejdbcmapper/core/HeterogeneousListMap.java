package io.github.simplejdbcmapper.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HeterogeneousListMap {
	// Map stores Class as key and Object (the List) as value
	private final Map<Class<?>, List<?>> map = new HashMap<>();

	public <T> void putList(Class<T> type, List<T> list) {
		map.put(Objects.requireNonNull(type), list);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getList(Class<T> type) {
		// Safe because putList only allows List<T> for Class<T>
		return (List<T>) map.get(type);
	}
}
