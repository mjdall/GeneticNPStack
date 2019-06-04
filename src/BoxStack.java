import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

class BoxStack implements Comparable<BoxStack> {
  private ArrayList<Box> boxStack;
  private int height;
  private Random rand;

  BoxStack(ArrayList<Box> boxes, boolean hardStack) {
    this.rand = new Random(System.nanoTime());
    createStack(boxes, hardStack);
  }

  private void createStack(ArrayList<Box> boxes, boolean hardStack) {
    this.boxStack = new ArrayList<>();
    Box previous = boxes.remove(0);
    boxStack.add(previous);
    this.height += previous.height;

    for (Box b : boxes) {
      if (!b.canFit(previous, !hardStack)) {
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

    //            System.out.println("Stack One:");
    //            this.printStack();
    //            System.out.println("\nStack Two");
    //            partner.printStack();
    //            System.out.println("\n");

    // add the boxes from the first stack
    for (int i = 0; i < splitIdx; i++) {
      bredStack.add(new Box(startingStack.get(i)));
    }

    // add the boxes from the second stack
    for (int j = splitIdx; j < maxBoxStack; j++) {
      bredStack.add(new Box(endingStack.get(j)));
    }

    Collections.sort(bredStack);
    return new BoxStack(bredStack, true);
  }

  void auditStack() {
    // initialise to a null box
    Box prev = this.boxStack.get(0);

    //  check all of the boxes comply
    for (int i = 1; i < this.getNumBoxes(); i++) {
      Box nextBox = this.boxStack.get(i);

      // if the next box in the array is bigger than this one
      if (nextBox.width >= prev.width || nextBox.length >= prev.length) {
        System.out.println("Previous:");
        prev.print();
        System.out.println("Current:");
        nextBox.print();
        throw new RuntimeException("Box stack does not comply");
      }
      prev = nextBox;
    }
  }

  public void tryInsert(ArrayList<Box> extraBoxes) {
    ArrayList<Integer> possibleInserts = new ArrayList<>();
    Box prev = this.boxStack.get(0);
    int numBoxes = this.getNumBoxes();

    for (int i = 1; i < numBoxes; i++) {
      Box curr = this.boxStack.get(i);

      // there isn't any room for any boxes
      if (prev.width - curr.width >= 2 && prev.length - curr.length >= 2) {
        possibleInserts.add(i);
      }

      prev = curr;
    }

    System.out.println(possibleInserts.size() + "possible inserts");
  }

  @Override
  public int compareTo(BoxStack boxStack) {
    int thisHeight = this.height;
    int thatHeight = boxStack.height;

    if (thisHeight == thatHeight) {
      return boxStack.getNumBoxes() - this.getNumBoxes();
    }

    return thatHeight - thisHeight;
  }

  int getNumBoxes() {
    return this.boxStack.size();
  }

  int getHeight() {
    return this.height;
  }

  void printStack() {
    for (int i = this.boxStack.size() - 1; i >= 0; i--) {
      this.boxStack.get(i).print();
    }
    System.out.println(this.getNumBoxes() + " boxes with a height of " + this.height);
  }
}