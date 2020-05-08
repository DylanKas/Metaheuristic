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
        add(deliveryPointList.size(), deliveryPoint);
    }

    public void add(final Integer index, final DeliveryPoint deliveryPoint) {
        if(deliveryPointList.size() == 0){
            length += 2 * calculateDistance(warehouse, deliveryPoint);
        } else  {
            final DeliveryPoint previousPoint, nextPoint;
            if((index != 0)) {
                previousPoint = deliveryPointList.get(index - 1);
            }
            else {
                previousPoint = warehouse;
            }

            if(index != deliveryPointList.size()){
                nextPoint = deliveryPointList.get(index);
            } else {
                nextPoint = warehouse;
            }

            length += calculateDistance(previousPoint, deliveryPoint);
            length += calculateDistance(deliveryPoint, nextPoint);

            length -= calculateDistance(previousPoint, nextPoint);
        }

        totalQuantity += deliveryPoint.getQuantity();

        deliveryPointList.add(index, deliveryPoint);
    }

    public DeliveryPoint remove(final int index){
        final DeliveryPoint deliveryPoint = deliveryPointList.get(index);

        if(deliveryPointList.size() - 1 == 0){
            length = 0d;
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
        return Math.sqrt(Math.pow(endPoint.getX() - startPoint.getX(), 2) + Math.pow(endPoint.getY() - startPoint.getY(), 2));
    }

    @Override
    public boolean equals(Object a){
        return deliveryPointList.equals(((DeliveryRoute) a).getDeliveryPointList());
    }

    @Override
    public DeliveryRoute clone() {
        final DeliveryRoute clone = new DeliveryRoute(warehouse);
        deliveryPointList.stream().forEach(clone::add);
        return clone;
    }
}
