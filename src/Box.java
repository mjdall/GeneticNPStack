import java.util.Random;

class Box implements Comparable<Box> {
  int width;
  int height;
  int length;
  int area;
  private Random rand;

  /**
   * Returns whether or not this box can fit onto another boxes face.
   *
   * @param onto - The box that we are trying to fit onto.
   * @param tryRollOver - If true, the box will also be tried in the rolled over position.
   * @return Whether or not the box can fit onto the face of `onto`.
   */
  boolean canFit(Box onto, boolean tryRollOver) {
    if (onto.width - this.width > 0 && onto.length - this.length > 0) {
      // it can fit the way it is - todo: Randomly return reorientated box
      return true;
    } else if (onto.width - this.length > 0 && onto.length - this.width > 0) {
      // we can turn it sideways and it will fit
      this.turnSideways();
      return true;
    }

    // if we can try roll it over to fit
    if (tryRollOver) {
      this.rollOver();
      return canFit(onto, false);
    }

    // the box won't fit in any orientation
    return false;
  }

  Box(Box copyFrom) {
    this.width = copyFrom.width;
    this.length = copyFrom.length;
    this.height = copyFrom.height;
    init();
  }

  Box(int[] dimensions) {
    assert dimensions.length == 3;
    this.width = dimensions[0];
    this.height = dimensions[1];
    this.length = dimensions[2];
    init();
  }

  private void init() {
    this.area = this.width * this.length;
    this.rand = new Random(System.nanoTime());
  }

  public boolean equals(Box b) {
    int compareVol = b.width * b.length * b.height;
    int thisVol = this.width * this.length * this.height;
    return compareVol == thisVol;
  }

  @Override
  public int compareTo(Box box2) {
    return box2.area - this.area;
  }

  void randomlyOrientate() {
    if (rand.nextBoolean()) {
      rollOver();
    }
    if (rand.nextBoolean()) {
      turnSideways();
    }
  }

  // Rolls a box over onto it's side.
  void rollOver() {
    // swap the w, l, d values
    int tmp = this.width;
    this.width = this.height;
    this.height = tmp;

    // find the new faces area
    this.area = this.width * this.length;
  }

  // Turns a box sideways.
  void turnSideways() {
    int tmp = this.width;
    this.width = this.length;
    this.length = tmp;
  }

  void print() {
    System.out.println(String.format("w:%d l:%d h:%d", this.width, this.length, this.height));
  }
}