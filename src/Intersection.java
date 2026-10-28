import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

public class Intersection {
    private final int baseGreenTime;

    public Intersection(int baseGreenTime) {
        if (baseGreenTime < 1) throw new IllegalArgumentException("Green time must be positive.");
        this.baseGreenTime = baseGreenTime;
    }

    public double runSimulation(int totalSeconds, boolean isDynamic, double spawnProbability) {
        return simulate(totalSeconds, isDynamic, spawnProbability, new Random().nextLong()).averageWait();
    }

    public SimulationResult simulate(int totalSeconds, boolean dynamic, double probability, long seed) {
        if (totalSeconds < 1 || !Double.isFinite(probability) || probability < 0 || probability > 1) {
            throw new IllegalArgumentException("Invalid simulation settings.");
        }
        Queue<Car> queue = new ArrayDeque<>();
        TrafficLight light = new TrafficLight(baseGreenTime);
        Random random = new Random(seed);
        SimulationResult result = new SimulationResult(totalSeconds);
        int cycleTimer = 0;
        long totalWait = 0;
        int served = 0;
        int arrived = 0;
        for (int time = 0; time < totalSeconds; time++) {
            if (random.nextDouble() < probability) queue.add(new Car(++arrived, time));
            if (cycleTimer == 0 || cycleTimer >= light.getCurrentGreenDuration() + 30) {
                cycleTimer = 0;
                light.setGreen(true, queue.size(), dynamic);
            } else if (cycleTimer == light.getCurrentGreenDuration()) {
                light.setGreen(false, queue.size(), dynamic);
            }
            if (light.isGreen() && !queue.isEmpty()) {
                int wait = queue.remove().getWaitTime(time);
                totalWait += wait;
                result.maxWait = Math.max(result.maxWait, wait);
                served++;
            }
            result.maxQueue = Math.max(result.maxQueue, queue.size());
            int remaining = light.isGreen() ? light.getCurrentGreenDuration() - cycleTimer
                    : light.getCurrentGreenDuration() + 30 - cycleTimer;
            result.frames[time] = new SimulationResult.Frame(queue.size(), served, arrived,
                    light.isGreen(), remaining, light.getCurrentGreenDuration());
            cycleTimer++;
        }
        result.totalWait = totalWait;
        result.served = served;
        result.arrived = arrived;
        result.remaining = queue.size();
        return result;
    }
}
