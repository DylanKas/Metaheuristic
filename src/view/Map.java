package view;

import controller.DeliveryPointController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import model.DeliveryPoint;
import model.DeliveryRoute;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;


public class Map {

    static double OFFSET_X = 25;
    static double OFFSET_Y = 25;

    static double LENGTH_X = 9;
    static double LENGTH_Y = 7.5;

    static double HOUSE_IMAGE_HEIGHT = 20;
    static double HOUSE_IMAGE_WIDTH = 20;

    static double WAREHOUSE_IMAGE_HEIGHT = 45;
    static double WAREHOUSE_IMAGE_WIDTH = 45;

    static Color DEFAULT_NODE_STROKE = Color.BLACK;
    static Color DEFAULT_NODE_FILL = javafx.scene.paint.Color.rgb(212,212,212);
    static double DEFAULT_NODE_RADIUS = 5;

    private Stage stage;
    private StackPane stackpane;
    private Pane mapLines;
    private Pane mapHouses;
    private ChoiceBox dataChoice;
    private TextArea logs;
    private Label length;

    private ListView itineraryList;

    private Slider listSizeTabu;
    private Slider maxIterationTabu;
    private Slider speedTabu;

    private Slider variationSimulateadAnnealing;
    private Slider maxIterationSimulatedAnnealing;
    private Slider speedSimulatedAnnealing;


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

        //Set reset button behavior
        EventHandler<ActionEvent> buttonNeighbor1Handler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    runAndWait(Map.this::resetLines);
                } finally {
                    deliveryPointController.simulatedAnnealing((int) maxIterationSimulatedAnnealing.getValue(), (double) variationSimulateadAnnealing.getValue());
                    drawRoutes(deliveryPointController.getDeliveryRoutes());
                }

            }
        };
        Button buttonNeighbor1 = (Button) stage.getScene().lookup("#buttonNeighbor1");
        buttonNeighbor1.setOnAction(buttonNeighbor1Handler);

        //Set reset button behavior
        EventHandler<ActionEvent> buttonNeighbor2Handler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    runAndWait(Map.this::resetLines);
                } finally {
                    drawRoutes(deliveryPointController.tabuSearch((int) listSizeTabu.getValue(),(int) maxIterationTabu.getValue()));
                }
            }
        };
        Button buttonNeighbor2 = (Button) stage.getScene().lookup("#buttonNeighbor2");
        buttonNeighbor2.setOnAction(buttonNeighbor2Handler);

        //Set reset button behavior
        EventHandler<ActionEvent> buttonNeighbor3Handler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    runAndWait(Map.this::resetLines);
                } finally {
                    drawRoutes(deliveryPointController.geneticAlgorithm(100,10000,0.5,50));
                }
            }
        };
        Button buttonNeighbor3 = (Button) stage.getScene().lookup("#buttonNeighbor3");
        buttonNeighbor3.setOnAction(buttonNeighbor3Handler);

        //Set choice box with data files
        final File folder = new File("./resources/data");
        ArrayList<String> choices = new ArrayList<>(listFilesForFolder(folder));
        ObservableList<String> list = FXCollections.observableArrayList(choices);
        dataChoice.setItems(list);

        //Set default data file and initialize the map
        dataChoice.setValue(list.get(0));
        logAction("---- Data selected: " + list.get(0));
        dataChoice.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

            // if the item of the list is changed
            public void changed(ObservableValue ov, Number value, Number new_value) {

                logAction("---- Data selected: " + list.get(new_value.intValue()));

                // queue on JavaFX thread and wait for completion
                try {
                    runAndWait(Map.this::resetMap);
                } finally {
                    deliveryPointController = DeliveryPointController.initializeFromFile("./resources/data/"+list.get(new_value.intValue()));
                    deliveryPointController.setGraphName(list.get(new_value.intValue()));
                    drawRoutes(deliveryPointController.generateOrderedSolution());
                    drawHouses(deliveryPointController.getDeliveryPointList());
                }

            }
        });
        deliveryPointController = DeliveryPointController.initializeFromFile("./resources/data/"+list.get(0));
        deliveryPointController.setGraphName(list.get(0));
        drawRoutes(deliveryPointController.generateOrderedSolution());
        drawHouses(deliveryPointController.getDeliveryPointList());

        drawCsv();
    }

    public void drawCsv(){
        //Date object
        Date date= new Date();
        //getTime() returns current time in milliseconds
        long time = date.getTime();

        //Pour executer en greedy isGreedy = true, sinon Filltruck = false
        boolean isGreedy = false;
        //Le nombre de ligne de csv
        int nRow = 1;
        int maxIteration = 1000;
        int nbPopulation = 100;
        double tauxMutation = 0.5;
        int selectedBestNumber = 50;

        String exportFolder = "./export/genetic/";
        // File input path
        try (PrintWriter writer = new PrintWriter(new File(exportFolder+"GENETIC_"+time+"_NP"+nbPopulation+"_NG"+maxIteration+"_TM"+tauxMutation+"_BN"+selectedBestNumber+".csv"))) {

            StringBuilder sb = new StringBuilder();
            sb.append("i");
            sb.append(';');
            sb.append("GraphName");
            sb.append(';');
            sb.append("Algorithm");
            sb.append(';');
            sb.append("N Colis");
            sb.append(';');
            sb.append("Graph Initial");
            sb.append(';');
            sb.append("x0 fitness");
            sb.append(';');
            sb.append("x0 Nb camions");
            sb.append(';');
            sb.append("xi fitness");
            sb.append(';');
            sb.append("xi Nb camions");
            sb.append(';');
            sb.append("Temps calcul (en ms)");
            sb.append(';');
            sb.append("Max iteration (generation)");
            sb.append(';');
            sb.append("Nb population");
            sb.append(';');
            sb.append("Taux mutation");
            sb.append(';');
            sb.append("Nombre des meilleurs gardes");
            sb.append('\n');
            writer.write(sb.toString());


            /**********************************************************************/
            /**************  Faire ici les algos pour le csv  *********************/
            /******************************************************************** */
            String graphInitialType;




            long startTime, endTime, elapsedTime;

            for(int i=0;i<nRow;i++){
                System.out.println("Row: "+i);
                final File folder = new File("./resources/data");
                ArrayList<String> choices = new ArrayList<>(listFilesForFolder(folder));
                String graphName;
                for(int j=0;j<choices.size();j++){
                //for(int j=0;j<1;j++){
                    System.out.println("  ---- Graph: "+choices.get(j));
                    graphName = choices.get(j);
                    startTime = System.nanoTime();

                    deliveryPointController = DeliveryPointController.initializeFromFile("./resources/data/"+graphName);
                    deliveryPointController.setGraphName(graphName);

                    if(isGreedy){
                        graphInitialType = "greedy";
                        deliveryPointController.generateGreedySolution();
                    }else{
                        graphInitialType = "filltruck";
                        deliveryPointController.generateOrderedSolution();
                    }

                    sb = new StringBuilder();
                    sb.append(i);
                    sb.append(';');
                    sb.append(deliveryPointController.getGraphName());
                    sb.append(';');
                    sb.append("genetic");
                    sb.append(';');
                    sb.append(deliveryPointController.getDeliveryPointList().size());
                    sb.append(';');
                    sb.append(graphInitialType);
                    sb.append(';');
                    sb.append((int) deliveryPointController.getTotalLength());
                    sb.append(';');
                    sb.append(deliveryPointController.getDeliveryRoutes().size());
                    sb.append(';');


                    deliveryPointController.geneticAlgorithm(nbPopulation,maxIteration,tauxMutation,selectedBestNumber);
                    endTime = System.nanoTime();
                    elapsedTime = (endTime - startTime);  //divide by 1000000 to get milliseconds.

                    sb.append((int) deliveryPointController.getTotalLength());
                    sb.append(';');
                    sb.append(deliveryPointController.getDeliveryRoutes().size());
                    sb.append(';');
                    sb.append(elapsedTime/1000000);
                    sb.append(';');
                    sb.append(maxIteration);

                    sb.append(';');
                    sb.append(nbPopulation);

                    sb.append(';');
                    sb.append(tauxMutation);

                    sb.append(';');
                    sb.append(selectedBestNumber);
                    sb.append('\n');

                    writer.write(sb.toString());
                }
            }



            System.out.println("done!");

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
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
        mapLines = (Pane) stage.getScene().lookup("#lines");
        mapHouses = (Pane) stage.getScene().lookup("#houses");
        stackpane = (StackPane) stage.getScene().lookup("#stackpane");
        stackpane.getChildren().addAll(mapLines,mapHouses);
        dataChoice = (ChoiceBox) stage.getScene().lookup("#dataChoice");
        itineraryList = (ListView) stage.getScene().lookup("#itinerary_list");
        logs = (TextArea) stage.getScene().lookup("#logs");
        length = (Label) stage.getScene().lookup("#length");
        listSizeTabu = (Slider) stage.getScene().lookup("#list_size_tabou");
        speedTabu = (Slider) stage.getScene().lookup("#speed_tabou");
        maxIterationTabu = (Slider) stage.getScene().lookup("#maximum_iteration_tabou");
        variationSimulateadAnnealing = (Slider) stage.getScene().lookup("#Variation");
        speedSimulatedAnnealing = (Slider) stage.getScene().lookup("#speed_recuit");
        maxIterationSimulatedAnnealing = (Slider) stage.getScene().lookup("#maximum_iteration_recuit");

        //Add text area for logs to logsPane
        logs.setEditable(false);
    }

    public void drawRoutes(List<DeliveryRoute> lists){
        mapLines.setStyle("-fx-background-color: #" + "76AE66");
        if(lists.size()==0){
            System.out.println("Empty DeliveryRoutes");
            return;
        }
        DeliveryPoint warehouse = lists.get(0).getWarehouse();
        int routeIteration = 0;
        for (DeliveryRoute deliveryRoute : lists) {
            if(deliveryRoute.getDeliveryPointList().size()==0){
                System.out.println("SIZE OF ZERO");
            }
            routeIteration++;
            int iteration = 0;
            List<DeliveryPoint> routes = deliveryRoute.getDeliveryPointList();
            double lastX = 0d;
            double lastY = 0d;


            String[] colors = {"#FF6633", "#FFB399", "#FF33FF", "#FFFF99", "#00B3E6",
                    "#E6B333", "#3366E6", "#999966", "#99FF99", "#B34D4D",
                    "#80B300", "#809900", "#E6B3B3", "#6680B3", "#66991A",
                    "#FF99E6", "#CCFF1A", "#FF1A66", "#E6331A", "#33FFCC",
                    "#66994D", "#B366CC", "#4D8000", "#B33300", "#CC80CC",
                    "#66664D", "#991AFF", "#E666FF", "#4DB3FF", "#1AB399",
                    "#E666B3", "#33991A", "#CC9999", "#B3B31A", "#00E680",
                    "#4D8066", "#809980", "#E6FF80", "#1AFF33", "#999933",
                    "#FF3380", "#CCCC00", "#66E64D", "#4D80CC", "#9900B3",
                    "#E64D66", "#4DB380", "#FF4D4D", "#99E6E6", "#6666FF"};


            Paint routeColor = Color.web(colors[routeIteration]);
            Line lineBegin = new Line();
            Line lineEnd = new Line();
            lineBegin.setStroke(routeColor);
            lineBegin.setStrokeWidth(2);
            lineEnd.setStroke(routeColor);
            lineEnd.setStrokeWidth(2);

            //first route from warehouse
            lineBegin.setStartX(OFFSET_X+LENGTH_X*warehouse.getX());
            lineBegin.setStartY(OFFSET_Y+LENGTH_Y*warehouse.getY());
            lineBegin.setEndX(OFFSET_X+LENGTH_X*routes.get(0).getX());
            lineBegin.setEndY(OFFSET_Y+LENGTH_Y*routes.get(0).getY());


            //last route to warehouse
            lineEnd.setStartX(OFFSET_X+LENGTH_X*routes.get(routes.size()-1).getX());
            lineEnd.setStartY(OFFSET_Y+LENGTH_Y*routes.get(routes.size()-1).getY());
            lineEnd.setEndX(OFFSET_X+LENGTH_X*warehouse.getX());
            lineEnd.setEndY(OFFSET_Y+LENGTH_Y*warehouse.getY());

            mapLines.getChildren().add(lineBegin);
            mapLines.getChildren().add(lineEnd);
            for (DeliveryPoint deliveryPoint : routes) {
                Line line = new Line();
                line.setStroke(routeColor);
                line.setStrokeWidth(2);

                if(iteration!=0){//Start from warehouse
                    line.setStartX(OFFSET_X+LENGTH_X*lastX);
                    line.setStartY(OFFSET_Y+LENGTH_Y*lastY);
                    line.setEndX(OFFSET_X+LENGTH_X*deliveryPoint.getX());
                    line.setEndY(OFFSET_Y+LENGTH_Y*deliveryPoint.getY());
                }

                lastX = deliveryPoint.getX();
                lastY = deliveryPoint.getY();

                mapLines.getChildren().add(line);
                iteration++;
            }
        }

        length.setText("Fitness: "+ new DecimalFormat("#.##").format((deliveryPointController.getTotalLength()))+"   -   Vehicles: "+lists.size());

        ListView<String> list = new ListView<String>();
        ObservableList<String> items =FXCollections.observableArrayList();
        //items.add("N vehicules:"+lists.get(i).getDeliveryPointList().size());
        for(int i=0;i<lists.size();i++){
            items.add(i+"| N: "+lists.get(i).getDeliveryPointList().size()+" | L: "+(int) Math.floor(lists.get(i).getLength() * 100)/100+" | Q: "+lists.get(i).getTotalQuantity());
        }

        itineraryList.setItems(items);
    }

    public void drawHouse(DeliveryPoint deliveryPoint){
        Platform.runLater(() -> {
            if(deliveryPoint.getI() == 0){
                return;
            }
            Image house = new Image("./house2.png");
            ImageView imageView = new ImageView();
            imageView.setImage(house);
            imageView.setX(OFFSET_X+LENGTH_X*deliveryPoint.getX()-HOUSE_IMAGE_HEIGHT/2);
            imageView.setY(OFFSET_Y+LENGTH_Y*deliveryPoint.getY()-HOUSE_IMAGE_WIDTH/2);
            imageView.setFitHeight(HOUSE_IMAGE_HEIGHT);
            imageView.setFitWidth(HOUSE_IMAGE_WIDTH);
            mapHouses.getChildren().add(imageView);
        });
    }

    public void drawWarehouse(DeliveryPoint warehouse){
        Platform.runLater(() -> {
            Image warehouseImage = new Image("./warehouse2.png");
            ImageView imageView2 = new ImageView();
            imageView2.setImage(warehouseImage);
            imageView2.setX(OFFSET_X+LENGTH_X*warehouse.getX()-WAREHOUSE_IMAGE_HEIGHT/2);
            imageView2.setY(OFFSET_Y+LENGTH_Y*warehouse.getY()-WAREHOUSE_IMAGE_WIDTH/2);
            imageView2.setFitHeight(WAREHOUSE_IMAGE_HEIGHT);
            imageView2.setFitWidth(WAREHOUSE_IMAGE_WIDTH);
            mapHouses.getChildren().add(imageView2);
        });
    }

    public void resetMap(){
        logAction("- Map has been reset");
        deliveryPointController.generateInitialPopulation(20,1);
        resetLines();
        resetHouses();
    }


    public void resetLines(){
        mapLines.getChildren().clear();
    }

    public void resetHouses(){
        mapHouses.getChildren().clear();
    }
    /**
     * Runs the specified {@link Runnable} on the
     * JavaFX application thread and waits for completion.
     *
     * @param action the {@link Runnable} to run
     * @throws NullPointerException if {@code action} is {@code null}
     */
    public static void runAndWait(Runnable action) {
        if (action == null)
            throw new NullPointerException("action");

        // run synchronously on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        // queue on JavaFX thread and wait for completion
        final CountDownLatch doneLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                doneLatch.countDown();
            }
        });

        try {
            doneLatch.await();
        } catch (InterruptedException e) {
            // ignore exception
        }
    }


}
