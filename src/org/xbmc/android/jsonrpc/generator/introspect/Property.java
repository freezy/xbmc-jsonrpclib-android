package org.xbmc.android.jsonrpc.generator.introspect;

import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.AdditionalPropertiesWrapper;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.TypeWrapper;

public class Property {

	@JsonProperty("$ref")
	protected String ref;

	protected HashMap<String, Property> properties;
	protected List<String> enums;

	protected String description;
	protected TypeWrapper type;
	protected Boolean required;

	protected Type items;
	protected Boolean uniqueItems;
	protected Integer minItems;

	protected Integer minLength;
	protected Integer minimum;
	protected Integer maximum;

	protected AdditionalPropertiesWrapper additionalProperties;

	@JsonProperty("default")
	protected String defaultValue;
	
	/**
	 * Returns true if properties are declared within this type, as opposed to
	 * reference, native type or enum.
	 * 
	 * If true, we assume that the {@link #properties} map is filled up.
	 * 
	 * @return True if object definition available, false otherwise.
	 */
	public boolean isObjectDefinition() {
		return type != null && type.getName() != null && type.getName().equals("object");
	}
	
	public boolean isNative() {
		return type != null && type.getName() != null && !type.getName().equals("object");
	}
	
	public boolean isMultitype() {
		return type != null && type.isArray();
	}

	public String getRef() {
		return ref;
	}
	
	public boolean isRef() {
		return ref != null;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public HashMap<String, Property> getProperties() {
		return properties;
	}

	public void setProperties(HashMap<String, Property> properties) {
		this.properties = properties;
	}

	public List<String> getEnums() {
		return enums;
	}
	
	public boolean isEnum() {
		return enums != null && !enums.isEmpty();
	}

	public void setEnums(List<String> enums) {
		this.enums = enums;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public TypeWrapper getType() {
		return type;
	}

	public void setType(TypeWrapper type) {
		this.type = type;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public Type getItems() {
		return items;
	}

	public void setItems(Type items) {
		this.items = items;
	}

	public Boolean getUniqueItems() {
		return uniqueItems;
	}

	public void setUniqueItems(Boolean uniqueItems) {
		this.uniqueItems = uniqueItems;
	}

	public Integer getMinItems() {
		return minItems;
	}

	public void setMinItems(Integer minItems) {
		this.minItems = minItems;
	}

	public Integer getMinLength() {
		return minLength;
	}

	public void setMinLength(Integer minLength) {
		this.minLength = minLength;
	}

	public Integer getMinimum() {
		return minimum;
	}

	public void setMinimum(Integer minimum) {
		this.minimum = minimum;
	}

	public Integer getMaximum() {
		return maximum;
	}

	public void setMaximum(Integer maximum) {
		this.maximum = maximum;
	}

	public AdditionalPropertiesWrapper getAdditionalProperties() {
		return additionalProperties;
	}

	public void setAdditionalProperties(AdditionalPropertiesWrapper additionalProperties) {
		this.additionalProperties = additionalProperties;
	}

	public String getDefault() {
		return defaultValue;
	}

	public void setDefault(String defaultValue) {
		this.defaultValue = defaultValue;
	}
}
