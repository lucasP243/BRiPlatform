package com.briplatform.server.resources;

import static java.lang.reflect.Modifier.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * The BRiService class is the base class of any service. A service can't be
 * added to the platform unless it extends this class. It also provides methods
 * to abstractly manage communication between the server-side socket and the
 * client-side socket. Additionally, it provides a static method to check if
 * a class is a valid BRiService.
 * 
 * @see #verifyBRiValidity(Class)
 * 
 * @author Lucas Pinard
 */
public abstract class BRiService implements Runnable {

	/** Server-side socket connected to the client. */
	private Socket client;

	/** Reader extracted from the {@link #client} socket. */
	private BufferedReader in;

	/** Writer extracted from the {@link #client} socket. */
	private PrintWriter out;

	/**
	 * Constructs a new service using the given socket.
	 * @param client the socket this service shall use.
	 */
	public BRiService(Socket client) {
		this.client = client;

		try {
			this.in = new BufferedReader(
					new InputStreamReader(client.getInputStream())
					);
			this.out = new PrintWriter(
					client.getOutputStream(), false
					);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public abstract void run();

	public void start() {
		new Thread(this).start();
	}

	/**
	 * Appends a new line to be sent to the client. The lines will be sent the
	 * next time the {@code read()} method is called.
	 * @param line the line to append.
	 */
	protected final void write(String line) {
		line = line.replace(System.lineSeparator(), "$$NEWLINE$$");
		out.write(line);
	}

	/**
	 * Sends all the waiting messages and reads the client answer.
	 * @return the client answer.
	 * @throws IOException if the socket is closed in the meantime.
	 */
	protected final String read() throws IOException {
		out.println();
		out.flush();
		String line = in.readLine();
		return line.replace("$$NEWLINE$$", System.lineSeparator());
	}

	/**
	 * Getter to the client socket.
	 * @return the client socket.
	 */
	protected final Socket getClient() {
		return client;
	}
	
	/**
	 * Gets the string representation of the client socket address.
	 * @return the string representation of the client socket address.
	 */
	protected final String getClientAddress() {
		return String.format(
				"%s:%d", 
				client.getInetAddress(), client.getPort()
				);
	}

	/**
	 * Closes the resources and end the connection.
	 */
	protected final void finish() {
		out.flush();
		try {
			in.close();
			out.close();
			client.close();
		} catch (@SuppressWarnings("unused") IOException e) {
			System.err.println("Connection ended.");
		}
	}
	
	@Override
	protected void finalize() {
		finish();
	}

	/**
	 * Verify if a given class respects the BRi standard.
	 * @param clazz the class to verify.
	 * @throws NotBRiNormalizedException if the class does not respects the BRi
	 * standard, with further information in the exception message
	 */
	public static void verifyBRiValidity(Class<?> clazz) 
			throws NotBRiNormalizedException {

		Class<?> sClazz = clazz;
		do {
			sClazz = sClazz.getSuperclass();
			if (sClazz == null || sClazz.equals(Object.class)) {
				throw new NotBRiNormalizedException(
						"The class should extends BRiService."
						);
			}
		} while (! sClazz.equals(BRiService.class));

		int mod = clazz.getModifiers();
		if (isAbstract(mod) || ! isPublic(mod)) {
			throw new NotBRiNormalizedException(
					"The class should be declared public and non-abstract."
					);
		}

		try {
			Constructor<?> c = clazz.getConstructor(Socket.class);
			if (!isPublic(c.getModifiers()) || c.getExceptionTypes().length>0) {
				throw new Exception(); // jump to catch clause
			}
		} catch (@SuppressWarnings("unused") Exception e) {
			throw new NotBRiNormalizedException(
					"The class should provide a public contructor(Socket)"
							+ " with no throws."
					);
		}

		try {
			Method m = clazz.getDeclaredMethod("toStringue");
			if (!(isPublic(m.getModifiers()) && isStatic(m.getModifiers())
					&& m.getReturnType().equals(String.class)
					&& m.getExceptionTypes().length == 0
					)) {
				throw new Exception(); // jump to catch clause
			}
		} catch (@SuppressWarnings("unused") Exception e) {
			throw new NotBRiNormalizedException(
					"The class should provide a public static String "
							+ "toStringue() method with no throws."
					);
		}
	}

	public static class NotBRiNormalizedException extends Exception {
		public NotBRiNormalizedException(String message) {
			super(message);
		}
	}

}
