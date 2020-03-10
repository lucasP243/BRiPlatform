package com.briplatform.server;

import java.net.MalformedURLException;

import com.briplatform.server.resources.Registry;

public class ServerBRi {
	
	private ServerBRi() {}
	
	private static final int PORT_PROG = 7500;
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
