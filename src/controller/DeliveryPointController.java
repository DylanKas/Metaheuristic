package controller;

import model.DeliveryPoint;
import model.DeliveryRoute;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
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

    public List<DeliveryRoute> generateRandomNeighborSolution() {
        final int deliveryRouteToModifyIndex = RANDOM.nextInt(deliveryRoutes.size());
        final DeliveryRoute deliveryRouteToModify = deliveryRoutes.get(deliveryRouteToModifyIndex);
        final List<DeliveryPoint> deliveryPointListToModify = deliveryRouteToModify.getDeliveryPointList();
        //TODO remove when we hae real operators
        if (deliveryPointListToModify.size() > 1) {
            final int deliveryPointToMoveIndex = RANDOM.nextInt(deliveryPointListToModify.size());
            final DeliveryPoint deliveryPointToMove = deliveryPointListToModify.remove(deliveryPointToMoveIndex);
            int insertIndex;
            while ((insertIndex = RANDOM.nextInt(deliveryPointListToModify.size() + 1)) == deliveryPointToMoveIndex);
            deliveryPointListToModify.add(insertIndex, deliveryPointToMove);
        }

        return deliveryRoutes;
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
}
