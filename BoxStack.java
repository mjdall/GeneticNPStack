import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

class BoxStack implements Comparable<BoxStack> {
  private ArrayList<Box> boxStack;
  private int height;
  private Random rand;

  BoxStack(ArrayList<Box> boxes, boolean hardStack) {
    this.height = 0;
    this.rand = new Random(System.nanoTime());
    createStack(boxes, hardStack);
  }

  /**
   * Deep clones the input list of boxes and then ranomises the orientation of each box.
   *
   * @param boxesToRandomise The boxes to clone and randomise.
   * @return A new randomised ArrayList of boxes.
   */
  static ArrayList<Box> randomiseBoxes(ArrayList<Box> boxesToRandomise) {
    ArrayList<Box> randomised = BoxStack.deepCloneBoxList(boxesToRandomise);
    for (Box b : randomised) {
      b.randomlyOrientate();
    }
    Collections.sort(randomised);
    return randomised;
  }

  /**
   * Deep clones the ArrayList of input boxes.
   *
   * @param boxes The ArrayList of boxes to deep clone.
   * @return The deep cloned list of boxes.
   */
  static ArrayList<Box> deepCloneBoxList(ArrayList<Box> boxes) {
    List<Box> deepClonedBoxList = boxes.stream().map(Box::new).collect(Collectors.toList());
    return new ArrayList<>(deepClonedBoxList);
  }

  /**
   * Creates a stack of boxes that don't violate the no-touching-faces rule.
   *
   * @param boxes An ArrayList of boxes to stack on each other.
   * @param hardStack Whether or not to rotate a box to try and fit it on the stack.
   */
  private void createStack(ArrayList<Box> boxes, boolean hardStack) {
    // make sure we're in an optimal order
    Collections.sort(boxes);

    this.boxStack = new ArrayList<>();
    Box previous = boxes.remove(0);
    boxStack.add(previous);
    this.height += previous.height;

    for (Box b : boxes) {
      if (!b.canFit(previous, !hardStack, true)) {
        continue;
      }

      // make sure we're only stacking when we can
      assert previous.width > b.width && previous.length > b.length;

      boxStack.add(b);
      previous = b;
      this.height += b.height;
    }
  }

  BoxStack breed(BoxStack partner) {
    // divide by 2 as we want to split somewhere in the middle of the two stacks
    int minBoxStack = Math.min(this.getNumBoxes(), partner.getNumBoxes());
    int maxBoxStack = Math.max(this.getNumBoxes(), partner.getNumBoxes());

    int splitIdx = 1 + rand.nextInt(minBoxStack - 1);

    // create the new stack, get the starting and ending stack
    ArrayList<Box> bredStack = new ArrayList<>();
    ArrayList<Box> startingStack =
        minBoxStack == this.getNumBoxes() ? this.boxStack : partner.boxStack;
    ArrayList<Box> endingStack =
        minBoxStack == this.getNumBoxes() ? partner.boxStack : this.boxStack;

    // add the boxes from the first stack
    for (int i = 0; i < splitIdx; i++) {
      bredStack.add(new Box(startingStack.get(i)));
    }

    // add the boxes from the second stack
    for (int j = splitIdx; j < maxBoxStack; j++) {
      bredStack.add(new Box(endingStack.get(j)));
    }

    // the boxes are sorted by area in the constructor
    return new BoxStack(bredStack, true);
  }

  /** Asserts the no-touching faces condition. */
  void auditStack() {
    // initialise to a null box
    Box prev = this.boxStack.get(0);

    //  check all of the boxes comply
    for (int i = 1; i < this.getNumBoxes(); i++) {
      Box nextBox = this.boxStack.get(i);

      // if the next box in the array is bigger than this one
      if (nextBox.width >= prev.width || nextBox.length >= prev.length) {
        System.out.println("offending orientation:");
        System.out.println("Previous:");
        prev.print();
        System.out.println("Current:");
        nextBox.print();
        System.out.println("Full stack:");
        this.printStack();
        throw new RuntimeException("Box stack does not comply");
      }
      prev = nextBox;
    }
  }

  /**
   * Goes through extraBoxes and tries to add boxes into this stack. Has conditions for reducing the
   * number of boxes to check each time.
   *
   * @param extraBoxes List of boxes to try and add into this stack.
   */
  void mutate(ArrayList<Box> extraBoxes) {
    int numBoxes = this.getNumBoxes();
    Box prev = this.boxStack.get(0);

    for (int i = 1; i < numBoxes; i++) {
      Box curr = this.boxStack.get(i);

      // there isn't room for a box to fit
      if (prev.width - curr.width <= 1 || prev.length - curr.length <= 1) {
        prev = curr;
        continue;
      }

      ArrayList<Box> boxesToDelete = new ArrayList<>();
      for (Box b : extraBoxes) {
        // if this box won't be able to fit anywhere else
        if (b.area >= prev.area) {
          // System.out.println("deleting box");
          boxesToDelete.add(b);
          continue;
        }

        // if we can fit a box between the two
        if (b.canFit(prev, true, true) && curr.canFit(b, false, false)) {

          // make sure we're only inserting when we can
          assert prev.width > b.width && prev.length > b.length;
          assert b.width > curr.width && b.length > curr.length;

          this.boxStack.add(i, b);

          // we now have more boxes to go over
          numBoxes++;

          // update our stack height
          this.height += b.height;

          // we're now trying to fit onto this box
          curr = b;
          break;
        }
      }

      // delete any box that we can't fit in anywhere
      for (Box toDelete : boxesToDelete) {
        extraBoxes.remove(toDelete);
      }

      prev = curr;
    }
  }

  // Removes any duplicate boxes from the stack.
  void removeDuplicates() {
    int numBoxes = this.getNumBoxes();
    int[] volList = new int[numBoxes];

    ArrayList<Box> boxesToRemove = new ArrayList<>();

    for (int i = 0; i < numBoxes; i++) {
      int newVol = this.boxStack.get(i).getVolume();
      volList[i] = newVol;
      for (int j = 0; j < i; j++) {
        if (volList[j] != newVol) {
          continue;
        }
        Box boxOne = this.boxStack.get(i);
        Box boxTwo = this.boxStack.get(j);
        if (boxOne.equals(boxTwo)) {
          boxesToRemove.add(boxOne.height > boxTwo.height ? boxTwo : boxOne);
        }
      }
    }
    for (Box toRemove : boxesToRemove) {
      this.boxStack.remove(toRemove);
    }
  }

  @Override
  // compares boxes based on descending top face area.
  public int compareTo(BoxStack boxStack) {
    int thisHeight = this.height;
    int thatHeight = boxStack.height;

    if (thisHeight == thatHeight) {
      return boxStack.getNumBoxes() - this.getNumBoxes();
    }

    return thatHeight - thisHeight;
  }

  // get the number of boxes in this stack.
  int getNumBoxes() {
    return this.boxStack.size();
  }

  // get the total height of this stack.
  int getHeight() {
    return this.height;
  }

  void printStack() {
    int totalHeight = 0;
    for (int i = this.boxStack.size() - 1; i >= 0; i--) {
      Box curr = this.boxStack.get(i);
      totalHeight += curr.height;
      System.out.println(curr.width + " " + curr.length + " " + curr.height + " " + totalHeight);
    }
  }
}
