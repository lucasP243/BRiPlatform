package com.briplatform.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.briplatform.server.resources.BRiService;

public class ConnectionListener implements Runnable {

	private ServerSocket skt;

	private Class<? extends BRiService> bindedService;
	
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
