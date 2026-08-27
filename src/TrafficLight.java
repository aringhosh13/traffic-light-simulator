public class TrafficLight {
    private boolean isGreen;
    private int currentGreenDuration;
    private final int baseGreenTime;

    public TrafficLight(int baseGreenTime) {
        this.baseGreenTime = baseGreenTime;
        this.isGreen = false;
        this.currentGreenDuration = 0;
    }

    public boolean isGreen() { return isGreen; }

    public void setGreen(boolean green, int queueSize, boolean isDynamic) {
        this.isGreen = green;
        if (green) {
            // Dynamic Rule: Extend green time by 10s if queue > 10
            this.currentGreenDuration = (isDynamic && queueSize > 10) ? baseGreenTime + 10 : baseGreenTime;
        }
    }

    public int getCurrentGreenDuration() { return currentGreenDuration; }
}