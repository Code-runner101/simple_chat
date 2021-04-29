package nio;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class NioServer {
    private static String path = "server/sentFiles";

    private ServerSocketChannel serverChannel;
    private Selector selector;
    static int countF;
    static int countD;

    public NioServer() {

        try {
            serverChannel = ServerSocketChannel.open();
            selector = Selector.open();
            serverChannel.bind(new InetSocketAddress(8189));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Server started...");

            while (serverChannel.isOpen()) {
                selector.select(); // block

                System.out.println("Keys selected!");

                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    }
                    if (key.isReadable()) {
                        handleRead(key);
                    }
                    keyIterator.remove();
                }
            }
        } catch (Exception e) {
            System.err.println("Server was broken");
        }
    }

    private String getMessageAsString(SocketChannel channel) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(256);

            int read;

            StringBuilder s = new StringBuilder();
            while (true) {

                read = channel.read(buffer);

                if (read == -1) {
                    channel.close();
                    break;
                }

                if (read == 0) {
                    break;
                }

                buffer.flip();

                while (buffer.hasRemaining()) {
                    s.append((char) buffer.get());
                }

                buffer.clear();
            }
            return s.toString();
        } catch (Exception e) {
            System.err.println("Connection was broken");
            return "err";
        }
    }

    private void handleRead(SelectionKey key) {
        try {

            SocketChannel channel = (SocketChannel) key.channel();

            String message = getMessageAsString(channel).trim();

            String[] parts = message.split(" ");

            if (parts[0].equals("ls")) {
                writeToChannel(channel, getServerFilesAsString());
            }

            if (parts[0].equals("cat")) {
                try {
                    Path file = Paths.get(path, parts[1]);
                    String fileData = getFileAsString(file);
                    writeToChannel(channel, fileData);
                } catch (Exception e) {
                    writeToChannel(channel, "WRONG COMMAND\n");
                }
            }

            if (parts[0].equals("touch")){
                    //всё хотел добиться того, чтобы пользователь мог сам давать имя файлу,
                    //но так этого сделать не смог(
//                    Path file = Paths.get(path, parts[1]);
//                    String fileName = createFile(file);
                    writeToChannel(channel, String.valueOf(createFile()));

            }

            if (parts[0].equals("mkdir")){
                    writeToChannel(channel, String.valueOf(createDirectory()));
            }

        } catch (Exception e) {
            System.err.println("Connection was broken");
        }
    }

    private String getFileAsString(Path path) throws IOException {
        return Files.lines(path)
                .collect(Collectors.joining("\n")) + "\n";
    }

    private void writeToChannel(SocketChannel channel, String message) throws IOException {
        channel.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
    }

    private String getServerFilesAsString() throws IOException {
        return Files.list(Paths.get("server", "sentFiles"))
                .map(this::getFileMeta)
                .collect(Collectors.joining("\n")) + "\n";
    }

    private String getFileMeta(Path path) {
        if (Files.isDirectory(path)) {
            return path.getFileName().toString() + " [DIR]";
        } else {
            return path.getFileName().toString() + " [FILE]";
        }
    }

    private Path createFile() throws IOException {
        countF++;
        File wayTo = new File(path);
        String fileName = wayTo + "/" + countF + ".txt";
        Path newFile = Files.createFile(Paths.get(fileName));
        return newFile;
    }

    private Path createDirectory() throws IOException {
        countD++;
        File wayTo = new File("server");
        String dirName = wayTo + "/serverFiles" + countD;
        Path newDir = Files.createDirectory(Paths.get(dirName));
        return newDir;
    }

//    private Path getPath(Files fileName) throws IOException {
//        return Paths.get()
//    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        System.out.println("Client accepted!");
    }
}
