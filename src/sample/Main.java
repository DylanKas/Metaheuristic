package sample;

import controller.DeliveryPointController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.DeliveryPoint;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 1200, 800));
        primaryStage.show();

        final DeliveryPointController controller = DeliveryPointController.initializeFromFile("./resource/data01.txt");

    }


    public static void main(String[] args) {
        launch(args);
    }
}
