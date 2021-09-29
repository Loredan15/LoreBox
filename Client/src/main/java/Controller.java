import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.geekbrains.Command;
import com.geekbrains.FileMessage;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Controller implements Initializable {

    private static String ROOT_DIR = "client-sep-2021/root";
    private static String ROOT_C = "C:";
    private static byte[] buffer = new byte[1024];
    public ListView<String> tableView;
    public TextField input;
    public Label labelTest;
    public ComboBox<String> disks;
    public Button buttonUp;
    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;
    private Net net;

    public void send(ActionEvent actionEvent) throws Exception {
        String fileName = input.getText();
//        input.clear();
//        sendFile(fileName);
        net.sendMessage(fileName);
    }

    private void sendFile(String fileName) throws IOException {
        Path file = Paths.get(ROOT_DIR, fileName);
        os.writeObject(new FileMessage(file));
        os.flush();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        net = Net.getInstance(s -> Platform.runLater(() -> tableView.getItems().add(s)));
        Path file = Paths.get(ROOT_C);
        labelTest.setText(file.normalize().toAbsolutePath().toString());
//        localPath.setText(file.normalize().toAbsolutePath().toString());

        disks.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            disks.getItems().add(p.toString());
        }
        disks.getSelectionModel().select(0);

        try {
            fillFilesInCurrentDir();
            Socket socket = new Socket("localhost", 8189);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
            Thread daemon = new Thread(() -> {
                try {
                    while (true) {
                        Command msg = (Command) is.readObject();
                        // TODO: 23.09.2021 Разработка системы команд
                        switch (msg.getType()) {

                        }
                    }
                } catch (Exception e) {
                    log.error("exception while read from input stream");
                }
            });
            daemon.setDaemon(true);
            daemon.start();
        } catch (IOException ioException) {
            log.error("e=", ioException);
        }
    }

    private void fillFilesInCurrentDir() throws IOException {
        tableView.getItems().clear();
        tableView.getItems().addAll(
                Files.list(Paths.get(ROOT_C))
                        .map(p -> p.getFileName().toString())
                        .collect(Collectors.toList())
        );
        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Path path = Paths.get(labelTest.getText()).resolve(tableView.getSelectionModel().getSelectedItem());
                if (Files.isDirectory(path)) {
                    updateList(path);

//                String item = tableView.getSelectionModel().getSelectedItem();
//                input.setText(item);
                }
            }
        });
    }

    public void updateList(Path path) {
        try {
            tableView.getItems().clear();
            labelTest.setText(path.normalize().toAbsolutePath().toString());
            tableView.getItems().clear();
            tableView.getItems().addAll(
                    Files.list(Paths.get(path.toString()))
                            .map(p -> p.getFileName().toString())
                            .collect(Collectors.toList())
            );
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "По какой-то причине не удалось обновить список файлов", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void pathUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(labelTest.getText()).getParent();
        if (upperPath != null) {
            updateList(upperPath);
        }
    }
}
