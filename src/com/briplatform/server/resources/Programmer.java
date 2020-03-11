package com.briplatform.server.resources;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.briplatform.server.resources.BRiService.NotBRiNormalizedException;

/**
 * This class represents a programmer user, who provides an url to a personnal 
 * FTP server containing their services.
 * 
 * @author Lucas Pinard
 */
public class Programmer implements Serializable {

	/**
	 * Algorith to hash password.
	 */
	private static MessageDigest hash;

	static {
		try { hash = MessageDigest.getInstance("SHA-256"); } 
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Failed to init hashing algorithm.", e);
		}
	}

	/**
	 * This programmer's username. It must be unique.
	 */
	private String username;

	/**
	 * This programmer's password, hashed using SHA-256 algorithm.
	 */
	private byte[] password;

	/**
	 * URL pointing to this programmer's FTP server.
	 */
	private URL FTPLocation;

	/**
	 * Services loaded by this programmer.
	 */
	private Map<String, Class<? extends BRiService>> services;

	/**
	 * Creates a new programmer.
	 * @param username This programmer's username.
	 * @param password the {@code String} to hash as a password.
	 * @param url the {@code String} to parse as an URL.
	 * @throws MalformedURLException if no protocol is specified, or an unknown 
	 * protocol is found, or the parsed URL fails to comply with the specific 
	 * syntax of the associated protocol.
	 */
	public Programmer(String username, String password, String url) 
			throws MalformedURLException {
		this.username = username;
		this.password = hash.digest(password.getBytes(StandardCharsets.UTF_8));
		this.FTPLocation = new URL(url);
		this.services = new HashMap<>();
	}

	/**
	 * Gets this programmer's username.
	 * @return this programmer's username
	 */
	public String getUsername() {
		return this.username;
	}

	/**
	 * Gets the URL of this programmer's FTP server.
	 * @return the URL of this programmer's FTP server
	 */
	public URL getFTPLocation() {
		return this.FTPLocation;
	}

	/**
	 * Verify is the input's hash correspond to the current hashed password.
	 * @param attempt the inputted password.
	 * @return {@code true} if the hashes corresponds, {@code false} otherwise
	 */
	public boolean login(String attempt) {
		return Arrays.equals(
				this.password,
				hash.digest(attempt.getBytes(StandardCharsets.UTF_8))
				);
	}

	/**
	 * Get the service associated to the specified name.
	 * @param name name of the desired service.
	 * @return the services if it is found, {@code null} otherwise
	 */
	public Class<? extends BRiService> getService(String name) {
		return services.get(name);
	}

	public String getServiceList() {
		Iterator<String> i = services.keySet().iterator();
		StringBuilder sb = new StringBuilder(
				"Services :" + System.lineSeparator()
				);
		while (i.hasNext()) {
			String s = i.next();
			sb.append(s + " - " + (
					Registry.getInstance().getService(s) != null?
							"on":"off"
							)
					);
			if (i.hasNext()) sb.append(System.lineSeparator());
		}
		return sb.toString();
	}

	/**
	 * Sets a new username for this programmer.
	 * @param username the new username.
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Changes this programmer's password (requires the current password).
	 * @param oldPwd the current password.
	 * @param newPwd the new password to set.
	 * @return {@code true} if the password has been successfully changed, 
	 * {@code false} if the authentibcation failed
	 */
	public boolean setPassword(String oldPwd, String newPwd) {
		if (!login(oldPwd)) return false;
		this.password = hash.digest(newPwd.getBytes(StandardCharsets.UTF_8));
		return true;
	}

	/**
	 * Changes the URL to this programmer's FTP server.
	 * @param url the {@code String} to parse as an URL
	 * @throws MalformedURLException if no protocol is specified, or an unknown 
	 * protocol is found, or the parsed URL fails to comply with the specific 
	 * syntax of the associated protocol.
	 */
	public void setFTPLocation(String url) 
			throws MalformedURLException {
		this.FTPLocation = new URL(url);
	}

	@SuppressWarnings("unchecked")
	/**
	 * Tries to add the service to this programmer's installed services from
	 * his FTP directory.
	 * @param name the name of the service to add
	 * @throws ClassNotFoundException if no service can be found.
	 * @throws NotBRiNormalizedException if the service is does not respect the
	 * BRiPlatform standard.
	 */
	public void addService(String name) 
			throws ClassNotFoundException, NotBRiNormalizedException {
		URLClassLoader loader = new URLClassLoader(new URL[] {FTPLocation});
		Class<?> service = loader.loadClass(username+"."+name);
		BRiService.verifyBRiValidity(service);
		services.put(name, (Class<? extends BRiService>) service);
		try {loader.close();} catch (IOException e) {e.printStackTrace();}
	}

	@SuppressWarnings("unchecked")
	/**
	 * Tries to add the service to this programmer's installed services from a
	 * JAR file of same name located in this programmer's FTP directory.
	 * @param name the name of the service to add
	 * @throws ClassNotFoundException if no service can be found.
	 * @throws NotBRiNormalizedException if the service is does not respect the
	 * BRiPlatform standard.
	 */
	public void addServiceFromJAR(String name) 
			throws ClassNotFoundException, NotBRiNormalizedException, 
			MalformedURLException {
		URLClassLoader loader = new URLClassLoader(
				new URL[] {new URL(FTPLocation, name+".jar")}
				);
		Class<?> service = loader.loadClass(username+"."+name);
		BRiService.verifyBRiValidity(service);
		services.put(name, (Class<? extends BRiService>) service);
		try {loader.close();} catch (IOException e) {e.printStackTrace();}
	}

	/**
	 * Uninstalls a service from this programmer's list of services.
	 * @param name the name of the service to remove.
	 */
	public void removeService(String name) {
		deactivateService(name);
		services.remove(name);
	}

	/**
	 * Activate a service by pushing it into the BRiPlatform registry.
	 * @param name the name of the service to activate.
	 * @see Registry
	 */
	public void activateService(String name) {
		Registry.getInstance().addService(services.get(name));
	}

	/**
	 * Deactivate a service by removing it from the BRiPlatform registry.
	 * @param name the name of the service to remove.
	 * @see Registry
	 */
	public void deactivateService(String name) {
		Registry.getInstance().removeService(services.get(name));
	}
}
