package com.briplatform.server.resources;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * This class represents a programmer user, who provides an url to a personnal 
 * FTP server containing their services.
 * 
 * @author Lucas Pinard
 * 
 * @version 1.0
 *
 */
class Programmer {

	/**
	 * Static value to automatically attribute IDs.
	 */
	private static int AUTOINCR;

	/**
	 * Algorith to hash password.
	 */
	private static MessageDigest hash;

	static {
		AUTOINCR = 0;
		try { hash = MessageDigest.getInstance("SHA-256"); } 
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Failed to init hashing algorithm.", e);
		}
	}

	/**
	 * This programmer's ID. It is unique.
	 */
	private final int ID;

	/**
	 * This programmer's username.
	 */
	private String username;

	/**
	 * This programmer's password, hashed using SHA-256 algorithm.
	 */
	private byte[] password;

	/**
	 * URL pointing to this programmer's FTP server.
	 */
	private URL servicesLocation;

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
		synchronized (this.getClass()) {
			this.ID = AUTOINCR++;
		}
		this.username = username;
		this.password = hash.digest(password.getBytes(StandardCharsets.UTF_8));
		this.servicesLocation = new URL(url);
	}

	/**
	 * Gets this programmer's ID.
	 * @return this programmer's ID
	 */
	public int getID() {
		return this.ID;
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
	public URL getServicesLocation() {
		return this.servicesLocation;
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
	 * {@code false} if the authentification failed
	 */
	public boolean setPassword(String oldPwd, String newPwd) {
		if (!login(oldPwd)) return false;
		this.password = hash.digest(newPwd.getBytes(StandardCharsets.UTF_8));
		return true;
	}

	/**
	 * Changes the URL to this programmer's FTP server.
	 * @param servicesLocation the {@code String} to parse as an URL
	 * @throws MalformedURLException if no protocol is specified, or an unknown 
	 * protocol is found, or the parsed URL fails to comply with the specific 
	 * syntax of the associated protocol.
	 */
	public void setServicesLocation(String servicesLocation) 
			throws MalformedURLException {
		this.servicesLocation = new URL(servicesLocation);
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
}
