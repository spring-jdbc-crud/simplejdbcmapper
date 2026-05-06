package io.github.simplejdbcmapper.relationship;

import java.util.List;

public interface GetListSpec {

	<E> List<E> getList(Class<E> type);

}
