package controller;

import model.DeliveryPoint;
import model.DeliveryRoute;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DeliveryPointController {
    private static final String SEPARATOR = ";";
    private static final int MAX_QUANTITY = 100;
    private static final Random RANDOM = new Random();

    private String graphName = "";
    private List<DeliveryPoint> deliveryPointList;
    private List<DeliveryRoute> deliveryRoutes;

    private DeliveryPointController(final List<DeliveryPoint> deliveryPointList) {
        this.deliveryPointList = deliveryPointList;
    }

    public List<DeliveryPoint> getDeliveryPointList() {
        return deliveryPointList;
    }

    public String getGraphName() {
        return graphName;
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
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

    public void simulatedAnnealing(final int maximumIteration, final double variation) {
        List<DeliveryRoute> currentSolution;
        double latestTotalLength = getTotalLength();
        double temperature = getInitialTemperature();
        //double variation = 0.90;


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

    public List<DeliveryRoute> tabuSearch(final int tabuLength, final int maximumIteration) {
        List<DeliveryRoute> bestSolution = cloneCurrentSolution();
        double latestTotalLength = getTotalLength();
        double bestLength = latestTotalLength;
        Queue<List<DeliveryRoute>> tabuList = new LinkedList<>();

        final long start = System.currentTimeMillis();
        List<List<DeliveryRoute>> neighbors;
        for(int i=0;i<maximumIteration;i++){
            neighbors = generateNeighbors();
            List<DeliveryRoute> min = neighbors.stream()
                    .filter(neighbor -> !tabuList.contains(neighbor))
                    .min( (solution1, solution2) -> (int) (getRoutesTotalLength(solution1) - getRoutesTotalLength(solution2)))
                    .get();
            double newRouteLength = getRoutesTotalLength(min);
            if(newRouteLength > latestTotalLength){
                if(tabuList.size() == tabuLength){
                    tabuList.remove();
                }
                tabuList.add(cloneSolution(min));
            }
            latestTotalLength = newRouteLength;
            if(newRouteLength < bestLength){
                bestLength = newRouteLength;
                bestSolution = cloneSolution(min);
            }
            deliveryRoutes = min;
        }


        deliveryRoutes = bestSolution;

        return bestSolution;
    }


    public List<List<DeliveryRoute>> generateInitialPopulation(int sizePopulation, double mutationProbability){
        //Probabilité de selectionné une solution générer après la mutation de la solution précèdente. Un nombre plus petit permet d'avoir une population plus varié.
        //mutationProbability = 0.05;

        final List<List<DeliveryRoute>> populations = new ArrayList<>();

        while(populations.size() < sizePopulation){
            if(RANDOM.nextFloat() < 0.5){
                generateRandomNeighborSolution();
            }else{
                generateRandomNeighborSolution2();
            }

            if(RANDOM.nextFloat() < mutationProbability){
                populations.add(cloneCurrentSolution());
            }
        }

        return populations;
    }

    public List<List<DeliveryRoute>> generateNeighbors(){
        final List<DeliveryRoute> storedRoutes = cloneCurrentSolution();
        final List<List<DeliveryRoute>> neighbors = new ArrayList<>();

        for(int routeIndex = 0; routeIndex < storedRoutes.size(); routeIndex++) {
            for(int pointIndex = 0; pointIndex < storedRoutes.get(routeIndex).getDeliveryPointList().size(); pointIndex++) {

                neighbors.addAll(generateAllNeighbors(routeIndex, pointIndex));
                deliveryRoutes = cloneCurrentSolution();
            }
        }

        deliveryRoutes = storedRoutes;

        Collections.shuffle(neighbors);

        return neighbors;
    }

    public List<List<DeliveryRoute>> generateAllNeighbors(int routeIndex,int pointIndex) {
        final List<List<DeliveryRoute>> neighbors = new ArrayList<>();

        neighbors.addAll(generateAllNeighborSolutions(routeIndex, pointIndex));
        neighbors.addAll(generateAllNeighborSolutions2(routeIndex,pointIndex));

        return neighbors;
    }

    /* Deplace un point aléatoirement dans sa route */
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

    public List<List<DeliveryRoute>> generateAllNeighborSolutions(final int routeIndex, final int pointIndex) {
        final List<List<DeliveryRoute>> neighbors = new ArrayList<>();
        final List<DeliveryRoute> storedSolution = cloneCurrentSolution();
        final DeliveryRoute deliveryRouteToModify = deliveryRoutes.get(routeIndex);

        final int size = deliveryRouteToModify.getDeliveryPointList().size();

        if (size > 1) {
            final DeliveryPoint deliveryPointToMove = deliveryRouteToModify.remove(pointIndex);

            for(int i = 0; i < size; i++) {
                if(i != pointIndex) {
                    deliveryRouteToModify.add(i, deliveryPointToMove);
                    neighbors.add(cloneCurrentSolution());
                    deliveryRouteToModify.remove(i);
                }
            }

            deliveryRoutes = storedSolution;
        }

        return neighbors;
    }

    public List<List<DeliveryRoute>> generateAllNeighborSolutions2(int routeIndex, int pointIndex) {
        final List<List<DeliveryRoute>> neighbors = new ArrayList<>();

        List<DeliveryRoute> defaultSolution = cloneCurrentSolution();

        final DeliveryRoute deliveryRouteToModify = deliveryRoutes.get(routeIndex);
        final DeliveryPoint deliveryPointToMove = deliveryRouteToModify.remove(pointIndex);
        final boolean removedEmptyList = deliveryRouteToModify.getDeliveryPointList().isEmpty();
        if (deliveryRouteToModify.getDeliveryPointList().isEmpty()) {
            deliveryRoutes.remove(routeIndex);
        }

        final List<DeliveryRoute> intermediateSolution = cloneCurrentSolution();

        for(int routeInsertIndex = 0; routeInsertIndex < intermediateSolution.size(); routeInsertIndex++) {

            if(removedEmptyList || routeInsertIndex != routeIndex) {

                final DeliveryRoute deliveryRouteToInsert = deliveryRoutes.get(routeInsertIndex);

                if (deliveryRouteToInsert.getTotalQuantity() + deliveryPointToMove.getQuantity() <= MAX_QUANTITY) {
                    final List<DeliveryPoint> deliveryPointListToInsert = deliveryRouteToInsert.getDeliveryPointList();
                    for (int pointInsertIndex = 0; pointInsertIndex <= deliveryPointListToInsert.size(); pointInsertIndex++) {
                        deliveryRouteToInsert.add(pointInsertIndex, deliveryPointToMove);
                        neighbors.add(cloneCurrentSolution());
                        deliveryRouteToInsert.remove(pointInsertIndex);
                    }

                }

            }
        }



        final DeliveryRoute newDeliveryRoute = new DeliveryRoute(getWarehouse());
        newDeliveryRoute.add(deliveryPointToMove);
        deliveryRoutes.add(newDeliveryRoute);
        neighbors.add(deliveryRoutes);
        deliveryRoutes = defaultSolution;

        return neighbors;
    }

    /* Deplace un point aléatoirement d'une route à une autre' */
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

    public double getRoutesTotalLength(final List<DeliveryRoute> solution) {
        return solution.stream().mapToDouble(DeliveryRoute::getLength).sum();
    }

    private List<DeliveryRoute> cloneCurrentSolution(){
        return deliveryRoutes.stream().map(DeliveryRoute::clone).collect(Collectors.toList());
    }

    private List<DeliveryRoute> cloneSolution(final List<DeliveryRoute> solution){
        return solution.stream().map(DeliveryRoute::clone).collect(Collectors.toList());
    }

    private List<DeliveryRoute> selectRouletteSolution(final List<List<DeliveryRoute>> solutions) {
        final Map<Double, List<DeliveryRoute>> solutionsMap = new HashMap<>();
        double totalProbability = 0;
        for(List<DeliveryRoute> solution : solutions) {
            final double solutionProbability = 1 / getRoutesTotalLength(solution);
            solutionsMap.put(solutionProbability, solution);
            totalProbability += solutionProbability;
        }

        final double random = totalProbability * RANDOM.nextDouble();

        double currentProbabilityCount = 0;

        for (Map.Entry<Double, List<DeliveryRoute>> entry : solutionsMap.entrySet()) {
            if(random < entry.getKey() + currentProbabilityCount ){
                return entry.getValue();
            }
            else {
                currentProbabilityCount += entry.getKey();
            }
        }

        return null;
    }
}
