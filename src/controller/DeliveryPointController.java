package controller;

import model.DeliveryPoint;
import model.DeliveryRoute;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DeliveryPointController {
    private static final String SEPARATOR = ";";
    private static final int MAX_QUANTITY = 100;
    private static final Random RANDOM = new Random();

    private List<DeliveryPoint> deliveryPointList;

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
        try {
            final BufferedReader reader = new BufferedReader(new FileReader(fileName));
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
        }
        return null;
    }

    public List<DeliveryRoute> generateOrderedSolution() {
        final List<DeliveryRoute> solution = new ArrayList<>();
        final DeliveryPoint warehouse = deliveryPointList.get(0);

        solution.add(new DeliveryRoute(warehouse));

        for (final DeliveryPoint deliveryPoint : deliveryPointList) {
            if (deliveryPoint.getQuantity() != 0) {
                final DeliveryRoute lastDeliveryRoute = solution.get(solution.size() - 1);

                if (lastDeliveryRoute.getTotalQuantity() + deliveryPoint.getQuantity() < MAX_QUANTITY) {
                    lastDeliveryRoute.add(deliveryPoint);
                } else {
                    final DeliveryRoute newDeliveryRoute = new DeliveryRoute(warehouse);

                    newDeliveryRoute.add(deliveryPoint);

                    solution.add(newDeliveryRoute);
                }
            }
        }

        return solution;
    }

    public List<DeliveryRoute> generateGreedySolution() {
        final List<DeliveryRoute> solution = new ArrayList<>();
        final DeliveryPoint warehouse = deliveryPointList.get(0);

        for (final DeliveryPoint deliveryPoint : deliveryPointList) {
            if (deliveryPoint.getQuantity() != 0) {
                final DeliveryRoute deliveryRoute = new DeliveryRoute(warehouse);
                deliveryRoute.add(deliveryPoint);
                solution.add(deliveryRoute);
            }
        }

        return solution;
    }

    public List<DeliveryRoute> generateRandomNeighborSolution(final List<DeliveryRoute> currentSolution) {
        final List<DeliveryRoute> solution = new ArrayList<>(currentSolution);
        final int deliveryRouteToModifyIndex = RANDOM.nextInt(solution.size());
        final DeliveryRoute deliveryRouteToModify = solution.get(deliveryRouteToModifyIndex);
        final List<DeliveryPoint> deliveryPointListToModify = deliveryRouteToModify.getDeliveryPointList();
        final int deliveryPointToMoveIndex = RANDOM.nextInt(deliveryRouteToModify.getDeliveryPointList().size());
        final DeliveryPoint deliveryPointToMove = deliveryPointListToModify.remove(deliveryPointToMoveIndex);
        int insertIndex;
        while((insertIndex = RANDOM.nextInt(deliveryPointListToModify.size())) == deliveryPointToMoveIndex);
        deliveryPointListToModify.add(insertIndex, deliveryPointToMove);
        return solution;
    }
}
