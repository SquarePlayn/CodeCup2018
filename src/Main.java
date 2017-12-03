import java.util.ArrayList;
import java.util.Random;

class Main {

    public final static boolean DEBUG = false; // Contest: true (doesn't matter a lot)
    public final static boolean PRINTDEBUGTOSTERR = false; // Contest: true
    public final static boolean SINGLEMODE = false; // Contest: true
    public static final String[] BROWNCELLS = {"H1", "F2", "A3", "C4", "D5"}; //Only needed for non single mode

    public static final int DEFAULTSCORE = 75;
    public static final int TOTALCELLS = 36;
    public static final int BROWNCOINS = 5;
    public static final int ROWS = 8;
    public static final int COINS = 15;

    public final static int TURNS = 15;

    public static void main(String[] args) {
        if(SINGLEMODE) {
            new GameHandler(Strategy.COMBINE_MAIN).run();
        } else {
            experiment();
        }
    }

    private static String[] getRandomBrownCells() {
        ArrayList<String> brownCells = new ArrayList<>();
        String[] returns = new String[BROWNCOINS];
        Random rand = new Random();
        for(int i=0; i<BROWNCOINS; i++) {
            String brownCell;
            do {
                char letter = 'A';
                int rLet = rand.nextInt(TOTALCELLS);
                int rowCellsLeft = ROWS;
                while (rLet >= rowCellsLeft) {
                    rLet -= rowCellsLeft;
                    rowCellsLeft--;
                    letter++;
                }
                brownCell = letter+""+(rLet+1);
            }while(brownCells.contains(brownCell));
            brownCells.add(brownCell);
            returns[i] = brownCell;
        }

        return returns;
    }

    private static void experiment() {
        int TESTCASES = 10000;
        int blueScore = 0;
        int redScore = 0;
        long blueTime = 0;
        long redTime = 0;
        for(int i=0; i<TESTCASES; i++) {
            Judge judge = new Judge(Strategy.COMBINE_MAIN, Strategy.LEASTLOSS, getRandomBrownCells());
            judge.run();
            redScore += judge.getScore(Color.RED);
            blueScore += judge.getScore(Color.BLUE);
            redTime += judge.getTime(Color.RED);
            blueTime += judge.getTime(Color.BLUE);
            /*
            System.out.println("Red Points: " + judge.getScore(Color.RED));
            System.out.println("Blue Points: " + judge.getScore(Color.BLUE));
            System.out.println("Red Time: " + judge.getTime(Color.RED));
            System.out.println("Blue Time: " + judge.getTime(Color.BLUE));
            */

        }
        blueScore /= TESTCASES;
        redScore /= TESTCASES;
        blueTime /= TESTCASES;
        redTime /= TESTCASES;
        System.out.println("Scores: ["+redScore+"|"+blueScore+"]");
        System.out.println("Times: ["+redTime+"|"+blueTime+"]");
    }

    public static void endGame() {
        debug("Game exiting");
        System.exit(2);
    }

    public static void debug(String message) {
        if(DEBUG) {
            if(PRINTDEBUGTOSTERR) {
                System.err.println(message);
            } else {
                System.out.println("[D] " + message);
            }
        }
    }

}
