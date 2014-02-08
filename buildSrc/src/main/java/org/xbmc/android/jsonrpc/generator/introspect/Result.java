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

/**
 * A result, as used as <tt>result</tt> node of the JSON introspect
 * tree.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class Result {
	
	private String id;
	private String description;
	private HashMap<String, Method> methods;
	private HashMap<String, Notification> notifications;
	private HashMap<String, Type> types;
	private String version;
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public HashMap<String, Method> getMethods() {
		return methods;
	}
	public void setMethods(HashMap<String, Method> methods) {
		this.methods = methods;
	}
	public HashMap<String, Notification> getNotifications() {
		return notifications;
	}
	public void setNotifications(HashMap<String, Notification> notifications) {
		this.notifications = notifications;
	}
	public HashMap<String, Type> getTypes() {
		return types;
	}
	public void setTypes(HashMap<String, Type> types) {
		this.types = types;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
}
