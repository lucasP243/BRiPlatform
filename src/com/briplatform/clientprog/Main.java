package com.briplatform.clientprog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		Socket server = null;
		Scanner r = null;
		try {
			server = new Socket("localhost", 7500);
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
