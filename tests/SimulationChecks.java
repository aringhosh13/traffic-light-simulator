import java.util.Arrays;

public class SimulationChecks {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        Intersection intersection = new Intersection(30);
        SimulationResult empty = intersection.simulate(100, false, 0, 42);
        check(empty.served == 0 && empty.remaining == 0 && empty.averageWait() == 0, "Empty traffic");
        SimulationResult fixed = intersection.simulate(100, false, 1, 42);
        SimulationResult adaptive = intersection.simulate(100, true, 1, 42);
        check(fixed.served == 60 && fixed.remaining == 40, "Fixed timing service capacity");
        check(adaptive.served == 70 && adaptive.remaining == 30, "Adaptive extension service capacity");
        check(fixed.frames[29].green() && !fixed.frames[30].green(), "Green-to-red boundary");
        check(!fixed.frames[59].green() && fixed.frames[60].green(), "Red-to-green boundary");
        check(adaptive.frames[60].greenDuration() == 40, "Extension at next green");
        check(!fixed.frames[90].green() && adaptive.frames[90].green(), "Extended phase remains green");
        SimulationResult first = intersection.simulate(3600, false, .65, 42);
        SimulationResult repeat = intersection.simulate(3600, false, .65, 42);
        SimulationResult paired = intersection.simulate(3600, true, .65, 42);
        check(Arrays.equals(first.frames, repeat.frames), "Repeat calls reset state and reproduce frames");
        check(first.averageWait() == repeat.averageWait(), "Repeatable wait statistics");
        for (int i = 0; i < first.frames.length; i++) {
            check(first.frames[i].arrived() == paired.frames[i].arrived(), "Identical paired arrivals");
        }
        for (SimulationResult result : new SimulationResult[] { empty, fixed, adaptive, first, paired }) {
            for (SimulationResult.Frame frame : result.frames) {
                check(frame.arrived() == frame.queue() + frame.served(), "Queue conservation");
                check(frame.remaining() > 0, "Positive phase countdown");
            }
            check(result.arrived == result.served + result.remaining, "Final queue conservation");
        }
        for (double probability : new double[] { -1, 1.1, Double.NaN, Double.POSITIVE_INFINITY }) {
            boolean rejected = false;
            try { intersection.simulate(10, false, probability, 1); }
            catch (IllegalArgumentException expected) { rejected = true; }
            check(rejected, "Reject invalid probability");
        }
        System.out.println("All simulation checks passed.");
    }
}
