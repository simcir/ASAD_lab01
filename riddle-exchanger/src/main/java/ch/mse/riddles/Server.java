package ch.mse.riddles;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Arrays;

public class Server {
    public static void main(String[] args) {
        try {
            LocateRegistry
                    .createRegistry(System.getenv("PORT") == null ? 1099 : Integer.parseInt(System.getenv("PORT")));
            System.out.println("RMI registry ready.");
        } catch (RemoteException e) {
            System.out.println("RMI registry already running.");
        }
        while (true) {
            try {
                // Clear console and Dump RMI content
                System.out.print("\033[H\033[2J");
                System.out.println("RMI registry content:");
                Arrays.stream(LocateRegistry
                        .getRegistry(System.getenv("PORT") == null ? 1099 : Integer.parseInt(System.getenv("PORT")))
                        .list()).forEach(System.out::println);
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("Server interrupted.");
                e.printStackTrace();
            }
        }
    }
}
