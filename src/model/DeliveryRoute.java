package model;

import java.util.ArrayList;
import java.util.List;

public class DeliveryRoute {
    private List<DeliveryPoint> deliveryPointList = new ArrayList<>();
    private double length = 0d;
    private int totalQuantity = 0;
    private DeliveryPoint warehouse;

    public DeliveryRoute(final DeliveryPoint warehouse) {
        this.warehouse = warehouse;
    }

    private void add(final DeliveryPoint deliveryPoint) {
        if(deliveryPointList.size() == 0){
            length += calculateDistance(warehouse, deliveryPoint);
        } else {
            length += calculateDistance(deliveryPointList.get(deliveryPointList.size() - 1), deliveryPoint);
        }

        totalQuantity += deliveryPoint.getQuantity();

        deliveryPointList.add(deliveryPoint);
    }

    private double calculateDistance(final DeliveryPoint startPoint, final DeliveryPoint endPoint) {
        return Math.sqrt((endPoint.getX() - startPoint.getX())^2 + (endPoint.getY() - startPoint.getY()));
    }
}
