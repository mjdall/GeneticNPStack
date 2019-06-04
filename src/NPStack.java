// Morgan Dally - 1313361

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

class NPStack {
  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println("Usage: java NPStack <boxes-filename> <solutions-to-check>");
      System.exit(1);
    }

    int numSolutions = -1;
    try {
      numSolutions = Integer.parseInt(args[1]);
      if (numSolutions <= 0) {
        throw new NumberFormatException();
      }
    } catch (NumberFormatException nfE) {
      System.out.println("Invalid population size: " + args[1] + ".");
      System.exit(1);
    }

    NPStack stacker = new NPStack(args[0], numSolutions);
    stacker.runEnvironment();
  }

  private NPStack(String filename, int numSolutions) {
    try {
      if (numSolutions < 1000) {
        throw new RuntimeException("num-solutions needs to be >= 1000");
      }
      this.epochs = numSolutions / populationSize;
      getBoxes(filename);
      // generate a random with the current time
      this.rand = new Random(System.nanoTime());
      this.currentGeneration = getPopulation(this.populationSize);
      this.evaluateGeneration(this.currentGeneration);
    } catch (IOException ioException) {
      System.out.println("Unexpected IO Exception:\n" + ioException);
      System.exit(1);
    }
  }

  private final int populationSize = 1000;
  private final double survivalRate = 0.5;
  private final double mutationRate = 0.1;

  private final int populationReduction = (int) Math.floor(populationSize * survivalRate);
  private final int mutationCeil = (int) Math.floor(populationSize * mutationRate);

  private int epochs;
  private ArrayList<Box> boxes;
  private ArrayList<BoxStack> currentGeneration;
  private Random rand;

  private void runEnvironment() {
    // start generations at 1 as we've already got the first generation
    for (int generation = 1; generation < this.epochs; generation++) {
      // evaluateGeneration(this.currentGeneration);
      int remainingPopulation = this.populationSize - this.populationReduction;
      ArrayList<BoxStack> newPopMembers = new ArrayList<>();

      for (int i = 0; i < this.populationReduction; i++) {
        // select two boxes to breed
        int parentOneIdx = biasedRandom(remainingPopulation);
        int parentTwoIdx = biasedRandom(remainingPopulation);

        // make sure it's not the same box
        while (parentOneIdx == parentTwoIdx) {
          parentTwoIdx = biasedRandom(remainingPopulation);
        }

        BoxStack parentOne = this.currentGeneration.get(parentOneIdx);
        BoxStack parentTwo = this.currentGeneration.get(parentTwoIdx);
        BoxStack child = parentOne.breed(parentTwo);

        if (rand.nextInt(mutationCeil) == 0) {
          child = child.;
        }
        newPopMembers.add(child);
      }

      // add the individuals that survived
      for (int i = 0; i < remainingPopulation; i++) {
        newPopMembers.add(this.currentGeneration.get(i));
      }

      Collections.sort(newPopMembers);
      this.currentGeneration = newPopMembers;
    }
    evaluateGeneration(this.currentGeneration);
    BoxStack bestStack = this.currentGeneration.get(0);
    System.out.println("Best stack after " + epochs + " generations:");
    bestStack.auditStack();
    bestStack.printStack();
    bestStack.tryInsert(this.boxes);
  }

  private int biasedRandom(int max) {
    // biases closer to 0
    return (int) (max * Math.pow(rand.nextFloat(), 3));
  }

  private ArrayList<BoxStack> getPopulation(int numMembers) {
    ArrayList<BoxStack> newGeneration = new ArrayList<>();

    for (int i = 0; i < numMembers; i++) {
      // deep clone the list of boxes and then randomise each boxes orientation
      ArrayList<Box> randomisedBoxClone = randomiseBoxes(deepCloneBoxList(this.boxes));

      // create and add a new BoxStack using the randomised list
      BoxStack populationMember = new BoxStack(randomisedBoxClone, false);
      newGeneration.add(populationMember);
    }

    Collections.sort(newGeneration);
    return newGeneration;
  }

  private ArrayList<Box> deepCloneBoxList(ArrayList<Box> boxes) {
    List<Box> deepClonedBoxList = boxes.stream().map(Box::new).collect(Collectors.toList());
    return new ArrayList<>(deepClonedBoxList);
  }

  private void evaluateGeneration(ArrayList<BoxStack> generation) {
    int populationSize = generation.size();
    int maxHeight = 0;
    int minHeight = -1;
    int totalBoxes = 0;
    int totalHeight = 0;

    for (BoxStack stack : generation) {
      int stackHeight = stack.getHeight();
      if (stackHeight > maxHeight) {
        maxHeight = stackHeight;
      }
      if (stackHeight < minHeight || minHeight == -1) {
        minHeight = stackHeight;
      }
      totalBoxes += stackHeight;
      totalHeight += stack.getNumBoxes();
    }

    int averageNumBoxes = totalBoxes / populationSize;
    int averageStackSize = totalHeight / populationSize;
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

  private ArrayList<Box> randomiseBoxes(ArrayList<Box> boxesToRandomise) {
    for (Box b : boxesToRandomise) {
      b.randomlyOrientate();
    }
    Collections.sort(boxesToRandomise);
    return boxesToRandomise;
  }

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
