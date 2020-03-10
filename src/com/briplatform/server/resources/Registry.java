 package com.briplatform.server.resources;

import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Registry {

	private static Registry instance = new Registry();

	public static Registry getInstance() {
		return Registry.instance;
	}

	private Map<String, Class<? extends BRiService>> services;

	private Map<String, Programmer> programmers;

	private Registry() {
		this.services = new ConcurrentHashMap<>();
		this.programmers = new ConcurrentHashMap<>();
	}

	public Class<? extends BRiService> getService(String name) {
		return services.get(name);
	}

	public String getServiceList() {
		Iterator<String> i = services.keySet().iterator();
		StringBuilder sb = new StringBuilder(
				"Available services :" + System.lineSeparator()
				);
		while (i.hasNext()) {
			sb.append(i.next());
			if (i.hasNext()) sb.append(System.lineSeparator());
		}
		return sb.toString();
	}
	
	public void addService(Class<? extends BRiService> service) {
		services.put(service.getSimpleName(), service);
	}
	
	public void removeService(Class<? extends BRiService> service) {
		services.remove(service.getSimpleName());
	}

	public Programmer getProgrammer(String name, String password) {
		Programmer p = programmers.get(name);
		if (p != null && p.login(password)) return p;
		return null;
	}
	
	public boolean addProgrammer(String username, String password, String url) throws MalformedURLException {
		if (programmers.get(username) != null) return false;
		programmers.put(username, new Programmer(username, password, url));
		return true;
	}
}
