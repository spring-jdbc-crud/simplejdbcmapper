package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "person", schema = "SCHEMA1")
public class PersonSchema1 {
	@Id
	private String personId;
	@Column
	private String lastName;
	@Column
	private String firstName;

	private String someNonDatabaseProperty;

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String id) {
		this.personId = id;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getSomeNonDatabaseProperty() {
		return someNonDatabaseProperty;
	}

	public void setSomeNonDatabaseProperty(String someNonDatabaseProperty) {
		this.someNonDatabaseProperty = someNonDatabaseProperty;
	}
}
