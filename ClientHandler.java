package io;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable{

    protected final Socket socket;
    protected final Server server;
    protected DataInputStream inputStream;
    protected DataOutputStream outputStream;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try{
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
            System.out.println("connected");
            while (true){
                String message = inputStream.readUTF();
                System.out.println("server:" + message);
                server.broadcast(message);
            }
        } catch (IOException e) {
            System.err.println("connection lost");
            server.kickClient(this);
        }
    }

    public void sendMessage(String message) throws IOException {
        outputStream.writeUTF(message);
        outputStream.flush();
    }

    public void close() throws IOException {
        outputStream.close();
        inputStream.close();
        socket.close();
        System.out.println("streams were closed");
    }
}
