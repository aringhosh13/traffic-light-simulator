# Traffic Light Simulator

A small Java project that compares fixed traffic-light timing with a simple adaptive rule. It includes a local dashboard and a set of example screenshots.

See the [screenshot gallery](docs/screenshots/README.md) for light, moderate, and peak traffic settings.

## How it works

The simulation checks for traffic once per second. A car arrives each second according to the arrival probability. A green light lets one car through each second. Both strategies use the same random seed, so they get the same arrivals.

Fixed timing uses the selected green time and 30 seconds of red. Adaptive timing adds 10 seconds to green when more than 10 cars are waiting at the start of a green phase.

## Example result

For a one-hour run with arrival probability `0.65`, green time `30` seconds, and seed `42`, the dashboard reports an average wait of 415.2 seconds with fixed timing and 220.5 seconds with adaptive timing. This is one simulated run; it does not predict results at a real intersection.

## Run the dashboard

Requires JDK 17 or newer. In PowerShell, from this project folder, run:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File ".\start-dashboard.ps1"
```

Then open http://127.0.0.1:8080. Leave the terminal running while you use the dashboard. Press Ctrl+C there to stop the server.

## Run the console version

```powershell
javac -d bin src/*.java
java -cp bin TrafficSimulator
```

## Limitations

The model has one queue, no cross traffic, and no turning cars. Average waiting time includes only cars that passed the light; cars left waiting at the end are reported separately.
