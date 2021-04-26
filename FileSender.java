package io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

public class FileSender extends ClientHandler {

    protected static String dirPath = "server/sentFiles";
    private byte buffer[];
    private static int count;

    public FileSender(Socket socket, Server server) {
        super(socket, server);
        buffer = new byte[256];
        count++;
        dirPath = dirPath + "/user" + count;
    }

    private void init() throws IOException {
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());

    }

    @Override
    public void run() {
        try {

            init();

            while (true) {
                String fileName = inputStream.readUTF();
                System.out.println("file name: " + fileName);
                long fileLength = inputStream.readLong();
                System.out.println("file length: " + fileLength);
                String filePath = dirPath + "/" + fileName;
                FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                try {
                    for (int i = 0; i < (fileLength + 255) / 256; i++) {
                        int read = inputStream.read(buffer);
                        fileOutputStream.write(buffer, 0, read);
                        System.out.println("file was successfully uploaded");
                    }
                } catch (Exception e){
                    System.err.println("file wasn't delivered");
                    }
                }
            }catch (Exception e) {
                    System.err.println("connection lost");
                    try {
                        close();
                        } catch (Exception t) {
                            System.err.println("exception caused by closing");
                    }
                }
            }
        }
