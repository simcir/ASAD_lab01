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
	private String bindingName;

	public Riddle(String question, Timestamp responseTime) {
		this.question = question;
		this.responseTime = responseTime;
		this.creationDate = new Date();

		this.bindingName = "riddle-" + this.creationDate.getTime();
		this.setTimeout();
		this.postRiddle();
	}

	private void postRiddle() {
		try {
			Remote stub = (Remote) UnicastRemoteObject.exportObject(this, 0);
			System.out.println("Exported riddle");
			Registry registry = LocateRegistry.getRegistry(1099);
			System.out.println("Got registry");
			registry.bind(this.bindingName, stub);
			System.out.println("Server ready");
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

	private void retractRiddle() {
		try {
			Registry registry = LocateRegistry.getRegistry(1099);
			registry.unbind(this.bindingName);
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

	public String toString() {
		return "Riddle: " + this.question + ", answer: " + (this.isAnswered() ? this.answer : "not answered yet");
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

	public boolean isAnswered() {
		return !answer.equals("");
	}

	public void remove() {
		if (!isAnswered())
			System.out.println("Riddle \"" + question + "\" timed out");
		retractRiddle();
	}

	public void setTimeout() {
		final long waitTime = responseTime.getTime();

		timeout = new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(waitTime);
					if (!isAnswered())
						remove();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		timeout.start();
	}
}
