package ch.mse.riddles;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class App {

	public static void initRMI() {
		try {
			Registry r = LocateRegistry.getRegistry(1099);
			Arrays.stream(r.list()).forEach(System.out::println);
			System.out.println("You are not the master");
		} catch (Exception e) {
			try {
				LocateRegistry.createRegistry(1099);
				System.out.println("You are the master do not disconnect");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		initRMI();
		User user;
		while (true) {
			try {
				System.out.println("Enter your name:");
				user = new User(scanner.nextLine());
				break;
			} catch (NameAlreadyTakenException e) {
				System.out.println("Name already taken");
			}
		}
		while (true) {
			System.out.println("Select an action:");
			System.out.println("[0] Add contact");
			System.out.println("[1] Send riddle");
			System.out.println("[2] List riddles");
			System.out.println("[3] Answer riddle");
			System.out.println("[4] List sent riddles");
			System.out.println("[5] Exit");
			String input = scanner.nextLine();
			String name = null;
			switch (input) {
				case "0":
					System.out.println("Who would you like to add as contact?");
					List<String> users = user.listOthersUsers();
					name = null;
					System.out.println("You can add:");
					users.forEach(System.out::println);
					do {
						name = scanner.nextLine();
					} while (!users.contains(name));
					user.addContact(name);
					System.out.println("Contact added");
					break;
				case "1":
					System.out.println("Who do you want to send the riddle to? (Enter the name)");
					List<String> contacts = user.listContacts();
					System.out.println("You can send to:");
					contacts.forEach(System.out::println);

					name = null;
					do {
						name = scanner.nextLine();
					} while (!contacts.contains(name));

					System.out.println("Enter the question:");
					String question = scanner.nextLine();
					System.out.println("Enter the timeout in seconds:");
					int timeout = Integer.parseInt(scanner.nextLine());
					System.out.println("Is it important? (y/n)");
					boolean important = scanner.nextLine().equals("y");
					try {
						user.createRiddle(question, name, new Timestamp(timeout * 1000), important);
					} catch (RemoteException e) {
						System.out.println("Error: " + e.getMessage());
					}
					System.out.println("Riddle sent");
					break;
				case "2":
					System.out.println("Listing riddles");
					try {
						user.listRiddles().forEach(System.out::println);
					} catch (RemoteException e) {
						System.out.println("Error: " + e.getMessage());
					}
					break;
				case "3":
					System.out.println("Which riddle would you like to answer?");
					List<String> riddles = null;
					try {
						riddles = user.listRiddles();
					} catch (RemoteException e) {
						System.out.println("Error: " + e.getMessage());
						break;
					}
					for (int i = 0; i < riddles.size(); i++) {
						System.out.println("[" + i + "] " + riddles.get(i));
					}
					int index = Integer.parseInt(scanner.nextLine());
					System.out.println("Enter your answer:");
					String answer = scanner.nextLine();
					user.answerRiddle(index, answer);
					System.out.println("Answer sent");
					break;
				case "4":
					System.out.println("Listing sent riddles");
					user.listRiddlesSent().forEach(System.out::println);
					break;
				case "5":
					System.out.println("Exiting");
					scanner.close();
					System.exit(0);
					break;
				default:
					System.out.println("Invalid input");
					break;
			}
		}
	}
}
