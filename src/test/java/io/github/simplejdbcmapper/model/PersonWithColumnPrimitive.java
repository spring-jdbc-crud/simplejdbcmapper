package io.github.simplejdbcmapper.model;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;
import io.github.simplejdbcmapper.annotation.Table;

@Table(name = "person")
public class PersonWithColumnPrimitive {
	@Id(type = IdType.AUTO_GENERATED)
	private Integer personId;
	@Column
	private String lastName;
	@Column
	private String firstName;

	@Column
	private boolean boolProperty;

	public Integer getPersonId() {
		return personId;
	}

	public void setPersonId(Integer id) {
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

	public boolean isBoolProperty() {
		return boolProperty;
	}

	public void setBoolProperty(boolean boolProperty) {
		this.boolProperty = boolProperty;
	}

}
