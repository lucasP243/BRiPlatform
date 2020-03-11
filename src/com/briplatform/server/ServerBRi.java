package com.briplatform.server;

import java.net.MalformedURLException;

import com.briplatform.server.resources.Registry;

/**
 * The class ServerBRi is a non-instantiable class which is the entry point of the 
 * server application. It creates server sockets for both client application, 
 * and injects a Programmer into the Registry as test data.
 */
public class ServerBRi {
	
	/** Forbid access to the ServerBRi default constructor. */
	private ServerBRi() {}
	
	/** Port on which the programmer clients shall connect to. */
	private static final int PORT_PROG = 7500;
	
	/** Port on which the amateur clients shall connect to. */
	private static final int PORT_AMAT = 7600;

	public static void init() {
		new ConnectionListener(PORT_PROG, ProgService.class);
		new ConnectionListener(PORT_AMAT, AmatService.class);
	}
	
	public static void main(String[] args) throws MalformedURLException {
		Registry.getInstance().addProgrammer("toto", "toto", "ftp://localhost:2121/classes/");
		init(); 
	}
}
