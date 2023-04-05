package ch.mse.riddles;

import java.util.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

interface RiddleRMI extends Remote {
	public String getQuestion() throws RemoteException;

	public Date getTimeoutDate() throws RemoteException;

	public void answer(String answer) throws RemoteException;
}

class Riddle implements RiddleRMI {
	private String question;
	private Date creationDate;
	private Timestamp responseTime;
	private String answer = "";
	private Thread timeout;

	public Riddle(String question, Timestamp responseTime) {
		this.question = question;
		this.responseTime = responseTime;
		this.creationDate = new Date();

		this.setTimeout();
		postRiddle(this);
	}

	private static void postRiddle(RiddleRMI r) {
		try {
			Remote stub = (Remote) UnicastRemoteObject.exportObject(r, 0);
			System.out.println("Exported riddle");
			Registry registry = LocateRegistry.createRegistry(1099);
			System.out.println("Got registry");
			registry.bind("Hello", stub);
			System.out.println("Server ready");
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

	private static void retractRiddle(RiddleRMI r) {
		try {
			Registry registry = LocateRegistry.getRegistry(1099);
			registry.unbind("Hello");
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

	public String getQuestion() throws RemoteException {
		return this.question;
	}

	public Date getTimeoutDate() throws RemoteException {
		return Date.from(Instant.ofEpochMilli(this.creationDate.getTime() + this.responseTime.getTime()));
	}

	public void answer(String answer) throws RemoteException {
		this.answer = answer;
	}

	public void remove() {
		System.out.println("Riddle removed, answer: " + answer);
		retractRiddle(this);
	}

	public void setTimeout() {
		final long waitTime = responseTime.getTime();

		timeout = new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(waitTime);
					remove();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		timeout.start();
	}
}
