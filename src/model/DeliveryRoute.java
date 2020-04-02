package model;

import java.util.ArrayList;
import java.util.List;

public class DeliveryRoute implements Cloneable{
    private List<DeliveryPoint> deliveryPointList = new ArrayList<>();
    private double length = 0d;
    private int totalQuantity = 0;
    private DeliveryPoint warehouse;

    public DeliveryRoute(final DeliveryPoint warehouse) {
        this.warehouse = warehouse;
    }

    public void add(final DeliveryPoint deliveryPoint) {
        if(deliveryPointList.size() == 0){
            length += calculateDistance(warehouse, deliveryPoint);
        } else {
            length += calculateDistance(deliveryPointList.get(deliveryPointList.size() - 1), deliveryPoint);
        }

        totalQuantity += deliveryPoint.getQuantity();

        deliveryPointList.add(deliveryPoint);
    }

    public DeliveryPoint remove(final int index){
        final DeliveryPoint deliveryPoint = deliveryPointList.get(index);

        if(deliveryPointList.size() - 1 == 0){
            length = 0;
        } else {
            final DeliveryPoint previousPoint, nextPoint;
            if((index != 0)) {
                previousPoint = deliveryPointList.get(index - 1);
            }
            else {
                previousPoint = warehouse;
            }

            if(index != deliveryPointList.size() - 1){
                nextPoint = deliveryPointList.get(index + 1);
            } else {
                nextPoint = warehouse;
            }

            length -= calculateDistance(previousPoint, deliveryPoint);
            length -= calculateDistance(deliveryPoint, nextPoint);

            length += calculateDistance(previousPoint, nextPoint);
        }

        deliveryPointList.remove(index);

        totalQuantity -= deliveryPoint.getQuantity();

        return deliveryPoint;
    }

    public List<DeliveryPoint> getDeliveryPointList() {
        return deliveryPointList;
    }

    public double getLength() {
        return length;
    }

    public DeliveryPoint getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(DeliveryPoint warehouse) {
        this.warehouse = warehouse;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    private double calculateDistance(final DeliveryPoint startPoint, final DeliveryPoint endPoint) {
        return Math.sqrt((endPoint.getX() - startPoint.getX())^2 + (endPoint.getY() - startPoint.getY())^2);
    }

    @Override
    public DeliveryRoute clone() {
        final DeliveryRoute clone = new DeliveryRoute(warehouse);
        deliveryPointList.stream().forEach(clone::add);
        return clone;
    }
}
