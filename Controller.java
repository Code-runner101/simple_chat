import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller implements Initializable {

    private static final String clientPath = "client/clientFiles";

    public ListView<String> listView;
    public TextField insert;
    private DataInputStream in;
    private DataOutputStream out;
    private byte[] buffer = new byte[256];

    public void send(ActionEvent actionEvent) throws IOException {
        String fileName = listView.getSelectionModel().getSelectedItem();
        out.writeUTF(fileName);
        long len = Files.size(Paths.get(clientPath, fileName));
        out.writeLong(len);
        try (FileInputStream fileInputStream = new FileInputStream(clientPath + "/" + fileName)){
            int read;
            while (true){
                read = fileInputStream.read(buffer);
                if (read ==-1){

                    break;
                }
                out.write(buffer, 0, read);
            }
        }
        out.flush();
    }

    public void initialize(URL location, ResourceBundle resources) {
        try {
            List<String> clientFiles = Files.list(Paths.get(clientPath))
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());

            listView.getItems().addAll(clientFiles);

            Socket socket = new Socket("localhost", 8899);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

        }catch (Exception e){
            System.err.println("socket error");
        }
    }
}
