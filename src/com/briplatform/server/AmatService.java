package com.briplatform.server;

import java.io.IOException;
import java.net.Socket;

import com.briplatform.server.resources.BRiService;
import com.briplatform.server.resources.Registry;

/**
 * This class is the amateur service which the programmer client app
 * connects to.
 * 
 * @author Lucas Pinard
 */
public class AmatService extends BRiService {

	public AmatService(Socket client) {
		super(client);
		start();
	}

	@Override
	public void run() {
		try {
			write(Registry.getInstance().getServiceList());
			String line = read();
			Class<? extends BRiService> service
				= Registry.getInstance().getService(line);
			
			if (service == null) {
				write("Service not found");
				finish();
				return;
			}
			
			BRiService s = service.getConstructor(Socket.class)
					.newInstance(getClient());
			s.start();
		} catch (@SuppressWarnings("unused") IOException e) {
			System.err.println("Connection ended with " + getClientAddress());
		} catch (Exception e) {
			throw new RuntimeException("Error while service instanciation", e);
		}
	}
	
	public static String toStringue() {
		return AmatService.class.getSimpleName();
	}

}
