# Dynamic Traffic Light Controller & Queue Simulator

A Java-based discrete-event simulation that evaluates the performance of static vs. adaptive dynamic traffic signal timings. The project models intersection dynamics, vehicle arrival stochastics, and queue clearance to demonstrate how real-time queue length sensing reduces urban intersection congestion.

---

## Project Overview

Traffic congestion and sub-optimal signal timing lead to significant fuel waste and delay in urban road networks. Traditional fixed-time traffic controllers operate on rigid, static phase cycles regardless of real-time demand. 

This project implements a **Java discrete-event queue simulator** that models intersection dynamics, vehicle arrival stochastics, and queue clearance. By benchmarking a static 30-second signal cycle against an adaptive control rule (*extend green phase by 10s if queue length exceeds 10 vehicles*), this simulator proves how lightweight, real-time feedback loops significantly optimize throughput and reduce average vehicle wait times under heavy peak traffic loads.

---

## Key Features

- **Discrete-Event Simulation Engine**: Simulates second-by-second vehicle arrivals, queue formations, and signal cycle transitions without real-time wall-clock delays.
- **Adaptive Signal Optimization**: Implements dynamic signal extension logic based on real-time queue thresholds ($N > 10$ cars).
- **Seeded Pseudo-Random Arrival Model**: Uses explicit pseudo-random seeds (`java.util.Random(seed)`) to evaluate static and dynamic strategies against identical second-by-second vehicle arrival sequences.
- **Stochastic & Deterministic Benchmarking**: Supports both reproducible baseline runs and multi-trial stochastic variance analysis across independent seeds.

---

## Performance & Benchmark Results

## Performance & Benchmark Results

Running a 1-hour high-density peak traffic simulation ($P(\text{arrival}) = 0.65\text{/s}$, 3,600s duration) yields the following metrics:

### 1. Reproducible Baseline Run (`seed = 42`)

| Timing Strategy | Base Green Time | Dynamic Extension Rule | Avg Wait Time |
| :--- | :--- | :--- | :--- |
| **Fixed Control** | 30 seconds | None | **443.00s** |
| **Dynamic Control** | 30 seconds | +10s if queue > 10 cars | **230.97s** |

**Baseline Result**: Controlled testing against identical arrival sequences yields a **47.86% reduction** in average wait time.

### 2. Multi-Trial Stochastic Performance

Across independent stochastic trials and seed variations, wait-time reductions consistently hold between **47.86% and 53.10%** (e.g., secondary trial: 449.29s fixed vs. 210.73s dynamic $\rightarrow$ **53.10% reduction**). This confirms that performance gains are statistically robust and prevent exponential queue compounding across fluctuating traffic patterns.

---

## Technical Stack & Concepts

- **Language**: Java (JDK 8+)
- **Data Structures**: `java.util.Queue`, `java.util.LinkedList` (FIFO car processing)
- **Object-Oriented Architecture**: `Car`, `TrafficLight`, `Intersection`, and `TrafficSimulator` modules
- **Concepts**: Queueing Theory, Discrete-Event Simulation, Pseudo-Random Process Seeding, Adaptive Control Systems
---

## Repository Structure

```text
traffic-light-simulator/
├── src/
│   ├── Car.java
│   ├── TrafficLight.java
│   ├── Intersection.java
│   └── TrafficSimulator.java
├── bin/            # Compiled .class files (git-ignored)
├── .gitignore
├── LICENSE
└── README.md
