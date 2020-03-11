 package com.briplatform.server.resources;

import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The class Registry is a monoinstance class that holds information about
 * registered services and programmers.
 * 
 * @author Lucas Pinard
 */
public class Registry {

	/** The single instance of this class. */
	private static Registry instance = new Registry();

	/**
	 * Gets the single instance of this class.
	 * @return the single instance of this class.
	 */
	public static Registry getInstance() {
		return Registry.instance;
	}

	/** <i>Thread-safe. </i>Maps the installed services to their name. */
	private Map<String, Class<? extends BRiService>> services;

	/** <i>Thread-safe. </i>Maps the registered services to their username. */
	private Map<String, Programmer> programmers;

	/** Private constructor to prevent instantitation. */
	private Registry() {
		this.services = new ConcurrentHashMap<>();
		this.programmers = new ConcurrentHashMap<>();
	}

	/**
	 * Gets a service, given its name.
	 * @param name the name of the service.
	 * @return the service associated to this name if found, or 
	 * {@code null} otherwise
	 */
	public Class<? extends BRiService> getService(String name) {
		return services.get(name);
	}

	/**
	 * Builds a String containing a list of available services.
	 * @return the built String
	 */
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
	
	/**
	 * Adds a service to the BRiPlatform registry.
	 * @param service the service to add.
	 */
	public void addService(Class<? extends BRiService> service) {
		services.put(service.getSimpleName(), service);
	}
	
	/**
	 * Removes a service from the BRiPlatform registry.
	 * @param service the service to remove.
	 */
	public void removeService(Class<? extends BRiService> service) {
		services.remove(service.getSimpleName());
	}

	/**
	 * Gets a programmer given its username and its password.
	 * @param name the username of the programmer.
	 * @param password the password of the programmer.
	 * @return the programmer if found AND if login succeed, 
	 * {@code null} otherwise.
	 */
	public Programmer getProgrammer(String name, String password) {
		Programmer p = programmers.get(name);
		if (p != null && p.login(password)) return p;
		return null;
	}
	
	/**
	 * Adds a programmer to the BRiPlatform registry.
	 * @param username the username of the programmer.
	 * @param password the password of the programmer.
	 * @param url url pointing to the FTP server of the programmer.
	 * @return {@code true} if creation suceed, {@code false} otherwise.
	 * @throws MalformedURLException if no protocol is specified, or an unknown 
	 * protocol is found, or the parsed URL fails to comply with the specific 
	 * syntax of the associated protocol.
	 */
	public boolean addProgrammer(String username, String password, String url) throws MalformedURLException {
		if (programmers.get(username) != null) return false;
		programmers.put(username, new Programmer(username, password, url));
		return true;
	}
}
