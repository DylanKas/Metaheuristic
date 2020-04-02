package view;

import controller.DeliveryPointController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import model.DeliveryPoint;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class Map {

    static double OFFSET_X = 25;
    static double OFFSET_Y = 25;

    static double LENGTH_X = 9;
    static double LENGTH_Y = 7.5;

    static Color DEFAULT_NODE_STROKE = Color.BLACK;
    static Color DEFAULT_NODE_FILL = javafx.scene.paint.Color.rgb(212,212,212);
    static double DEFAULT_NODE_RADIUS = 5;

    private Stage stage;
    private Pane map;
    private ChoiceBox dataChoice;
    private TextArea logs;

    private DeliveryPointController deliveryPointController;

    public Map(Stage stage) {
        this.stage = stage;
        this.stage.setResizable(false);
        this.stage.show();
        //Initialize Java FX nodes by their ID
        lookupNodes();
        logAction("--------- Application started ---------");

        //Set reset button behavior
        EventHandler<ActionEvent> buttonResetHandler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                resetMap();
            }
        };
        Button buttonReset = (Button) stage.getScene().lookup("#buttonReset");
        buttonReset.setOnAction(buttonResetHandler);

        //Set choice box with data files
        final File folder = new File("./resources/data");
        ArrayList<String> choices = new ArrayList<>(listFilesForFolder(folder));
        ObservableList<String> list = FXCollections.observableArrayList(choices);
        dataChoice.setItems(list);

        //Set default data file and initialize the map
        dataChoice.setValue(list.get(0));
        logAction("---- Data selected: " + list.get(0));
        dataChoice.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<>() {
            // if the item of the list is changed
            public void changed(ObservableValue ov, Number value, Number new_value) {
                logAction("---- Data selected: " + list.get(new_value.intValue()));
                resetMap();
                deliveryPointController = DeliveryPointController.initializeFromFile("./resources/data/"+list.get(new_value.intValue()));
                drawHouses(deliveryPointController.getDeliveryPointList());
            }
        });
        deliveryPointController = DeliveryPointController.initializeFromFile("./resources/data/"+list.get(0));
        drawHouses(deliveryPointController.getDeliveryPointList());
    }

    public ArrayList<String> listFilesForFolder(final File folder) {
        ArrayList<String> files = new ArrayList<String>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                files.add(fileEntry.getName());
            }
        }
        return files;
    }

    private void drawHouses(List<DeliveryPoint> lists){
        map.setStyle("-fx-background-color: #" + "76AE66");
        for (DeliveryPoint deliveryPoint : lists) {
            drawHouse(deliveryPoint);
        }
        logAction("- Delivery points added");
        drawWarehouse(deliveryPointController.getWarehouse());
        logAction("- Warehouse added (x: "+deliveryPointController.getWarehouse().getX()+", y: "+deliveryPointController.getWarehouse().getY()+")");
    }
    private void logAction(String action){
        logs.appendText(action + "\n");
    }
    private void lookupNodes() {
        map = (Pane) stage.getScene().lookup("#map");
        dataChoice = (ChoiceBox) stage.getScene().lookup("#dataChoice");
        logs = (TextArea) stage.getScene().lookup("#logs");
        //Add text area for logs to logsPane
        logs.setEditable(false);
    }

    public void drawHouse(DeliveryPoint deliveryPoint){
        Platform.runLater(() -> {
           //Circle circle = new Circle(OFFSET_X+LENGTH_X*x, OFFSET_Y+LENGTH_Y*y, DEFAULT_NODE_RADIUS);
           //circle.setFill(DEFAULT_NODE_FILL);
           //circle.setStroke(DEFAULT_NODE_STROKE);
           //map.getChildren().add(circle);
           //map.setStyle("-fx-background-color: #" + "ffffff");
            if(deliveryPoint.getI() == 0){
                return;
            }
            Image house = new Image("/house2.png");
            ImageView imageView = new ImageView();
            imageView.setImage(house);
            imageView.setX(OFFSET_X+LENGTH_X*deliveryPoint.getX());
            imageView.setY(OFFSET_Y+LENGTH_Y*deliveryPoint.getY());
            imageView.setFitHeight(20);
            imageView.setFitWidth(20);
            map.getChildren().add(imageView);
        });
    }

    public void drawWarehouse(DeliveryPoint warehouse){
        Platform.runLater(() -> {
            Image warehouseImage = new Image("/warehouse2.png");
            ImageView imageView2 = new ImageView();
            imageView2.setImage(warehouseImage);
            imageView2.setX(OFFSET_X+LENGTH_X*warehouse.getX());
            imageView2.setY(OFFSET_Y+LENGTH_Y*warehouse.getY());
            imageView2.setFitHeight(45);
            imageView2.setFitWidth(45);
            map.getChildren().add(imageView2);
        });
    }

    public void resetMap(){
        logAction("- Map has been reset");
        Platform.runLater(() -> map.getChildren().clear());
    }


}
