package com.briplatform.clientprog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * The clientprog.Main class is a basic client which reads from a socket and 
 * writes an answer until the connection is ended by the server or the client
 * is closed. It connects on {@link #IPv4} to {@link #PORT}.
 * 
 * @author Lucas Pinard
 */
public class Main {
	
	/** IPv4 address where this clients attemps to connect. */
	private static final String IPv4 = "localhost";
	
	/** Connection port. */
	private static final int PORT = 7500;

	public static void main(String[] args) {
		Socket server = null;
		Scanner r = null;
		try {
			server = new Socket(IPv4, PORT);
			BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
			PrintWriter out = new PrintWriter(server.getOutputStream(), true);
			r = new Scanner(System.in);
			do {
				System.out.print(in.readLine().replace("$$NEWLINE$$", System.lineSeparator()));
				out.println(r.nextLine().replace(System.lineSeparator(), "$$NEWLINE$$"));
			} while (true);
		} catch (@SuppressWarnings("unused") NullPointerException | IOException e) {
			System.err.println("Connection ended");
		} finally {
			try {
				r.close();
				server.close();
			} catch (@SuppressWarnings("unused") Exception e) {}
		}
	}

}
