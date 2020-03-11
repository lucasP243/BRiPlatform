package com.briplatform.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.briplatform.server.resources.BRiService;

/**
 * The ConnectionListener class opens a ServerSocket on a given port and
 * initiates a given service for each connection.
 * 
 * @author Lucas Pinard
 */
public class ConnectionListener implements Runnable {

	/** The ServerSocket listening. */
	private ServerSocket skt;

	/** The service to initiate for each connection. */
	private Class<? extends BRiService> bindedService;
	
	/**
	 * Constructs a new ConnectionListener listening on given port and 
	 * binded to given service and runs it in a new Thread.
	 * @param port the port to listen to.
	 * @param bind  the service to initiate for each connection.
	 */
	public ConnectionListener(int port, Class<? extends BRiService> bind) {
		try {
			this.skt = new ServerSocket(port);
		} catch (IOException e) {
			throw new RuntimeException("Failed to init serversocket", e);
		}
		
		this.bindedService = bind;
		
		new Thread(this).start();
	}

	@Override
	public void run() {
		do try {
			Socket client = skt.accept();
			bindedService.getConstructor(Socket.class).newInstance(client);
		} catch (IOException e) {
			System.err.println(String.format(
					"ServerSocket failed to accept client on port %i.\n%s",
					skt.getLocalPort(),
					e.getMessage()
					));
		} catch (Exception e) {
			e.printStackTrace();
		} while (true);
	}

}
