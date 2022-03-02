import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent parent = FXMLLoader.load(getClass().getResource("chat.fxml"));
        primaryStage.setScene(new Scene(parent));
        primaryStage.setTitle("LoreBox");
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> Platform.exit());
    }
}
