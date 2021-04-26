package io;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {
    private final LinkedList <ClientHandler> clients;

    public Server(int port){
        clients = new LinkedList<ClientHandler>();
        try {
            ServerSocket server = new ServerSocket(port);
            System.out.println("server started");
            while (true){
                Socket socket = server.accept();
                System.out.println("client entry log");
                ClientHandler handler = new FileSender(socket, this);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (Exception e){
            System.err.println("server crash");
        }
    }

    public void broadcast(String message) throws IOException {
        for (ClientHandler c : clients) {
            c.sendMessage(message);
        }
    }

    public void kickClient(ClientHandler handler){
        clients.remove(handler);
    }
}
