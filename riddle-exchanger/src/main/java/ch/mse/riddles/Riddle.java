package ch.mse.riddles;

import java.util.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyPair;
import java.security.PublicKey;

interface RiddleRMI extends Remote {
	public byte[] getEncryptedQuestion() throws RemoteException;

	public Date getTimeoutDate() throws RemoteException;

	public void answer(String encryptedAnswer) throws RemoteException;

	public boolean isAnswered() throws RemoteException;

	public boolean isImportant() throws RemoteException;
}

class Riddle implements RiddleRMI {
	private String question;
	private Date creationDate;
	private Timestamp responseTime;
	private byte[] encryptedAnswer;
	private Thread timeout;
	private String bindingName;
	private PublicKey targetPubKey;
	private KeyPair ownerKeyPair;
	private boolean important;

	public Riddle(String question, Timestamp responseTime, PublicKey targetPubKey, KeyPair ownerPair, boolean important) {
		this.question = question;
		this.responseTime = responseTime;
		this.creationDate = new Date();
		this.targetPubKey = targetPubKey;
		this.ownerKeyPair = ownerPair;
		this.bindingName = "riddle-" + this.creationDate.getTime();
		this.important = important;
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
		return "Riddle: " + this.question + ", answer: " + (this.isAnswered() ? new String(CryptoUtils.decrypt(this.encryptedAnswer, this.ownerKeyPair.getPrivate())) : "not answered yet");
	}

	public byte[] getEncryptedQuestion() throws RemoteException {
		return CryptoUtils.encrypt(question, targetPubKey);
	}

	public Date getTimeoutDate() throws RemoteException {
		return Date.from(Instant.ofEpochMilli(this.creationDate.getTime() + this.responseTime.getTime()));
	}

	public void answer(String answer) throws RemoteException {
		this.encryptedAnswer = CryptoUtils.encrypt(answer, ownerKeyPair.getPublic());
	}

	public boolean isAnswered() {
		return !(encryptedAnswer == null);
	}

	public boolean isImportant() {
		return important;
	}

	public void remove() {
		if (!isAnswered()){
			System.out.println("Riddle \"" + question + "\" timed out");
		}
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
