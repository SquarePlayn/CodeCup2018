import java.util.ArrayList;
import java.util.Scanner;
import java.util.Random;

// CodeCup has no javafx.util.Pair so made my own
class Pair<O1, O2> {

    O1 key;
    O2 value;

    Pair(O1 key, O2 value) {
        this.key = key;
        this.value = value;
    }

    public O1 getKey() {
        return key;
    }

    public O2 getValue() {
        return value;
    }
}

class Board {

    private ArrayList<Cell> allCells = new ArrayList<>();
    private ArrayList<ArrayList<Cell>> cells = new ArrayList<>();
    private Coin[] redCoins = new Coin[SuperNova.COINS];
    private Coin[] blueCoins = new Coin[SuperNova.COINS];
    private Coin[] brownCoins = new Coin[SuperNova.BROWNCOINS];

    public void buildBoard() {
        if(cells.isEmpty()) {
            SuperNova.debug("Building board");
            buildCells();
            buildConnections();
            buildCoins();
            SuperNova.debug("Building board complete");
        }
    }

    private void buildCoins() {
        for(int i = 1; i<= SuperNova.COINS; i++) {
            redCoins[i-1] = new Coin(Color.RED, i);
            blueCoins[i-1] = new Coin(Color.BLUE, i);
        }
    }

    public void setBrownSpot(String spot, int i) {
        brownCoins[i] = new Coin(Color.BROWN, 0);
        brownCoins[i].setSpot(getCell(spot));
        getCell(spot).setCoin(brownCoins[i]);
    }

    private void buildCells() {

        for(int i = 0; i< SuperNova.ROWS; i++) {
            ArrayList<Cell> newRow = new ArrayList<>();
            for(int j = 0; j< SuperNova.ROWS-i; j++) {
                Cell newCell = new Cell(i, j);
                newRow.add(newCell);
                allCells.add(newCell);
            }
            cells.add(newRow);
        }
    }

    private void buildConnections() {
        for(int i = 0; i< SuperNova.ROWS; i++) {
            for(int j = 0; j< SuperNova.ROWS-i; j++) {
                Cell cell = getCell(i, j);

                //Up
                if(i>0) {
                    cell.getAdj().add(getCell(i-1, j));
                    cell.getAdj().add(getCell(i-1, j+1));
                }

                //Left
                if(j>0) {
                    cell.getAdj().add(getCell(i, j-1));
                }

                //Right
                if(j< SuperNova.ROWS-i-1){
                    cell.getAdj().add(getCell(i, j+1));
                }

                //Down
                if(i< SuperNova.ROWS-1){
                    //Left down
                    if(j>0) {
                        cell.getAdj().add(getCell(i+1, j-1));
                    }

                    //Right down
                    if(j< SuperNova.ROWS-i-1) {
                        cell.getAdj().add(getCell(i+1, j));
                    }
                }
            }
        }
    }

    public Cell getCell(int i, int j) {

        return cells.get(i).get(j);
    }

    public Cell getCell(String name) {
        char letter = name.charAt(0);
        int number = name.charAt(1)-'0';
        return getCell(letter-'A', number-1);
    }

    public void printBoard() {
        for(ArrayList<Cell> row: cells) {
            for(Cell cell: row) {
                System.out.print(cell.getName()+" ");
            }
            System.out.println("");
        }
    }

    public Coin getHighestRemainingCoin(Color color) {
        ArrayList<Coin> remainingCoins = getRemainingCoins(color);
        return remainingCoins.get(remainingCoins.size()-1);
    }

    public ArrayList<Cell> getAllCells() {
        return allCells;
    }

    public ArrayList<ArrayList<Cell>> getCells() {
        return cells;
    }

    public Coin[] getCoins(Color color){
        if(color == Color.RED) {
            return redCoins;
        } else if(color == Color.BLUE){
            return blueCoins;
        } else {
            return brownCoins;
        }
    }

    public Coin getCoin(Color color, int value) {
        return getCoins(color)[value-1];
    }

    public ArrayList<Cell> getEmptyCells() {
        ArrayList<Cell> emptyCells = new ArrayList<>();
        for(Cell cell: allCells) {
            if(cell.getCoin() == null) {
                emptyCells.add(cell);
            }
        }
        return emptyCells;
    }

    public ArrayList<Coin> getRemainingCoins(Color color) {
        ArrayList<Coin> remainingCoins = new ArrayList<>();
        Coin[] coins = getCoins(color);
        for(Coin coin: coins) {
            if(coin.getSpot() == null) {
                remainingCoins.add(coin);
            }
        }
        return remainingCoins;
    }
}

class Cell {

    private final int i, j;
    private final ArrayList<Cell> adj = new ArrayList<>();
    private Coin coin;

    public Cell(int i, int j) {
        this.i = i;
        this.j = j;
    }

    public String getName() {
        String let = ""+(char)('A'+i);
        return let+(j+1);
    }

    public void printAdjacent() {
        for(Cell cell: adj) {
            System.out.print(cell.getName()+" ");
        }
        System.out.println("");
    }

    public void setCoin(Coin coin) {
        this.coin = coin;
    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    public ArrayList<Cell> getAdj() {
        return adj;
    }

    public Coin getCoin() {
        return coin;
    }

    public int getScore(Color color) {
        int score = SuperNova.DEFAULTSCORE;
        for(Cell cell: adj){
            if(cell.getCoin() != null) {
                if (cell.getCoin().getColor() == color) {
                    score += cell.getCoin().getValue();
                } else if (cell.getCoin().getColor() == Color.BLUE || cell.getCoin().getColor() == Color.RED) {
                    score -= cell.getCoin().getValue();
                }
            }
        }
        return score;
    }
}

enum Color {
    RED, BLUE, BROWN
}

class Coin {

    private final Color color;
    private final int value;
    private Cell spot;

    public Coin(Color color, int value) {
        this.color = color;
        this.value = value;
    }

    public void setSpot(Cell spot) {
        this.spot = spot;
    }

    public Color getColor() {
        return color;
    }

    public int getValue() {
        return value;
    }

    public Cell getSpot() {
        return spot;
    }

}

enum Strategy {
    RANDOM, HIGHESTOPEN, LEASTLOSS, COMBINE_MAIN, COMBINE_TEST, MINMAX
}

class GameHandler {

    private final Strategy strategy;

    private Color ourColor;
    private Color oppColor;

    private final Board board = new Board();

    private int turn = -1;

    private boolean canOutput = false;
    private boolean canInput = false;

    public GameHandler(Strategy strategy) {
        this.strategy = strategy;
        board.buildBoard();
    }

    public void run() {
        preamble();
        readFirstLine();

        //Make all turns
        for(int i = 0; i< SuperNova.TURNS; i++) {
            outputLine();
            inputLine();
        }

        SuperNova.debug("[ERROR] We are after the mainloop, we are not supposed to be here!");
        SuperNova.endGame();
    }

    public void preamble() {
        SuperNova.debug("Reading preamble");
        //Read preamble brown cells
        for(int i = 0; i< SuperNova.BROWNCOINS; i++) {
            String spot = Judge.readLine(this);
            board.setBrownSpot(spot, i);
        }
        turn = 0;
        canInput = true;
    }

    private void readFirstLine() {
        SuperNova.debug("Reading first line");
        if(!canInput){
            SuperNova.debug("[ERROR] Call to read first line while not ready to read");
            SuperNova.endGame();
        }

        // Handle first line of input ("Start" or Assignment)
        String nextLine = Judge.readLine(this);
        if(nextLine.equals("Start")) {
            //We are red
            ourColor = Color.RED;
            oppColor = Color.BLUE;
        } else {
            ourColor = Color.BLUE;
            oppColor = Color.RED;
            computeInput(nextLine, oppColor);
        }

        canOutput = true;
        canInput = false;
    }

    public void inputLine() {
        if(!canInput){
            SuperNova.debug("[ERROR] Call to read line while not ready to read");
            SuperNova.endGame();
        }

        if(turn == 0) {
            readFirstLine();
        } else {
            computeInput(Judge.readLine(ourColor), oppColor);

            canOutput = true;
            canInput = false;
        }
    }

    public void outputLine() {
        if(!canOutput){
            SuperNova.debug("[ERROR] Call to output line while not ready to read");
            SuperNova.endGame();
        }

        Judge.outputLine(ourColor, computeOutput());
        turn++;

        canInput = true;
        canOutput = false;
    }

    private void computeInput(String input, Color player) {
        if(input.equals("Quit")) {
            SuperNova.endGame();
            return;
        }

        Cell cell = board.getCell(input.substring(0, 2));
        int value = Integer.parseInt(input.substring(3, input.length()));

        if(cell.getCoin() != null) {
            SuperNova.debug("[ERROR] Wanted to set a coin that was already set");
            SuperNova.endGame();
        } else if(value == 0 || value > 15) {
            SuperNova.debug("[ERROR] Invalid coin value parsed");
            SuperNova.endGame();
        } else {

            //Set the coin
            cell.setCoin(board.getCoin(player, value));
            board.getCoin(player, value).setSpot(cell);

        }
    }

    public Board getBoard() {
        return board;
    }

    private String computeOutput() {
        String output = "";
        switch (strategy) {
            case RANDOM:
                output = computeOutputRandom();
                break;
            case HIGHESTOPEN:
                output = computeOutputHighFree();
                break;
            case LEASTLOSS:
                output = computeOutputLeastLoss();
                break;
            case MINMAX:
                output = computeOutputMinMax();
                break;
            case COMBINE_MAIN:
                output = computeOutputCombinedMain();
                break;
            case COMBINE_TEST:
                output = computeOutputCombinedTest();
                break;
            default:
                SuperNova.debug("[ERROR] Strategy Switch failed");
                SuperNova.endGame();
                break;
        }

        //Actually set the info (and check if it is correct)
        computeInput(output, ourColor);

        return output;
    }

    private String computeOutputRandom() {

        //Get a random empty cell
        ArrayList<Cell> emptyCells = board.getEmptyCells();
        Cell cell = emptyCells.get((int)(Math.random()*emptyCells.size()));

        //Get a random value
        ArrayList<Coin> remainingCoins = board.getRemainingCoins(ourColor);
        Coin coin = remainingCoins.get((int)(Math.random()*remainingCoins.size()));

        return cell.getName()+"="+coin.getValue();
    }

    private String computeOutputHighFree() {
        //Get the most open cell
        Cell mostOpenCell = board.getEmptyCells().get(0);
        int maxOpenness = Integer.MIN_VALUE;
        for(Cell cell: board.getEmptyCells()) {
            int numEmpty = 0;
            for(Cell empty: cell.getAdj()) {
                if(empty.getCoin() == null) {
                    numEmpty++;
                }
            }
            if(numEmpty > maxOpenness) {
                maxOpenness = numEmpty;
                mostOpenCell = cell;
            }
        }

        Coin coin = board.getHighestRemainingCoin(ourColor);

        return mostOpenCell.getName()+"="+coin.getValue();
    }

    private String computeOutputLeastLoss() {
        //Put a high number in the spot that leads to the least decrease of score
        Cell leastDecreseCell = board.getEmptyCells().get(0);
        int leastDecrease = Integer.MAX_VALUE;
        for(Cell cell: board.getEmptyCells()) {
            int score = cell.getScore(ourColor);
            if(score < leastDecrease) {
                leastDecrease = score;
                leastDecreseCell = cell;
            }
        }

        Coin coin = board.getHighestRemainingCoin(ourColor);

        return leastDecreseCell.getName()+"="+coin.getValue();
    }

    private String computeOutputMinMax() {
        return minMax(ourColor).getValue();
    }

    private Pair<Integer, String> minMax(Color turn) {

        //Detect end of game
        if(board.getRemainingCoins(turn).isEmpty()) {
            return new Pair<>(board.getEmptyCells().get(0).getScore(ourColor), "");
        }

        boolean ourTurn = turn == ourColor;

        int bestScore = ourTurn ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        String bestDecision = "";

        for(int c=0; c < board.getEmptyCells().size(); c++) {
            Cell cell = board.getEmptyCells().get(c);
            for(int i=0; i<board.getRemainingCoins(turn).size(); i++) {
                Coin coin = board.getRemainingCoins(turn).get(i);
                cell.setCoin(coin);
                coin.setSpot(cell);
                int score = minMax(ourTurn ? oppColor : ourColor).getKey();
                if(ourTurn) {
                    if(score > bestScore) {
                        bestScore = score;
                        bestDecision = cell.getName() + "=" + coin.getValue();
                    }
                } else {
                    if(score < bestScore) {
                        bestScore = score;
                        bestDecision = cell.getName() + "=" + coin.getValue();
                    }
                }
                cell.setCoin(null);
                coin.setSpot(null);
            }
        }

        return new Pair<>(bestScore, bestDecision);

    }

    private String computeOutputCombinedMain() {
        if(turn < 7) {
            return computeOutputHighFree();
        } else {
            return computeOutputLeastLoss();
        }
    }

    private String computeOutputCombinedTest() {
        if(turn < 7) {
            return  computeOutputHighFree();
        } else if(turn < 12) {
            return computeOutputLeastLoss();
        } else {
            return computeOutputMinMax();
        }
    }
}

class Judge {

    private static GameHandler gameRed;
    private static GameHandler gameBlue;

    private static Scanner scanner = new Scanner(System.in);

    private static ArrayList<String> redInput;
    private static ArrayList<String> blueInput;

    private static int redTime;
    private static int blueTime;
    private static long redTrackTime;
    private static long blueTrackTime;

    private boolean finished = false;

    public Judge(Strategy stratRed, Strategy stratBlue, String[] brownCells) {
        gameRed = new GameHandler(stratRed);
        gameBlue = new GameHandler(stratBlue);

        redInput = new ArrayList<>();
        blueInput = new ArrayList<>();
        for(String brownCell: brownCells) {
            redInput.add(brownCell);
            blueInput.add(brownCell);
        }
        redInput.add("Start");

        redTime = 0;
        blueTime = 0;

    }

    public void run() {
        startTime(Color.RED);
        gameRed.preamble();
        stopTime(Color.RED);

        startTime(Color.BLUE);
        gameBlue.preamble();
        stopTime(Color.BLUE);

        for(int turn = 0; turn< SuperNova.TURNS; turn++) {
            startTime(Color.RED);
            gameRed.inputLine();
            gameRed.outputLine();
            stopTime(Color.RED);

            startTime(Color.BLUE);
            gameBlue.inputLine();
            gameBlue.outputLine();
            stopTime(Color.BLUE);
        }

        //Clean up last entry for completeness and checks
        if(redInput.isEmpty()) {
            SuperNova.debug("[ERROR] Judge failed: no 1 input left in redInput after last turn");
            SuperNova.endGame();
        }
        redInput.remove(0);
        if(!redInput.isEmpty() || !blueInput.isEmpty()) {
            SuperNova.debug("[ERROR] Judge failed: One input is not empty after all turns");
            SuperNova.endGame();
        }

        //Append "Quit"s for completeness
        redInput.add("Quit");
        blueInput.add("Quit");

        finished = true;
    }

    public static String readLine(GameHandler game) {
        if(SuperNova.SINGLEMODE) {
            return readLine(Color.BROWN);
        } else if(game.equals(gameRed)) {
            return readLine(Color.RED);
        } else if(game.equals(gameBlue)) {
            return readLine(Color.BLUE);
        } else {
            SuperNova.debug("[ERROR] We should not end up here, readline of gamehandler failed");
            return readLine(Color.BROWN);
        }

    }

    public static String readLine(Color player) {
        if(SuperNova.SINGLEMODE) {
            return scanner.nextLine();
        } else {
            ArrayList<String> input;
            if(player == Color.RED) {
                input = redInput;
            } else {
                input = blueInput;
            }

            if(input.isEmpty()){
                SuperNova.debug("[ERROR] Judge failed: readLine requested while player had no line to read");
                SuperNova.endGame();
            }

            //Remove the newest line from the input it had left
            return input.remove(0);
        }
    }

    public static void outputLine(Color player, String output) {
        if(SuperNova.SINGLEMODE) {
            System.out.println(output);
        } else {
            ArrayList<String> oppInput;
            if(player == Color.RED) {
                oppInput = blueInput;
            } else if(player == Color.BLUE) {
                oppInput = redInput;
            } else {
                SuperNova.debug("[ERROR]OutputLine got invalid player color");
                SuperNova.endGame();
                return;
            }

            if(!oppInput.isEmpty()) {
                SuperNova.debug("[ERROR] Judge failed: outputLine requested while player is not ready to write, other player is behind.");
                SuperNova.endGame();
            }

            oppInput.add(output);
        }
    }

    private void startTime(Color color) {
        if(color == Color.RED) {
            redTrackTime = getTime();
        } else if(color == Color.BLUE) {
            blueTrackTime = getTime();
        } else {
            SuperNova.debug("[ERROR] Wrong color passed to startTime in Judge");
            SuperNova.endGame();
        }
    }

    private void stopTime(Color color) {
        if(color == Color.RED) {
            redTime += getTime() - redTrackTime;
        } else if(color == Color.BLUE) {
            blueTime += getTime() - blueTrackTime;
        } else {
            SuperNova.debug("[ERROR] Wrong color passed to stopTime in Judge");
            SuperNova.endGame();
        }
    }

    public long getTime() {
        return System.currentTimeMillis();
    }

    public int getScore(Color color) {
        if(!finished) {
            SuperNova.debug("[ERROR] Call to see score made before game was finished");
            SuperNova.endGame();
        }

        GameHandler game;
        if(color == Color.RED) {
            game = gameRed;
        } else if(color == Color.BLUE) {
            game = gameBlue;
        } else {
            SuperNova.debug("[ERROR] Asked for score of invalid color");
            SuperNova.endGame();
            return 0;
        }

        return game.getBoard().getEmptyCells().get(0).getScore(color);
    }

    public int getTime(Color color) {
        if(!finished) {
            SuperNova.debug("[ERROR] Call to see time made before game was finished");
            SuperNova.endGame();
        }

        if(color == Color.RED) {
            return redTime;
        } else if(color == Color.BLUE) {
            return blueTime;
        } else {
            SuperNova.debug("[ERROR] Asked for time of invalid color");
            SuperNova.endGame();
            return 0;
        }
    }
}

class SuperNova {

    public final static boolean DEBUG = true; // Contest: true (doesn't matter a lot)
    public final static boolean PRINTDEBUGTOSTERR = true; // Contest: true
    public final static boolean SINGLEMODE = true; // Contest: true
    public static final String[] BROWNCELLS = {"H1", "F2", "A3", "C4", "D5"}; //Only needed for non single mode
    private static final boolean RANDOM_BROWNCELLS = true; // Only for non single mode

    private static final Strategy STRAT_ONE = Strategy.LEASTLOSS;    // Compare mode Red Strat
    private static final Strategy STRAT_TWO = Strategy.COMBINE_TEST;       // Compare mode Blue Strat
    private static final Strategy STRAT_SINGLE = Strategy.COMBINE_TEST; // Single mode Strat
    private static final int TESTCASES = 5; // Amount of testcases in experimental mode

    public static final int DEFAULTSCORE = 75;
    public static final int TOTALCELLS = 36;
    public static final int BROWNCOINS = 5;
    public static final int ROWS = 8;
    public static final int COINS = 15;

    public final static int TURNS = 15;

    public static void main(String[] args) {
        if(SINGLEMODE) {
            new GameHandler(STRAT_SINGLE).run();
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
        int blueScore = 0;
        int redScore = 0;
        long blueTime = 0;
        long redTime = 0;
        for(int i=0; i<TESTCASES; i++) {
            String[] brownCells = RANDOM_BROWNCELLS ? getRandomBrownCells() : BROWNCELLS;
            Judge judge = new Judge(STRAT_ONE, STRAT_TWO, brownCells);
            judge.run();
            redScore += judge.getScore(Color.RED);
            blueScore += judge.getScore(Color.BLUE);
            redTime += judge.getTime(Color.RED);
            blueTime += judge.getTime(Color.BLUE);
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
