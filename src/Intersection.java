import java.util.LinkedList;
import java.util.Queue;

public class Intersection {
    private final Queue<Car> queue;
    private final TrafficLight light;
    private int carIdCounter;
    private int totalWaitTime;
    private int totalCarsServed;

    public Intersection(int baseGreenTime) {
        this.queue = new LinkedList<>();
        this.light = new TrafficLight(baseGreenTime);
        this.carIdCounter = 1;
        this.totalWaitTime = 0;
        this.totalCarsServed = 0;
    }

    public double runSimulation(int totalSeconds, boolean isDynamic, double spawnProbability) {
        int redTime = 30;
        int cycleTimer = 0;

        for (int time = 0; time < totalSeconds; time++) {
            if (Math.random() < spawnProbability) {
                queue.add(new Car(carIdCounter++, time));
            }

            if (cycleTimer == 0) {
                light.setGreen(true, queue.size(), isDynamic);
            } else if (cycleTimer == light.getCurrentGreenDuration()) {
                light.setGreen(false, queue.size(), isDynamic);
            } else if (cycleTimer >= light.getCurrentGreenDuration() + redTime) {
                cycleTimer = 0;
                light.setGreen(true, queue.size(), isDynamic);
            }

            if (light.isGreen() && !queue.isEmpty()) {
                Car car = queue.poll();
                totalWaitTime += car.getWaitTime(time);
                totalCarsServed++;
            }

            cycleTimer++;
        }

        return totalCarsServed == 0 ? 0 : (double) totalWaitTime / totalCarsServed;
    }
}