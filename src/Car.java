public class Car {
    private final int id;
    private final int arrivalTime;

    public Car(int id, int arrivalTime) {
        this.id = id;
        this.arrivalTime = arrivalTime;
    }

    public int getWaitTime(int currentTime) {
        return currentTime - arrivalTime;
    }
}