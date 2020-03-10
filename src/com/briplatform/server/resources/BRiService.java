package com.briplatform.server.resources;

import static java.lang.reflect.Modifier.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.Socket;

public abstract class BRiService implements Runnable {

	private Socket client;

	private BufferedReader in;

	private PrintWriter out;

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

	public final void write(String line) {
		line = line.replace(System.lineSeparator(), "$$NEWLINE$$");
		out.write(line);
	}

	public final String read() throws IOException {
		out.println();
		out.flush();
		String line = in.readLine();
		return line.replace("$$NEWLINE$$", System.lineSeparator());
	}

	public final Socket getClient() {
		return client;
	}
	
	public final String getClientAddress() {
		return String.format(
				"%s:%d", 
				client.getInetAddress(), client.getPort()
				);
	}

	public final void finish() {
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

	public static void verifyBRiIntegrity(Class<?> clazz) 
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
