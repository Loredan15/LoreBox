import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import ru.maxol.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Controller implements Initializable {

    private static String ROOT_DIR = "client-sep-2021/root";
    private static String ROOT_C = "C:";
    private static byte[] buffer = new byte[1024];
    public ListView<String> clientView;
    public ListView<String> serverView;
    public Label pathLabelClient;
    public Label pathLabelServer;
    public ComboBox<String> disks;
    public Button buttonUpClient;
    public Button buttonUpServer;
    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;
    private Net net;
    private Path currentDir;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        net = Net.getInstance(s -> Platform.runLater(() -> clientView.getItems().add(s)));
        Path file = Paths.get(ROOT_C);
        currentDir = Paths.get("client", "root");
        pathLabelClient.setText(file.normalize().toAbsolutePath().toString());
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
                        Command command = (Command) is.readObject();
                        // TODO: 23.09.2021 Разработка системы команд
                        switch (command.getType()) {
                            case FILE_MESSAGE:
                                FileMessage message = (FileMessage) command;
                                Files.write(currentDir.resolve(message.getName()), message.getBytes());
                                updateClientView();
                                break;
                            case FILE_REQUEST:
                            case LIST_REQUEST:
                            case PATH_REQUEST:
                            case LIST_RESPONSE:
                                ListResponse response = (ListResponse) command;
                                List<String> names = response.getNames();
                                updateServerView(names);
                                break;
                            case PATH_RESPONSE:
                                PathResponse pathResponse = (PathResponse) command;
                                String path = pathResponse.getPath();
                                Platform.runLater(() -> pathLabelServer.setText(path));
                                break;

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
        clientView.getItems().clear();
        clientView.getItems().addAll(
                Files.list(Paths.get(ROOT_C))
                        .map(p -> p.getFileName().toString())
                        .collect(Collectors.toList())
        );
        clientView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Path path = Paths.get(pathLabelClient.getText()).resolve(clientView.getSelectionModel().getSelectedItem());
                if (Files.isDirectory(path)) {
                    updateList(path);

//                String item = tableView.getSelectionModel().getSelectedItem();
//                input.setText(item);
                }
            }
        });

        serverView.getItems().clear();
        serverView.getItems().addAll(
                Files.list(Paths.get(ROOT_DIR)).map(p -> p.getFileName().toString()).collect(Collectors.toList()));

    }

    public void updateList(Path path) {
        try {
            clientView.getItems().clear();
            pathLabelClient.setText(path.normalize().toAbsolutePath().toString());
            clientView.getItems().clear();
            clientView.getItems().addAll(
                    Files.list(Paths.get(path.toString()))
                            .map(p -> p.getFileName().toString())
                            .collect(Collectors.toList())
            );
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "По какой-то причине не удалось обновить список файлов", ButtonType.OK);
            alert.showAndWait();
        }
    }

    private void updateClientView() throws IOException {
        pathLabelClient.setText(currentDir.toString());
        List<String> names = Files.list(currentDir)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        Platform.runLater(() -> {
            clientView.getItems().clear();
            clientView.getItems().addAll(names);
        });
    }

    private void updateServerView(List<String> names) {
        Platform.runLater(() -> {
            serverView.getItems().clear();
            serverView.getItems().addAll(names);
        });
    }

    public void pathUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathLabelClient.getText()).getParent();
        if (upperPath != null) {
            updateList(upperPath);
        }
    }

    public void transferClientToServer(ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        FileMessage message = new FileMessage(currentDir.resolve(fileName));
        os.writeObject(message);
        os.flush();
    }

    public void transferServerToClient(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        os.writeObject(new FileRequest(fileName));
        os.flush();
    }

//    public void clientPathUp(ActionEvent actionEvent) throws IOException {
//        currentDir = currentDir.getParent();
//        clientPath.setText(currentDir.toString());
//        refreshClientView();
//    }

    public void serverPathUp(ActionEvent actionEvent) throws IOException {
        os.writeObject(new PathUpRequest());
        os.flush();
    }
}
