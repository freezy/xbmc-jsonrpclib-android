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

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.ExtendsWrapper;

/**
 * A global type as defined in introspect's <tt>types</tt> collection.
 * 
 * Only two additional attributes compared to {@link Property}:
 * {@link Type#getId()} and {@link Type#getExtends()}.
 * 
 * @see http://tools.ietf.org/html/draft-zyp-json-schema-03
 * @author freezy <freezy@xbmc.org>
 */
@JsonIgnoreProperties({ "default" })
public class Type extends Property {

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

	private String id;

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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String toString() {
		return type.toString();
	}

}
