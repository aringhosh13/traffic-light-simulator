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
- **Stochastic Arrival Modeling**: Uses controlled `Math.random()` distributions to model natural, bursty vehicle arrival patterns during peak traffic hours.
- **Performance Benchmarking**: Automatically computes average wait times and percentage performance gains across comparative simulation runs.

---

## Performance Results

Running a 1-hour high-density peak traffic simulation ($P(\text{arrival}) = 0.65\text{/s}$) yields the following benchmark metrics:

| Timing Strategy | Base Green Time | Dynamic Extension Rule | Avg Wait Time |
| :--- | :--- | :--- | :--- |
| **Fixed Control** | 30 seconds | None | ~397.46s |
| **Dynamic Control** | 30 seconds | +10s if queue > 10 cars | ~206.99s |

**Result**: A simple dynamic queue check reduces average simulated wait times by **~47.9%** under heavy traffic load.

---

## Technical Stack & Concepts

- **Language**: Java (JDK 8+)
- **Data Structures**: `java.util.Queue`, `java.util.LinkedList` (FIFO car processing)
- **Object-Oriented Architecture**: `Car`, `TrafficLight`, `Intersection`, and `TrafficSimulator` modules
- **Concepts**: Queueing Theory, Discrete-Event Simulation, Stochastic Processes, Adaptive Control Systems

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
