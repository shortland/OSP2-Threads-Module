# OSP2 Threads Module Implementation

This is an implementation of the "Threads" module in the OSP2 operating system.

The "Threads" module dispatches and schedules threads according to a round-robin & multi-queue design.

Threads are originally placed into queue Q1, in which threads are dispatched on clock cycles 1,2, and 3.
Threads that are dispatched over 4 times on Q1 are placed onto queue Q2, in which threads are dispatched on clock cycles 4 and 5.
Finally, threads that are dispatched 8 or more times, are placed onto queue Q3, in which threads are dispatched on clock cycle 6.

## Usage

`$ make run`

or

`$ make gui`
