package io.github.simplejdbcmapper.relationship;

import java.util.List;

public interface GetListSpec {
	<T> List<T> getList(Class<T> type);
}
