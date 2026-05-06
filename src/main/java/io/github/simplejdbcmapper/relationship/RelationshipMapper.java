package io.github.simplejdbcmapper.relationship;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

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

	private List<ExtractorResult> results = new ArrayList<>();

	public <T> void addEntityResult(Class<T> entityType, List<T> list, String idPropertyName) {
		Assert.notNull(entityType, "entityType must not be null");
		Assert.notNull(list, "list must not be null");

		results.add(new ExtractorResult(entityType, list, idPropertyName));
	}

	public <T> RelationshipSpec type(Class<T> type) {
		Assert.notNull(type, "type must not be null");
		// will throw an exception for invalid type
		getExtractorResult(type);
		return Relationship.newInstance(type, results);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getList(Class<T> type) {
		for (ExtractorResult result : results) {
			if (result.entityType == type) {
				return (List<T>) result.list;
			}
		}
		throw new IllegalArgumentException(type + " was not part of the query result set");
	}

	ExtractorResult getExtractorResult(Class<?> type) {
		for (ExtractorResult result : results) {
			if (result.entityType == type) {
				return result;
			}
		}
		throw new IllegalArgumentException(type + "was not part of the query result set");
	}

	record ExtractorResult(Class<?> entityType, List<?> list, String idPropertyName) {
	}

}
