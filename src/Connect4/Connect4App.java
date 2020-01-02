package Connect4;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Connect4App extends Application {

    private static final int TILE_SIZE = 100;
    private static final int COLUMNS = 7; // there are 7 columns in connect 4.
    private static final int ROWS = 6; // there are 6 rows in connect 4

    private boolean redMove = true; // Red starts the game. If this is false, then it is Yellow's turn to move.
    private Disc[][] gridOfDiscs = new Disc[COLUMNS][ROWS]; // This grid has Columns width and Rows height.

    private Pane root = new Pane(); // made this a field so all methods can manipulate it. Before I couldn't due to scope.
    private Pane discRoot = new Pane();



    /**
     * Create the JavaFX scene that the user interacts with.
     * @return
     */
    private Parent createContent() {

        root.getChildren().add(discRoot); // placing the add here allows the discs to have a nice overlay on the grid

        Shape gameGrid = makeGrid(); // We will create a grid in the makeGrid method and store it in here.

        root.getChildren().add(gameGrid); // Add our grid to the root

        root.getChildren().addAll(userHoverColumns()); // Add the hover box for the columns to the root

        return root;
    }


    /**
     * Method that makes the grid we drop discs into.
     * @return
     */
    private Shape makeGrid() {

        Shape grid = new Rectangle((COLUMNS + 1.7) * TILE_SIZE, (ROWS + 1.7) * TILE_SIZE); // setting the width and height for the rectangle.

        // using y and x within the for loop as they correspond with columns and rows. Makes it easier to understand.
        // making the circles in here.
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLUMNS; x++) {
                Circle slots = new Circle(TILE_SIZE/2);
                slots.setCenterX(TILE_SIZE / 2); // setting the X starting point to the size of each tile halved.
                slots.setCenterY(TILE_SIZE / 2);

                slots.setTranslateX(x * (TILE_SIZE + 20) + TILE_SIZE / 4); // to get the spaces between the circles (rows). The TILE_SIZE/4 is dealing with the offset.
                slots.setTranslateY(y * (TILE_SIZE + 20) + TILE_SIZE / 4); // to get the spaces between the circles (columns)

                // when we make a circle, we must subtract from the rectangle grid that we had. We are making holes/slots in that grid.
                // we are not actually making circle objects here. Just removing the shape (with given dimensions) from the grid, effectively leaving the slots.
                grid = grid.subtract(grid, slots);
            }
        }


        Light.Distant light  = new Light.Distant(); // creating the light that will shine onto the grid.
        light.setAzimuth(45.0);
        light.setElevation(30.0);

        Lighting lighting = new Lighting(); // using the created light and applying it to the grid.
        lighting.setLight(light);
        lighting.setSurfaceScale(15.0); // raises light around border of circles in the grid.

        grid.setFill(Color.DODGERBLUE);
        grid.setEffect(lighting);
        return grid;
    }


    /**
     * Method that allows the user to hover over a column in the grid and see what column they are hovering over.
     */
    private ArrayList<Rectangle> userHoverColumns() { /* MAKE THIS CHANGE COLOR BASED ON WHICH USER TURN IT IS. FROM RED TO YELLOW */ // difficult because the color setting only happens at the start.

        ArrayList<Rectangle> hover = new ArrayList<>();

        for (int x = 0; x < COLUMNS; x++) {
            Rectangle hoverBox = new Rectangle(TILE_SIZE, (ROWS + 1.7) * TILE_SIZE); // creating the box that will show upon hovering. Setting the width and height.
            hoverBox.setTranslateX(x * (TILE_SIZE + 20) + TILE_SIZE / 4); //
            hoverBox.setFill(Color.TRANSPARENT); // make the box have no fill to start with. It is invisible if not in use by the user.

            hoverBox.setOnMouseEntered(eventOccurs -> hoverBox.setFill(Color.rgb(255, 255, 255, 0.17))); // lambda function. When the event occurs, set the fill colour to this.
            hoverBox.setOnMouseExited(eventOccurs -> hoverBox.setFill(Color.TRANSPARENT)); // when the mouse leaves the given box, leave

            final int column = x; // this is the column the user will be hovering over.
            hoverBox.setOnMouseClicked(eventOccurs -> dropDisc(new Disc(redMove), column)); // when the user clicks, run the dropDisk method.
            // If redMove is false, then the Disc class will set the color to yellow thanks to our conditional statement

            hover.add(hoverBox);
        }

        return hover;
    }


    /**
     * Method that controls the disc dropping into the grid.
     * @param disc
     * @param column
     */
    private void dropDisc(Disc disc, int column) {

        int row = ROWS -1; // We want to add discs so it falls down, so we need to start count from the maximum value to 0.

        do {
            if (!getDisc(column, row).isPresent()) { // Basically if there is already a disc inside the grid place we are looking at, then break out of the Do-While Loop.
                break;
            }
            row--;
        } while(row >= 0); // Keep doing the above code until row is less than 0.

        if (row < 0) {
            return; // We failed to find a row which is empty
        }

        gridOfDiscs[column][row] = disc;
        discRoot.getChildren().add(disc); // add the disc to the root.
        disc.setTranslateX(column * (TILE_SIZE + 20) + TILE_SIZE / 4);

        final int currentRow = row;

        TranslateTransition animation = new TranslateTransition(Duration.seconds(0.5), disc); // animation that controls the disc dropping.
        animation.setToY(row * (TILE_SIZE + 20) + TILE_SIZE / 4); // we want the animation/disc to drop vertically along the Y axis.
        animation.setOnFinished(eventOccurs -> { // when the animation finishes
            if (gameEnd(column, currentRow)) { // if we are at the end of the game when the animation finishes, then run the gameOver method.
                gameOver();
            }

            redMove = !redMove; // switch to yellow turn.
        });

        animation.play(); // runs the animation. Without this it won't work.
    }


    /**
     * Method to check if the game has ended.
     * @return a boolean True if the game has ended.
     */
    public boolean gameEnd(int column, int row) {

        List<Point2D> vertical = IntStream.rangeClosed(row - 3, row + 3) // a Stream is a sequence of elements supporting aggregate operations. Vertical uses Rows.
                .mapToObj(r -> new Point2D(column, r)) // mapping the stream of rows to a Point, (X as column, r as row)
                .collect(Collectors.toList()); // store this as a list, and put it into the list called vertical.


        List<Point2D> horizontal = IntStream.rangeClosed(column - 3, column + 3) // a Stream is a sequence of elements supporting aggregate operations. Horizontal uses columns.
                .mapToObj(c -> new Point2D(c, row))
                .collect(Collectors.toList());

        Point2D topLeft = new Point2D(column - 3, row - 3); // column - 3 is giving us the left point, row - 3 is giving us the top point.

        List<Point2D> diagonal1 = IntStream.rangeClosed(0, 6) // the possible slots in the grid that the discs can drop into.
                .mapToObj(i -> topLeft.add(i,i)) // 'i' will iterate through 0-6, checking from top left to bottom right for chains.
                .collect(Collectors.toList());

        Point2D bottomLeft = new Point2D(column - 3, row + 3); // column - 3 is giving us the left point, row - 3 is giving us the top point.

        List<Point2D> diagonal2 = IntStream.rangeClosed(0, 6) // the possible slots in the grid that the discs can drop into.
                .mapToObj(i -> bottomLeft.add(i,-i)) // 'i' will iterate through 0-6, checking from bottom left to top right for chains.
                .collect(Collectors.toList());

        return chainLength(vertical) || chainLength(horizontal) || chainLength(diagonal1) || chainLength(diagonal2); // returns true if a chain has occurred on any of these.
    }


    /**
     * Helper method for gameEnd which checks if there has been a chain of 4 discs.
     * @param points
     * @return
     */
    public boolean chainLength(List<Point2D> points) { // Point2D gives a 2D geometric point. We are passing in a list of x,y coordinates as 'points'.

        int chain = 0;

        for (Point2D point : points) {
            int column = (int) point.getX();
            int row = (int) point.getY();

            Disc disc = getDisc(column, row).orElse(new Disc(!redMove)); //get the disc at the Point2D position (the given x,y). orElse gets called if there is no disc at that position, so we supply a new disc. Using orElse because it returns an Optional.
            if (disc.red == redMove) { // if it is reds move. Checking if the 'red' boolean in the Disc class is true and equal to redMove. If it is, then the current disc is red. If it is false, and redMove is false, then we know it is Yellows turn.
                chain++;
                if (chain == 4) {
                    return true;
                }
            }
            else { // this must be here rather than an else to the above if statement. Otherwise, it will always reset chain to 0 after incrementing. Chain will never get to reach 4.
                chain = 0;
            }
        }

        return false; // if we reach here, we haven't found a chain of length 4.
    }


    /**
     * Method that ends the game.
     */
    public void gameOver() {


        Shape gameOverOverlay = new Rectangle((COLUMNS + 1.7) * TILE_SIZE, (ROWS + 1.7) * TILE_SIZE);
        gameOverOverlay.setFill(Color.BLACK);



        Text text = new Text(85,375, "GAME OVER");
        Text subText = new Text(180, 450, "WINNER:" + (redMove ? "RED PLAYER" : "YEL PLAYER"));
        text.setStyle("-fx-font-family: 'Press Start 2P', cursive; -fx-font-size: 80;");
        subText.setStyle("-fx-font-family: 'Press Start 2P', cursive; -fx-font-size: 30;");
        text.setFill(Color.WHITE);
        subText.setFill(redMove ? Color.RED : Color.YELLOW);


        root.getChildren().add(gameOverOverlay);
        root.getChildren().add(text);
        root.getChildren().add(subText);
    }


    /**
     * Method that ensures the disc can be dropped into the grid.
     * @param column
     * @param row
     * @return
     */
    private Optional<Disc> getDisc(int column, int row) { // Using Optional here as it can return null if there is nothing in the column, or true if there is. Column represents X, row Y.

        if(column < 0 || column >= COLUMNS || row < 0 || row >= ROWS) {
            return Optional.empty(); // saying there is noting we can return in this case.
        }
        return Optional.ofNullable(gridOfDiscs[column][row]); // if there is nothing to put in, then it will remain empty, if not, then place given parameter integers in the gridOfDiscs.
    }



    @Override
    public void start (Stage stage) throws Exception {

        root.getStylesheets().add("https://fonts.googleapis.com/css?family=Press+Start+2P&display=swap");
        stage.setScene(new Scene(createContent())); // run the createContent method on the stage.
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

}
