package Connect4;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Disc extends Circle{

    public final boolean red;
    private static final int TILE_SIZE = 100;

    public Disc(boolean red) { // if false then we know the disc should be yellow.
        super(TILE_SIZE/2, red ? Color.RED : Color.YELLOW); // sets the size of the disc, and then a conditional for the color if the 'red' is true or not.
        this.red = red;

        // Also need to set the size of the circle to be the same as the slots in the grid.
        setCenterX(TILE_SIZE / 2); // setting the X starting point to the size of each tile halved.
        setCenterY(TILE_SIZE / 2);
    }
}
