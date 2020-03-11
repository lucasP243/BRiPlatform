package com.briplatform.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.util.Arrays;

import com.briplatform.server.resources.BRiService;
import com.briplatform.server.resources.Programmer;
import com.briplatform.server.resources.Registry;

/**
 * This class is the programmer service which the programmer client app
 * connects to. It provides a handful of commands to manage the programmer's
 * account and his installed services.
 * 
 * @author Lucas Pinard
 */
public class ProgService extends BRiService {

	/** Shortcut for the {@link System#lineSeparator()}. */
	private static final String nl = System.lineSeparator();

	static {
		try {
			BRiService.verifyBRiValidity(ProgService.class);
		} catch (NotBRiNormalizedException e) {
			throw new RuntimeException(e);
		}
	}

	/** Reference to the connected programmer. */
	private Programmer connected = null;

	public ProgService(Socket client) {
		super(client);
		start();
	}

	@Override
	public void run() {
		do try {
			write("Username: ");
			String username = read();
			write("Password: ");
			String password = read();

			if ((connected = Registry.getInstance()
					.getProgrammer(username, password)) == null) {
				write("Invalid username or password, please try again.");
			}
		} catch (@SuppressWarnings("unused") IOException e) {
			System.err.println("Connection ended with " + getClientAddress());
			return;
		} while (connected == null);

		// Once connected, can access commands
		write("Type help to obtain list of available command.");
		String[] answer = null;
		do try {
			write("$$NEWLINE$$>> ");
			answer = read().split(" ");
			ProgService.class.getMethod(answer[0], String[].class)
			.invoke(this, new Object[]{Arrays.copyOfRange(answer, 1, answer.length)});
		} catch (@SuppressWarnings("unused") IOException e) {
			System.err.println("Connection ended with " + connected.getUsername() + " " + getClientAddress());
			return;
		} catch (@SuppressWarnings("unused") Exception e) {
			write("unknown command");
		} while (true);
	}

	public void help(String[] args) {
		if (args == null || args.length == 0) {
			write(
					"changeftp - to change your ftp server address."+nl
					+ "add - add a service to the BRiPlatform."+nl
					+ "see - to see your services and their status."+nl
					+ "on - to activate one of your services."+nl
					+ "off - to deactivate one of your services"+nl
					+ "update - to update one of your services."+nl
					+ "rem - to remove one of your services."+nl
					+ "close - to end the connection."
					);
		} 
		else switch(args[0]) {
		case "changeftp":
			write("changeftp <url> - sets your ftp server address to url");
			return;
		case "add":
			write("add <name> <class|jar> [on|off] - adds the service to the BRiPlatform");
			return;
		case "see":
			write("see - to see your services and their status");
			return;
		case "on":
			write("on <name> - activates the specified service");
			return;
		case "off":
			write("off <name> - deactivates the specified service");
			return;
		case "update":
			write("update <name> <class|jar> [on|off] - updates the specified service");
			return;
		case "rem":
			write("rem <name> - uninstalls the specified service");
			return;
		case "close":
			write("close - to end the connection.");
			return;

		default:
			write(args[1] + " : unknown command");
		}
	}

	public void changeftp(String[] args) {
		try {
			connected.setFTPLocation(args[0]);
			write("Success");
		} catch (@SuppressWarnings("unused") 
		ArrayIndexOutOfBoundsException | MalformedURLException e) {
			write("Invalid syntax");
		}
	}

	public void add(String[] args) {
		if (args.length < 2) {
			write("Invalid syntax");
			return;
		}
		try {
			switch (args[1]) {
			case "class":
				connected.addService(args[0]);
				break;
			case "jar":
				connected.addServiceFromJAR(args[0]);
				break;
			default:
				write("Invalid syntax");
			}
		} catch (NotBRiNormalizedException e) {
			write(e.getMessage());
			return;
		} catch (@SuppressWarnings("unused") ClassNotFoundException e) {
			write("Service not found");
			return;
		} catch (@SuppressWarnings("unused") MalformedURLException e) {
			write("Invalid syntax");
			return;
		}
		
		if (args.length < 3 || args[2] != "off") {
			connected.activateService(args[0]);
		}
		write("Success");
	}

	public void see(@SuppressWarnings("unused") String[] args) {
		write(connected.getServiceList());
	}

	public void on(String[] args) {
		connected.activateService(args[0]);
		write("Success");
	}

	public void off(String[] args) {
		connected.deactivateService(args[0]);
		write("Success");
	}

	public void update(String[] args) {
		if (args.length < 2) {
			write("Invalid syntax");
		}
		connected.removeService(args[0]);
		add(args);
	}

	public void rem(String[] args) {
		connected.removeService(args[0]);
		write("Success");
	}
	
	public void close(String[] args) {
		finish();
	}

	public static String toStringue() {
		return ProgService.class.getSimpleName();
	}

}
