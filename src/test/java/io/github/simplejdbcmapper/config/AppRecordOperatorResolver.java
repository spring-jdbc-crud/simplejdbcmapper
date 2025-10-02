package io.github.simplejdbcmapper.config;

import io.github.simplejdbcmapper.core.RecordOperatorResolver;

public class AppRecordOperatorResolver implements RecordOperatorResolver {
	public Object getRecordOperator() {
		return "tester";
	}
}
