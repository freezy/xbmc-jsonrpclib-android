package org.xbmc.android.jsonrpc.generator.introspect;

import java.util.HashMap;

public class Result {
	
	private String id;
	private String description;
	private HashMap<String, Method> methods;
	private HashMap<String, Notification> notifications;
	private HashMap<String, Type> types;
	private Integer version;
	
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
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	
}
