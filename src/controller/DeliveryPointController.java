package controller;

import model.DeliveryPoint;
import model.DeliveryRoute;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DeliveryPointController {
    private static final String SEPARATOR = ";";
    private static final int MAX_QUANTITY = 100;
    private static final Random RANDOM = new Random();

    private List<DeliveryPoint> deliveryPointList;
    private List<DeliveryRoute> deliveryRoutes;

    private DeliveryPointController(final List<DeliveryPoint> deliveryPointList) {
        this.deliveryPointList = deliveryPointList;
    }

    public List<DeliveryPoint> getDeliveryPointList() {
        return deliveryPointList;
    }

    public DeliveryPoint getWarehouse() {
        return deliveryPointList.get(0);
    }

    public static DeliveryPointController initializeFromFile(final String fileName) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fileName));
            final List<DeliveryPoint> deliveryPointList = new ArrayList<>();

            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                final DeliveryPoint deliveryPoint = new DeliveryPoint();
                String[] splitLine = line.split(SEPARATOR);
                deliveryPoint.setI(Integer.parseInt(splitLine[0]));
                deliveryPoint.setX(Integer.parseInt(splitLine[1]));
                deliveryPoint.setY(Integer.parseInt(splitLine[2]));
                deliveryPoint.setQuantity(Integer.parseInt(splitLine[3]));
                deliveryPointList.add(deliveryPoint);
            }
            return new DeliveryPointController(deliveryPointList);
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public List<DeliveryRoute> getDeliveryRoutes() {
        return deliveryRoutes;
    }

    public void setDeliveryRoutes(List<DeliveryRoute> deliveryRoutes) {
        this.deliveryRoutes = deliveryRoutes;
    }

    public List<DeliveryRoute> generateOrderedSolution() {
        deliveryRoutes = new ArrayList<>();
        final DeliveryPoint warehouse = deliveryPointList.get(0);

        deliveryRoutes.add(new DeliveryRoute(warehouse));

        for (final DeliveryPoint deliveryPoint : deliveryPointList) {
            if (deliveryPoint.getQuantity() != 0) {
                final DeliveryRoute lastDeliveryRoute = deliveryRoutes.get(deliveryRoutes.size() - 1);

                if (lastDeliveryRoute.getTotalQuantity() + deliveryPoint.getQuantity() < MAX_QUANTITY) {
                    lastDeliveryRoute.add(deliveryPoint);
                } else {
                    final DeliveryRoute newDeliveryRoute = new DeliveryRoute(warehouse);

                    newDeliveryRoute.add(deliveryPoint);

                    deliveryRoutes.add(newDeliveryRoute);
                }
            }
        }

        return deliveryRoutes;
    }

    public List<DeliveryRoute> generateGreedySolution() {
        deliveryRoutes = new ArrayList<>();
        final DeliveryPoint warehouse = deliveryPointList.get(0);

        for (final DeliveryPoint deliveryPoint : deliveryPointList) {
            if (deliveryPoint.getQuantity() != 0) {
                final DeliveryRoute deliveryRoute = new DeliveryRoute(warehouse);
                deliveryRoute.add(deliveryPoint);
                deliveryRoutes.add(deliveryRoute);
            }
        }
        return deliveryRoutes;
    }

    public double getInitialTemperature(){
        return -getTotalLength()/Math.log(0.8);
    }

    public void simulatedAnnealing(final int maximumIteration) {
        List<DeliveryRoute> currentSolution;
        double latestTotalLength = getTotalLength();
        double temperature = getInitialTemperature();
        double variation = 0.90;


        for(int i = 0; i < maximumIteration; i++){
            currentSolution = cloneCurrentSolution();
            if (RANDOM.nextBoolean()) {
                generateRandomNeighborSolution2();
            } else {
                generateRandomNeighborSolution();
            }
            final double currentTotalLength = getTotalLength();
            if(currentTotalLength < latestTotalLength || (RANDOM.nextDouble() < Math.exp((latestTotalLength - currentTotalLength) / temperature))){
                latestTotalLength = currentTotalLength;
            }
            else {
                deliveryRoutes = currentSolution;
            }
            temperature = variation * temperature;
        }
    }

    public List<List<DeliveryRoute>> generateNeighbors(){
        final List<DeliveryRoute> storedRoutes = cloneCurrentSolution();
        final List<List<DeliveryRoute>> neighbors = new ArrayList<>();

        for(DeliveryRoute deliveryRoute : deliveryRoutes) {
            for(DeliveryPoint deliveryPoint : deliveryRoute.getDeliveryPointList()) {
                neighbors.addAll(generateAllNeighbors(deliveryPoint));
                deliveryRoutes = storedRoutes;
            }
        }

        return neighbors;
    }

    public List<List<DeliveryRoute>> generateAllNeighbors(final DeliveryPoint deliveryPoint) {
        final List<List<DeliveryRoute>> neighbors = new ArrayList<>();

        neighbors.addAll(generateAllNeighborSolutions(deliveryPoint));

        return neighbors;
    }

    public List<DeliveryRoute> generateRandomNeighborSolution() {
        final int deliveryRouteToModifyIndex = RANDOM.nextInt(deliveryRoutes.size());
        final DeliveryRoute deliveryRouteToModify = deliveryRoutes.get(deliveryRouteToModifyIndex);
        final List<DeliveryPoint> deliveryPointListToModify = deliveryRouteToModify.getDeliveryPointList();
        //TODO remove when we have real operators
        if (deliveryPointListToModify.size() > 1) {
            final int deliveryPointToMoveIndex = RANDOM.nextInt(deliveryPointListToModify.size());
            final DeliveryPoint deliveryPointToMove = deliveryRouteToModify.remove(deliveryPointToMoveIndex);
            int insertIndex;
            while ((insertIndex = RANDOM.nextInt(deliveryPointListToModify.size() + 1)) == deliveryPointToMoveIndex);
            deliveryRouteToModify.add(insertIndex, deliveryPointToMove);
        }

        return deliveryRoutes;
    }

    public List<List<DeliveryRoute>> generateAllNeighborSolutions(final DeliveryPoint deliveryPoint) {
        final List<List<DeliveryRoute>> neighbors = new ArrayList<>();
        final List<DeliveryRoute> storedSolution = cloneCurrentSolution();
        final DeliveryRoute deliveryRouteToModify = deliveryRoutes.stream()
                .filter(route -> route.getDeliveryPointList().contains(deliveryPoint))
                .findFirst()
                .get();

        final List<DeliveryPoint> deliveryPointListToModify = deliveryRouteToModify.getDeliveryPointList();

        if (deliveryPointListToModify.size() > 1) {
            deliveryPointListToModify.stream()
                    .filter(point -> point.getX() == deliveryPoint.getX() && point.getY() == deliveryPoint.getY())
                    .map(point -> {
                        final List<List<DeliveryRoute>> localNeighbors = new ArrayList<>();
                        final int pointIndex = deliveryPointListToModify.indexOf(point);
                        final DeliveryPoint deliveryPointToMove = deliveryRouteToModify.remove(pointIndex);

                        for(int i = 0; i <= deliveryPointListToModify.size(); i++) {
                            if(i != pointIndex) {
                                deliveryRouteToModify.add(i, deliveryPointToMove);
                                localNeighbors.add(cloneCurrentSolution());
                                deliveryRoutes = storedSolution;
                            }
                        }

                        return localNeighbors;
                    })
                    .collect(Collectors.toList())
                    .forEach(list -> neighbors.addAll(list));
        }

        return neighbors;
    }

    public List<DeliveryRoute> generateRandomNeighborSolution2() {
        final int deliveryRouteToModifyIndex = RANDOM.nextInt(deliveryRoutes.size());
        final DeliveryRoute deliveryRouteToModify = deliveryRoutes.get(deliveryRouteToModifyIndex);
        final List<DeliveryPoint> deliveryPointListToModify = deliveryRouteToModify.getDeliveryPointList();
        final int deliveryPointToMoveIndex = RANDOM.nextInt(deliveryRouteToModify.getDeliveryPointList().size());
        final DeliveryPoint deliveryPointToMove = deliveryRouteToModify.remove(deliveryPointToMoveIndex);
        final List<Integer> indexToVisitList = IntStream.rangeClosed(0, deliveryRoutes.size() - 1)
                .boxed().collect(Collectors.toList());
        indexToVisitList.remove(deliveryRouteToModifyIndex);

        Collections.shuffle(indexToVisitList);

        boolean hasInserted = false;
        int currentIndex = 0;

        while (!hasInserted && currentIndex < indexToVisitList.size()) {
            final int routeToInsertIndex = indexToVisitList.get(currentIndex);
            final DeliveryRoute deliveryRouteToInsert = deliveryRoutes.get(routeToInsertIndex);
            if (deliveryRouteToInsert.getTotalQuantity() + deliveryPointToMove.getQuantity() <= MAX_QUANTITY) {
                final List<DeliveryPoint> deliveryPointListToInsert = deliveryRouteToInsert.getDeliveryPointList();
                final int insertIndex = RANDOM.nextInt(deliveryPointListToInsert.size() + 1);
                deliveryRouteToInsert.add(insertIndex, deliveryPointToMove);
                hasInserted = true;
            }
            currentIndex++;
        }

        if (deliveryPointListToModify.isEmpty()) {
            deliveryRoutes.remove(deliveryRouteToModifyIndex);
        }

        if (!hasInserted) {
            final DeliveryRoute newDeliveryRoute = new DeliveryRoute(getWarehouse());
            newDeliveryRoute.add(deliveryPointToMove);
            deliveryRoutes.add(newDeliveryRoute);
        }

        return deliveryRoutes;
    }

    public double getTotalLength() {
        return deliveryRoutes.stream().mapToDouble(DeliveryRoute::getLength).sum();
    }

    private List<DeliveryRoute> cloneCurrentSolution(){
        return deliveryRoutes.stream().map(DeliveryRoute::clone).collect(Collectors.toList());
    }
}
