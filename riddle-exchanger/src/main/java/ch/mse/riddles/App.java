package ch.mse.riddles;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Timestamp;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter 's' to start the server, 'c' to start the client");
		String input = scanner.next();
		try {
			LocateRegistry.getRegistry(1099);
		} catch (Exception e) {
			try {
				LocateRegistry.createRegistry(1099);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		if (input.equals("s")) {
			sender();
		} else if (input.equals("c")) {
			receiver();
		}
		scanner.close();
	}

	public static void sender() {
		System.out.println("Sender");
		Riddle r = new Riddle("What is the answer to life, the universe and everything?", new Timestamp(50 * 1000));
		System.out.println("Created riddle");

	}

	public static void receiver() {
		System.out.println("Receiver");
		try {
			Registry registry = LocateRegistry.getRegistry(1099);
			String[] ll = registry.list();
			for (String s : ll) {
				System.out.println(s);
			}
			Remote stub = registry.lookup("Hello");
			RiddleRMI r = (RiddleRMI) stub;
			System.out.println("Riddle: " + r.getQuestion());
			r.answer("42");
		} catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		}
	}
}
