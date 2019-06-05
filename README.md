# NP Hard Box Stacking - Genetic Algorithm
## Running
Run with `java NPStack <filename> <num-solutions>`
num-solutions defines how many solutions to check before stopping.

By default you need at least 1000 solutions, as my population size
is set to 1000.

Filename is a text file with box dimensions stored per line.
I.e:
* 8 3 2
* 3 1 10
* 4 2 10
* 6 10 3
* 3 9 9
* 4 1 2
* 7 6 2
* 9 6 4
* 5 8 10
* this line will be ignored
* 8 10 10
* -1 0 10 <- this box won't be included

## Implementation
My implementation is influenced by simulated annealing. When the population
reaches the point where the min box height is equal to the max box height,
99% of the population will be removed and re-generated. The mutation rate
is slightly lowered during every reset, as this seemed to give better performance.
If a population has settle on the same value, 200 times in a row, then the program
will be stopped early.

### Currently the hyper params are:
* populationSize: 1000                -> How many solutions are being analysed at once.
* survivalRate: 20%                   -> 20% of the fittest individuals in the population will survive.
* mutationRate: 10%                   -> Reduced by 9% every time a reset occurs.
* resetPopulationRemovalRate: 99%     -> How much of the population is removed during a reset.
* earlyStopPeaks: 200                 -> How many times the same solution is converged upon consecutively to stop early.

The code could be changed to tune the hyper parameters as time goes along (Like mutation rate slows down).
