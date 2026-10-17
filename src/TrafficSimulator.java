public class TrafficSimulator {
    public static void main(String[] args) {
        int simulationDuration = 3600; 
        int baseGreen = 30;            
        double peakSpawnRate = 0.65;   
        long seed = 42; // Constant seed for baseline benchmark reproducibility

        // Both intersections process identical car spawn sequences
        Intersection fixedIntersection = new Intersection(baseGreen, seed);
        double avgWaitFixed = fixedIntersection.runSimulation(simulationDuration, false, peakSpawnRate);

        Intersection dynamicIntersection = new Intersection(baseGreen, seed);
        double avgWaitDynamic = dynamicIntersection.runSimulation(simulationDuration, true, peakSpawnRate);

        System.out.println("=== REPRODUCIBLE TRAFFIC QUEUE SIMULATION RESULTS ===");
        System.out.printf("Fixed Timing Avg Wait Time   : %.2f seconds\n", avgWaitFixed);
        System.out.printf("Dynamic Timing Avg Wait Time : %.2f seconds\n", avgWaitDynamic);

        double reduction = ((avgWaitFixed - avgWaitDynamic) / avgWaitFixed) * 100;
        System.out.printf("Wait Time Reduction         : %.2f%%\n", reduction);
    }
}
