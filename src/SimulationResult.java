import java.util.Locale;

public class SimulationResult {
    public record Frame(int queue, int served, int arrived, boolean green, int remaining, int greenDuration) {}
    public final Frame[] frames;
    public int served, arrived, remaining, maxQueue, maxWait;
    public long totalWait;

    public SimulationResult(int duration) { frames = new Frame[duration]; }
    public double averageWait() { return served == 0 ? 0 : (double) totalWait / served; }

    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append(String.format(Locale.ROOT,
                "{\"averageWait\":%.6f,\"served\":%d,\"arrived\":%d,\"remaining\":%d,\"maxQueue\":%d,\"maxWait\":%d,\"frames\":[",
                averageWait(), served, arrived, remaining, maxQueue, maxWait));
        for (int i = 0; i < frames.length; i++) {
            if (i > 0) json.append(',');
            Frame f = frames[i];
            json.append('[').append(f.queue()).append(',').append(f.served()).append(',')
                    .append(f.arrived()).append(',').append(f.green() ? 1 : 0).append(',')
                    .append(f.remaining()).append(',').append(f.greenDuration()).append(']');
        }
        return json.append("]}").toString();
    }
}
