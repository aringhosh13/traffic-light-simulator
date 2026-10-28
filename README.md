# Traffic Light Simulator

A small Java experiment comparing fixed and adaptive traffic-light timing, with a local browser dashboard.

See the [simulator screenshots](docs/screenshots/README.md) for light, moderate, and peak traffic examples.

## Run the dashboard

Requires JDK 17 or newer. No npm packages or external services are needed.

From this folder in PowerShell:

```powershell
.\start-dashboard.ps1
```

Open http://127.0.0.1:8080. To use a different port: `./start-dashboard.ps1 -Port 8081`.

Or compile and launch directly from the project folder:

```powershell
javac -d bin src/*.java
java -cp bin DashboardServer
```

Keep the terminal running; Ctrl+C stops the server. The server listens only on this computer.

## Using it

Choose the duration, arrival probability, base green time, and random seed, then click Run simulation. Both strategies receive the exact same arrivals. The results table describes the full run. Playback starts paused; click Play or move the time slider to inspect individual seconds. CSV export includes every recorded second and the run settings.

The dashboard starts at moderate traffic (0.45). Enter 0.65 for the original peak traffic setting. Reset restores the form; Run simulation applies the settings.

## Model

- One queue; at most one car arrives per second, according to the arrival probability.
- One car is served per green second. Arrivals happen before signal evaluation and service.
- Fixed timing uses the selected green duration and 30 seconds of red.
- Adaptive timing adds 10 seconds of green if the queue exceeds 10 cars when the green phase begins. The duration stays fixed for that phase.
- There are no turning vehicles, amber phase, or competing cross-traffic queues.
- Average wait and longest wait include only served cars. The final backlog is reported separately, not drained after the requested duration.
- Queue drawings are schematic and show up to 10 cars (fewer on narrow screens). Numeric counters and CSV include the entire queue.
- Results are one seeded experiment, not a claim of statistical significance. At the original peak setting, demand exceeds both strategies' maximum capacity and queues tend to grow.

The Java model is in `src/Intersection.java`; the local HTTP adapter is in `src/DashboardServer.java`. The dashboard uses plain HTML, CSS, and JavaScript in `web/`.

## Console version

```powershell
javac -d bin src/*.java
java -cp bin TrafficSimulator
```

## Verification

```powershell
javac -cp bin -d bin tests/SimulationChecks.java
java -cp bin SimulationChecks
```

Checks cover repeatable arrivals, queue conservation, phase boundaries, adaptive extension, repeat runs, and invalid inputs.

With the dashboard server running, optional HTTP checks use Node.js 18 or newer:

```powershell
node tests/dashboard-checks.mjs
```

Pass a different server URL as the last argument if needed. Node.js is only needed for these HTTP checks, not for the dashboard.

![Dashboard preview](docs/dashboard-preview.png)
