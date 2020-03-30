package view;

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
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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

    public Map(Stage stage) {
        this.stage = stage;
        this.stage.setResizable(false);
        this.stage.show();
        //Initialize Java FX nodes by their ID
        lookupNodes();

        logAction("--------- Application started ---------");
        drawCircle(0,0,5);
        drawCircle(100,100,10);


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
        List<String> namesList = Arrays.asList( "data01.txt", "data02.txt", "data03.txt","data04.txt","data05.txt");
        ArrayList<String> choices = new ArrayList<>(namesList);
        ObservableList<String> list = FXCollections.observableArrayList(choices);
        dataChoice.setItems(list);
        dataChoice.setValue(list.get(0));
        logAction("Data selected: " + list.get(0));
        dataChoice.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<>() {
            // if the item of the list is changed
            public void changed(ObservableValue ov, Number value, Number new_value) {
                logAction("Data selected: " + list.get(new_value.intValue()));
            }
        });

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

    public void drawCircle(int x, int y, int q){
        Platform.runLater(() -> {
            Circle circle = new Circle(OFFSET_X+LENGTH_X*x, OFFSET_Y+LENGTH_Y*y, DEFAULT_NODE_RADIUS);
            circle.setFill(DEFAULT_NODE_FILL);
            circle.setStroke(DEFAULT_NODE_STROKE);
            map.getChildren().add(circle);
            map.setStyle("-fx-background-color: #" + "ffffff");
        });
    }

    public void resetMap(){
        logAction("INFO: Map has been reset");
        Platform.runLater(() -> map.getChildren().clear());
    }


}
