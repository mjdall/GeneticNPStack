// Morgan Dally - 1313361

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

class NPStack {
  // number of solutions to hold at once
  private final int populationSize = 1000;
  private final double survivalRate = 0.2;

  // if the min height == max height, 99% of the population will be cut and regenerated
  private final double resetPopulationRemovalRate = 0.99;

  private final int populationReduction = (int) Math.floor(populationSize * survivalRate);

  private double mutationRate = 0.1;
  private final int mutationCeil = (int) Math.floor(populationSize * mutationRate);

  private int epochs;
  private ArrayList<Box> boxes;
  private ArrayList<BoxStack> currentGeneration;
  private Random rand;
  private int oldPeak;
  private int numSamePeaks = 0;

  /**
   * @param filename - The filename to get a list of boxes from.
   * @param numSolutions - The number of solutions to check.
   */
  private NPStack(String filename, int numSolutions) {
    try {
      // We need atleast 1000 solutions as that's our generation size
      if (numSolutions < 1000) {
        throw new RuntimeException("num-solutions needs to be >= 1000");
      }

      // how many generations we can go for with the number of solutions
      // we can check
      this.epochs = numSolutions / populationSize;

      // read in the boxes from file
      getBoxes(filename);

      // generate a random with the current time
      this.rand = new Random(System.nanoTime());

      // make the first generation
      this.currentGeneration = getPopulation(this.populationSize);

      // this prints generation stats
      this.evaluateGeneration(this.currentGeneration, false);
    } catch (IOException ioException) {
      System.out.println("Unexpected IO Exception:\n" + ioException);
      System.exit(1);
    }
  }

  public static void main(String[] args) {
    // we need 2 args
    if (args.length != 2) {
      System.out.println("Usage: java NPStack <boxes-filename> <solutions-to-check>");
      System.exit(1);
    }

    // first try and parse the number of solutions to an int
    int numSolutions = -1;
    try {
      numSolutions = Integer.parseInt(args[1]);
      if (numSolutions <= 0) {
        throw new NumberFormatException();
      }
    } catch (NumberFormatException nfE) {
      // we couldn't get a valid number of solutions
      System.out.println("Invalid population size: " + args[1] + ".");
      System.exit(1);
    }

    // initialised the stacker
    NPStack stacker = new NPStack(args[0], numSolutions);

    // run the stacker with it's current configuration
    stacker.runEnvironment();
  }

  // we reset generations, this allows us to stop early
  // if the same reset points settled on the same value
  // 200 times consecutively.
  private boolean populationSettled() {
    return this.numSamePeaks == 200;
  }

  // runs the GA until we've gone through the defined number of epochs.
  private void runEnvironment() {
    // start generations at 1 as we've already got the first generation
    for (int generation = 1; generation < this.epochs; generation++) {

      // check how many boxes will stay after the population is reduced
      int remainingPopulation = this.populationSize - this.populationReduction;

      // initialise the population list
      ArrayList<BoxStack> newPopMembers = new ArrayList<>();

      // go through and breed boxes till we fill the rest of the population
      for (int i = 0; i < this.populationReduction; i++) {

        // select two boxes to breed, slightly biases indexes closer to 0
        // fitter individuals will be bred more often
        int parentOneIdx = biasedRandom(remainingPopulation);
        int parentTwoIdx = biasedRandom(remainingPopulation);

        // make sure it's not the same box
        while (parentOneIdx == parentTwoIdx) {
          parentTwoIdx = biasedRandom(remainingPopulation);
        }

        // get the two boxes and breed a new one
        BoxStack parentOne = this.currentGeneration.get(parentOneIdx);
        BoxStack parentTwo = this.currentGeneration.get(parentTwoIdx);
        BoxStack child = parentOne.breed(parentTwo);

        // if this box has been selected for mutation
        if (rand.nextInt(mutationCeil) == 0) {
          child.mutate(BoxStack.deepCloneBoxList(this.boxes));
        }

        newPopMembers.add(child);
      }

      // add the individuals that survived
      for (int i = 0; i < remainingPopulation; i++) {
        newPopMembers.add(this.currentGeneration.get(i));
      }

      Collections.sort(newPopMembers);
      this.currentGeneration = newPopMembers;

      // min height == max height, get rid of most of the population
      if (evaluateGeneration(this.currentGeneration, false)) {
        resetPopulation();
        generation++;
      }

      if (this.populationSettled()) {
        this.epochs = generation;
        break;
      }
    }

    // get the box with the most height
    BoxStack bestStack = this.currentGeneration.get(0);

    // make sure we don't violate the one use condition
    bestStack.removeDuplicates();

    bestStack.printStack();

    // make sure our stack is actually valid
    bestStack.auditStack();
  }

  private void resetPopulation() {
    // how many new boxes we will generate
    int newGenerationMembers = (int) (this.populationSize * this.resetPopulationRemovalRate);

    // how many boxes we will keep
    int remainingMembers = this.populationSize - newGenerationMembers;

    // get the new members and add the survivors
    ArrayList<BoxStack> newGeneration = getPopulation(newGenerationMembers);
    for (int i = 0; i < remainingMembers; i++) {
      newGeneration.add(this.currentGeneration.get(i));
    }

    Collections.sort(newGeneration);
    this.currentGeneration = newGeneration;

    // decrease mutation rate of children slightly
    // this gave better performance than increasing it
    this.mutationRate *= 0.09;
  }

  // Gets a random number from (0, max], has a bias for lower values
  private int biasedRandom(int max) {
    return (int) (max * Math.pow(rand.nextFloat(), 1.1));
  }

  /**
   * Generates a new set of randomised box stacks.
   *
   * @param numMembers Number of box stacks to generate.
   * @return An ArrayList of randomly generated box stacks.
   */
  private ArrayList<BoxStack> getPopulation(int numMembers) {
    ArrayList<BoxStack> newGeneration = new ArrayList<>();

    for (int i = 0; i < numMembers; i++) {
      // deep clone the list of boxes and then randomise each boxes orientation
      ArrayList<Box> randomisedBoxClone = BoxStack.randomiseBoxes(this.boxes);

      // create and add a new BoxStack using the randomised list
      BoxStack populationMember = new BoxStack(randomisedBoxClone, false);
      newGeneration.add(populationMember);
    }

    Collections.sort(newGeneration);
    return newGeneration;
  }

  /**
   * Gets the current min, max, avg height, etc of the current generation.
   *
   * @param generation The BoxStack to evaluate.
   * @param printStats Whether or not to print the stats of the generation.
   * @return A boolean of whether or not the min value is equal to the max value. This is used to
   *     determine whether we should make some more boxes to add to the population.
   */
  private boolean evaluateGeneration(ArrayList<BoxStack> generation, boolean printStats) {
    int populationSize = generation.size();
    int maxHeight = generation.get(0).getHeight();
    int minHeight = generation.get(populationSize - 1).getHeight();
    int totalHeight = 0;
    int totalNumBoxes = 0;

    for (BoxStack stack : generation) {
      totalHeight += stack.getHeight();
      totalNumBoxes += stack.getNumBoxes();
    }

    int averageNumBoxes = totalNumBoxes / populationSize;
    int averageStackSize = totalHeight / populationSize;

    if (printStats) {
      System.out.println(
          "Average Stack Size: "
              + averageStackSize
              + " Average Num Boxes: "
              + averageNumBoxes
              + " max height: "
              + maxHeight
              + " min height: "
              + minHeight);
    }

    // early stopping counters
    if (minHeight == maxHeight) {
      if (oldPeak == maxHeight) {
        this.numSamePeaks += 1;
      } else {
        this.oldPeak = maxHeight;
        this.numSamePeaks = 0;
      }
    }

    return minHeight == maxHeight;
  }

  /**
   * Reads in boxes from a filename passed via cmd line.
   *
   * @param filename File to read boxes in from.
   * @throws IOException If an IOException occurs whilst reading in from the file.
   */
  private void getBoxes(String filename) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(filename));
    this.boxes = new ArrayList<>();
    String line;

    while ((line = br.readLine()) != null) {
      // split at whitespace
      String[] splitLine = line.split(" ");

      // only accept lines with 3 entries, i.e. "1 2 3"
      if (splitLine.length != 3) {
        continue;
      }

      int[] dimensions = new int[3];

      // try and parse each of the entries into an int
      try {
        for (int i = 0; i < 3; i++) {
          dimensions[i] = Integer.parseInt(splitLine[i]);

          // check the dimension is valid
          if (dimensions[i] < 1) {
            throw new NumberFormatException();
          }
        }
      } catch (NumberFormatException nfe) {
        continue;
      }

      this.boxes.add(new Box(dimensions));
    }
    br.close();
  }
}
