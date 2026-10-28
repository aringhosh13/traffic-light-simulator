public class TrafficSimulator {
    public static void main(String[] args) {
        int simulationDuration = 3600; 
        int baseGreen = 30;            
        double peakSpawnRate = 0.65;   

        Intersection fixedIntersection = new Intersection(baseGreen);
        double avgWaitFixed = fixedIntersection.simulate(simulationDuration, false, peakSpawnRate, 42).averageWait();

        Intersection dynamicIntersection = new Intersection(baseGreen);
        double avgWaitDynamic = dynamicIntersection.simulate(simulationDuration, true, peakSpawnRate, 42).averageWait();

        System.out.println("=== TRAFFIC LIGHT QUEUE SIMULATION RESULTS ===");
        System.out.printf("Fixed Timing Avg Wait Time   : %.2f seconds\n", avgWaitFixed);
        System.out.printf("Dynamic Timing Avg Wait Time : %.2f seconds\n", avgWaitDynamic);

        double reduction = avgWaitFixed == 0 ? 0 : ((avgWaitFixed - avgWaitDynamic) / avgWaitFixed) * 100;
        System.out.printf("Wait Time Reduction         : %.2f%%\n", reduction);
    }
}
