package io.github.simplejdbcmapper.relationship;

import java.util.ArrayList;
import java.util.List;

/**
 * The starting point for any relationship assignment. It works with the lists
 * provided and depending on the arguments to the api methods, populates the
 * target property. The terminal method in the api flow is populate() which
 * triggers the processing.
 * 
 * <p>
 * Processing does not access the database or use SimpleJdbcMapper.
 * 
 * @author Antony Joseph
 */
public class RelationshipMapper {
	static final String IS_PREFIX = "is";
	static final String GET_PREFIX = "get";
	static final String SET_PREFIX = "set";

	// private final Map<Class<?>, List<?>> map = new HashMap<>();

	private List<ExtractorResult> results = new ArrayList<>();

	public <T> void addList(Class<T> type, List<T> list) {
		results.add(new ExtractorResult(type, list, null));
	}

	public <T> void addResult(Class<T> type, List<T> list, String idPropertyName) {
		results.add(new ExtractorResult(type, list, idPropertyName));
	}

	public <T> RelationshipSpec type(Class<T> type) {
		return Relationship.newInstance(type, results);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getList(Class<T> type) {
		for (ExtractorResult result : results) {
			if (result.entityType == type) {
				return (List<T>) result.list;
			}
		}
		return null;
	}

	ExtractorResult getExtractorResult(Class<?> type) {
		for (ExtractorResult result : results) {
			if (result.entityType == type) {
				return result;
			}
		}
		return null;
	}

	record ExtractorResult(Class<?> entityType, List<?> list, String idPropertyName) {
	}

}
