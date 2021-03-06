package sample;

import controller.DeliveryPointController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import view.Map;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Metaheuristic - Mehdi Labourdette - Dylan Kong A Siou");
        primaryStage.setScene(new Scene(root, 1200, 800));
        new Map(primaryStage);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
