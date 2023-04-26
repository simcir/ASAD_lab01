package ch.mse.riddles;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

interface UserRMI extends Remote {
	public String getName() throws RemoteException;

	public void addRiddle(RiddleRMI r) throws RemoteException;
}

class User implements UserRMI {

	private static boolean isNameTaken(String name) {
		try {
			Registry registry = LocateRegistry.getRegistry(1099);
			registry.lookup(name);
			return true;
		} catch (NotBoundException e) {
			return false;
		} catch (Exception e) {
			System.err.println("Couldn't lookup name: " + e.toString());
			e.printStackTrace();
			return false;
		}
	}

	private static void postUser(UserRMI u) {
		try {
			Remote stub = (Remote) UnicastRemoteObject.exportObject(u, 0);
			Registry registry = LocateRegistry.getRegistry(1099);
			registry.bind("user-" + u.getName(), stub);
			System.out.println("User created");
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

	public List<String> listOthersUsers() {
		try {
			Registry registry = LocateRegistry.getRegistry(1099);
			return Arrays.stream(registry.list()).filter(s -> s.startsWith("user-") && !s.equals("user-" + this.name))
					.map(s -> s.substring(5)).collect(Collectors.toList());
		} catch (Exception e) {
			System.err.println("Error listing users: " + e.toString());
			e.printStackTrace();
			return null;
		}
	}

	private static UserRMI getUser(String name) {
		try {
			Registry registry = LocateRegistry.getRegistry(1099);
			return (UserRMI) registry.lookup("user-" + name);
		} catch (Exception e) {
			System.err.println("Can't get user: " + e.toString());
			e.printStackTrace();
			return null;
		}
	}

	private static final String ERROR_NAME = "error getting name";
	private String name;
	private List<UserRMI> contacts;
	private List<Riddle> riddlesSent;
	private List<RiddleRMI> riddlesReceived;

	public User(String name) throws NameAlreadyTakenException {
		if (isNameTaken(name)) {
			throw new NameAlreadyTakenException(name);
		}
		this.name = name;
		this.riddlesSent = new ArrayList<>();
		this.riddlesReceived = new ArrayList<>();
		this.contacts = new ArrayList<>();
		postUser(this);
	}

	public void createRiddle(String question, String receiver, Timestamp responseTime) throws RemoteException {
		Riddle r = new Riddle(question, responseTime);
		riddlesSent.add(r);
		getContact(receiver).addRiddle(r);
	}

	public String getName() throws RemoteException {
		return name;
	}

	public void addRiddle(RiddleRMI r) throws RemoteException {
		riddlesReceived.add(r);
	}

	public void answerRiddle(int riddleIndex, String answer) {
		try {
			RiddleRMI r = riddlesReceived.get(riddleIndex);
			r.answer(answer);
			riddlesReceived.remove(r);
		} catch (Exception e) {
			System.err.println("Couldn't answer riddle: " + e.toString());
			e.printStackTrace();
		}
	}

	public List<String> listRiddlesSent() {
		return riddlesSent.stream().map(r -> r.toString()).collect(Collectors.toList());
	}

	public List<String> listContacts() {
		return contacts.stream().map(u -> {
			try {
				return u.getName();
			} catch (RemoteException e) {
				e.printStackTrace();
				return ERROR_NAME;
			}
		}).collect(Collectors.toList());
	}

	public void addContact(String name) {
		try {
			contacts.add(getUser(name));
		} catch (Exception e) {
			System.err.println("Error adding contact: " + e.toString());
			e.printStackTrace();
		}
	}

	public UserRMI getContact(String name) {
		for (UserRMI userRMI : contacts) {
			String username;
			try {
				username = userRMI.getName();
			} catch (Exception e) {
				continue;
			}
			if (username.equals(name)) {
				return userRMI;
			}
		}
		return null;
	}

	public List<String> listRiddles() throws RemoteException {
		List<String> riddles = new ArrayList<>();
		for (RiddleRMI r : riddlesReceived) {
			riddles.add(r.getQuestion());
		}
		return riddles;
	}
}

class NameAlreadyTakenException extends Exception {
	public NameAlreadyTakenException(String name) {
		super("The name " + name + " is already taken.");
	}
}
