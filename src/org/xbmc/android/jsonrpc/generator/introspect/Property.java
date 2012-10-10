/*
 *      Copyright (C) 2005-2012 Team XBMC
 *      http://xbmc.org
 *
 *  This Program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2, or (at your option)
 *  any later version.
 *
 *  This Program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XBMC Remote; see the file license.  If not, write to
 *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *  http://www.gnu.org/copyleft/gpl.html
 *
 */
package org.xbmc.android.jsonrpc.generator.introspect;

import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;
import org.xbmc.android.jsonrpc.generator.Introspect;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.AdditionalPropertiesWrapper;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.ExtendsWrapper;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.TypeWrapper;

/**
 * A Property defines an attribute of a type and is also the base class
 * for {@link Param} and {@link Type}.
 * <p/>
 * A property is used in the following classes:
 * <ul><li>{@link Type#properties} - As a list of properties of an object.</li>
 *     <li>{@link Result#types} - As a global type extending Property.</li>
 *     <li>{@link Method#params} - As a method parameter type extending Property.</li>
 *     <li>{@link Property#items} - As an array type.</li>
 * </ul>
 * 
 * All of its members can be found in the JSON-schema draft (see link below).
 * 
 * @see http://tools.ietf.org/html/draft-zyp-json-schema-03
 * @author freezy <freezy@xbmc.org>
 */
public class Property {

	/**
	 * This attribute defines a URI of a schema that contains the full
	 * representation of this schema.  When a validator encounters this
	 * attribute, it SHOULD replace the current schema with the schema
	 * referenced by the value's URI (if known and available) and re-
	 * validate the instance.  This URI MAY be relative or absolute, and
	 * relative URIs SHOULD be resolved against the URI of the current
	 * schema.
	 */
	@JsonProperty("$ref")
	protected String ref;
	
	/**
	 * The value of this property MUST be another schema which will provide
	 * a base schema which the current schema will inherit from.  The
	 * inheritance rules are such that any instance that is valid according
	 * to the current schema MUST be valid according to the referenced
	 * schema.  This MAY also be an array, in which case, the instance MUST
	 * be valid for all the schemas in the array.  A schema that extends
	 * another schema MAY define additional attributes, constrain existing
	 * attributes, or add other constraints.<p/>
	 * 
	 * Conceptually, the behavior of extends can be seen as validating an
	 * instance against all constraints in the extending schema as well as
	 * the extended schema(s).  More optimized implementations that merge
	 * schemas are possible, but are not required.  An example of using
	 * "extends":<p/>
	 * 
	 * <code><pre>
	 *    {
	 *       "description":"An adult",
	 *       "properties":{"age":{"minimum": 21}},
	 *       "extends":"person"
	 *     }
	 *     
	 *     {
	 *       "description":"Extended schema",
	 *       "properties":{"deprecated":{"type": "boolean"}},
	 *       "extends":"http://json-schema.org/draft-03/schema"
	 *     }
	 * </pre></code>
	 */
	@JsonProperty("extends")
	private ExtendsWrapper extendsValue;

	/**
	 * This attribute is an object with property definitions that define the
	 * valid values of instance object property values.  When the instance
	 * value is an object, the property values of the instance object MUST
	 * conform to the property definitions in this object.  In this object,
	 * each property definition's value MUST be a schema, and the property's
	 * name MUST be the name of the instance property that it defines.  The
	 * instance property value MUST be valid according to the schema from
	 * the property definition.  Properties are considered unordered, the
	 * order of the instance properties MAY be in any order.
	 */
	protected HashMap<String, Property> properties;
	
	/**
	 *  This provides an enumeration of all possible values that are valid
	 *  for the instance property.  This MUST be an array, and each item in
	 *  the array represents a possible value for the instance value.  If
	 *  this attribute is defined, the instance value MUST be one of the
	 *  values in the array in order for the schema to be valid.  Comparison
	 *  of enum values uses the same algorithm as defined in "uniqueItems"
	 *  (Section 5.15).
	 */
	protected List<String> enums;

	/**
	 * This attribute is a string that provides a full description of the of
	 * purpose the instance property.
	 */
	protected String description;
	
	/**
	 * This attribute defines what the primitive type or the schema of the
	 * instance MUST be in order to validate.  This attribute can take one
	 * of two forms:<p/>
	 * 
	 * <b>Simple Types</b>: A string indicating a primitive or simple type. The
	 * following are acceptable string values:
	 * <ul><li><tt>string</tt> Value MUST be a string.</li>
	 *     <li><tt>number</tt> Value MUST be a number, floating point numbers are
	 *             allowed.</li>
	 *     <li><tt>integer</tt> Value MUST be an integer, no floating point numbers are
                   allowed. This is a subset of the number type.</li>
	 *     <li><tt>boolean</tt> Value MUST be a boolean.</li>
	 *     <li><tt>object</tt> Value MUST be an object.</li>
	 *     <li><tt>array</tt> Value MUST be an array.</li>
	 *     <li><tt>null</tt> Value MUST be null. Note this is mainly for purpose of
	 *             being able use union types to define nullability.  If this type
	 *             is not included in a union, null values are not allowed (the
	 *             primitives listed above do not allow nulls on their own).</li>
	 *     <li><tt>any</tt> Value MAY be of any type including null.</li></ul>
	 *     
	 * If the property is not defined or is not in this list, then any
	 * type of value is acceptable.  Other type values MAY be used for
	 * custom purposes, but minimal validators of the specification
	 * implementation can allow any instance value on unknown type
	 * values.<p/>
	 * 
	 * <b>Union Types</b>: An array of two or more simple type definitions. Each
	 * item in the array MUST be a simple type definition or a schema.
	 * The instance value is valid if it is of the same type as one of
	 * the simple type definitions, or valid by one of the schemas, in
	 * the array.<br>
	 * 
	 * For example, a schema that defines if an instance can be a string or
	 * a number would be:<br>
	 * <code><tt>{"type":["string","number"]}</tt></code>
	 */
	protected TypeWrapper type;
	
	/**
	 * This attribute indicates if the instance must have a value, and not
	 * be undefined.  This is false by default, making the instance
	 * optional.
	 */
	protected Boolean required;

	/**
	 * This attribute defines the allowed items in an instance array, and
	 *  MUST be a schema or an array of schemas.  The default value is an
	 *  empty schema which allows any value for items in the instance array.
	 *  <br>
	 *  When this attribute value is a schema and the instance value is an
	 *  array, then all the items in the array MUST be valid according to the
	 *  schema.
	 *  <br>
	 *  When this attribute value is an array of schemas and the instance
	 *  value is an array, each position in the instance array MUST conform
	 *  to the schema in the corresponding position for this array.  This
	 *  called tuple typing.  When tuple typing is used, additional items are
	 *  allowed, disallowed, or constrained by the "additionalItems"
	 *  (Section 5.6) attribute using the same rules as
	 *  "additionalProperties" (Section 5.4) for objects.
	 */
	protected Property items;
	
	/**
	 * This attribute indicates that all items in an array instance MUST be
	 * unique (contains no two identical values).
	 * <br>
	 * Two instance are consider equal if they are both of the same type
	 * and:
	 * <ul><li>are null; or</li>
	 *     <li>are booleans/numbers/strings and have the same value; or</li>
	 *     <li>are arrays, contains the same number of items, and each item in
	 *         the array is equal to the corresponding item in the other array;
	 *         or</li>
	 *     <li>are objects, contains the same property names, and each property
	 *         in the object is equal to the corresponding property in the other
	 *         object.</li></ul>
	 */
	protected Boolean uniqueItems;
	
	/**
	 * This attribute defines the minimum number of values in an array when
	 * the array is the instance value.
	 */
	protected Integer minItems;

	/**
	 * When the instance value is a string, this defines the minimum length
	 * of the string.
	 */
	protected Integer minLength;
	
	/**
	 * This attribute defines the minimum value of the instance property
	 * when the type of the instance value is a number.
	 */
	protected Integer minimum;
	
	/**
	 * This attribute defines the maximum value of the instance property
	 * when the type of the instance value is a number.
	 */
	protected Integer maximum;

	/**
	 * This attribute defines a schema for all properties that are not
	 * explicitly defined in an object type definition.  If specified, the
	 * value MUST be a schema or a boolean.  If false is provided, no
	 * additional properties are allowed beyond the properties defined in
	 * the schema.  The default value is an empty schema which allows any
	 * value for additional properties.
	 */
	protected AdditionalPropertiesWrapper additionalProperties;

	/**
	 * This attribute defines the default value of the instance when the
	 * instance is undefined.
	 */
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
		return type != null && type.getName() != null
				&& !type.getName().equals("object")
				&& !type.getName().equals("array");
	}
	
	public boolean isMultitype() {
		return type != null && type.isList();
	}
	
	public boolean isArray() {
		return type != null && type.getName() != null && type.getName().equals("array");
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
		return (enums != null && !enums.isEmpty());
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

	public Property getItems() {
		return items;
	}

	public void setItems(Property items) {
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
	
	/**
	 * Returns true if this type extends another type, false otherwise.
	 * @return True if extends, false otherwise.
	 */
	public boolean doesExtend() {
		return extendsValue != null;
	}
	
	public ExtendsWrapper getExtends() {
		return extendsValue;
	}

	public void setExtends(ExtendsWrapper extendsValue) {
		this.extendsValue = extendsValue;
	}
	
	private void copyTo(Property dest) {
		// firstly copy attributes from parent(s)
		if (extendsValue != null) {
			// if multiple, copy from each
			if (extendsValue.isList()) {
				for (String e : extendsValue.getList()) {
					Introspect.find(e).copyTo(dest);
				}
			} else {
				Introspect.find(extendsValue.getName()).copyTo(dest);
			}
		}
		// now copy all attributes from here
		if (additionalProperties != null) {
			dest.setAdditionalProperties(additionalProperties);
		}
		if (defaultValue != null) {
			dest.setDefault(defaultValue);
		}
		if (description != null) {
			dest.setDescription(description);
		}
		if (enums != null) {
			dest.setEnums(enums);
		}
		if (items != null) {
			dest.setItems(items);
		}
		if (maximum != null) {
			dest.setMaximum(maximum);
		}
		if (minimum != null) {
			dest.setMinimum(minimum);
		}
		if (minItems != null) {
			dest.setMinItems(minItems);
		}
		if (minLength != null) {
			dest.setMinLength(minLength);
		}
		if (required != null) {
			dest.setRequired(required);
		}
		if (type != null) {
			dest.setType(type);
		}
		if (uniqueItems != null) {
			dest.setUniqueItems(uniqueItems);
		}
	}
	
	/**
	 * Traverses all parents and copies all attributes. A new object is then
	 * returned, not a reference.
	 * 
	 * @return Copy this object, with all attributes copied from parents and
	 *         references.
	 */
	public Property obj() {
		final Property property = new Property();
		Introspect.find(this).copyTo(property);
		return property;
	}
}
