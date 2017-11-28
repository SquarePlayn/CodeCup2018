import java.util.ArrayList;

class Main {

    public final static boolean DEBUG = true; // Contest: true (doesn't matter a lot)
    public final static boolean PRINTDEBUGTOSTERR = true; // Contest: true
    public final static boolean SINGLEMODE = true; // Contest: true
    public static final String[] BROWNCELLS = {"H1", "F2", "A3", "C4", "D5"}; //Only needed for non single mode

    public static final int DEFAULTSCORE = 75;
    public static final int TOTALCELLS = 36;
    public static final int BROWNCOINS = 5;
    public static final int ROWS = 8;
    public static final int COINS = 15;

    public final static int TURNS = 15;

    public static void main(String[] args) {
        if(SINGLEMODE) {
            new GameHandler(Strategy.HIGHESTOPEN).run();
        } else {
            experiment();
        }
    }

    private static void experiment() {
        int TESTCASES = 10000;
        int blueScore = 0;
        int redScore = 0;
        long blueTime = 0;
        long redTime = 0;
        for(int i=0; i<TESTCASES; i++) {
            Judge judge = new Judge(Strategy.RANDOM, Strategy.HIGHESTOPEN, BROWNCELLS);
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

enum Strategy {
    RANDOM, HIGHESTOPEN
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
        for(int i = 0; i< Main.TURNS; i++) {
            outputLine();
            inputLine();
        }

        Main.debug("[ERROR] We are after the mainloop, we are not supposed to be here!");
        Main.endGame();
    }

    public void preamble() {
        Main.debug("Reading preamble");
        //Read preamble brown cells
        for(int i = 0; i< Main.BROWNCOINS; i++) {
            String spot = Judge.readLine(this);
            board.setBrownSpot(spot, i);
        }
        turn = 0;
        canInput = true;
    }

    private void readFirstLine() {
        Main.debug("Reading first line");
        if(!canInput){
            Main.debug("[ERROR] Call to read first line while not ready to read");
            Main.endGame();
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
            Main.debug("[ERROR] Call to read line while not ready to read");
            Main.endGame();
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
            Main.debug("[ERROR] Call to output line while not ready to read");
            Main.endGame();
        }

        Judge.outputLine(ourColor, computeOutput());
        turn++;

        canInput = true;
        canOutput = false;
    }

    private void computeInput(String input, Color player) {
        if(input.equals("Quit")) {
            Main.endGame();
            return;
        }

        Cell cell = board.getCell(input.substring(0, 2));
        int value = Integer.parseInt(input.substring(3, input.length()));

        if(cell.getCoin() != null) {
            Main.debug("[ERROR] Wanted to set a coin that was already set");
            Main.endGame();
        } else if(value == 0 || value > 15) {
            Main.debug("[ERROR] Invalid coin value parsed");
            Main.endGame();
        } else {

            //Set the coin
            cell.setCoin(board.getCoin(player, value));
            board.getCoin(player, value).setSpot(cell);

        }
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
            default:
                Main.debug("[ERROR] Strategy Switch failed");
                Main.endGame();
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

        ArrayList<Coin> remainingCoins = board.getRemainingCoins(ourColor);
        Coin coin = remainingCoins.get(remainingCoins.size()-1);

        return mostOpenCell.getName()+"="+coin.getValue();
    }

    public Board getBoard() {
        return board;
    }
}

class Board {

    private ArrayList<Cell> allCells = new ArrayList<>();
    private ArrayList<ArrayList<Cell>> cells = new ArrayList<>();
    private Coin[] redCoins = new Coin[Main.COINS];
    private Coin[] blueCoins = new Coin[Main.COINS];
    private Coin[] brownCoins = new Coin[Main.BROWNCOINS];

    public void buildBoard() {
        if(cells.isEmpty()) {
            Main.debug("Building board");
            buildCells();
            buildConnections();
            buildCoins();
            Main.debug("Building board complete");
        }
    }

    private void buildCoins() {
        for(int i = 1; i<= Main.COINS; i++) {
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

        for(int i = 0; i< Main.ROWS; i++) {
            ArrayList<Cell> newRow = new ArrayList<>();
            for(int j = 0; j< Main.ROWS-i; j++) {
                Cell newCell = new Cell(i, j);
                newRow.add(newCell);
                allCells.add(newCell);
            }
            cells.add(newRow);
        }
    }

    private void buildConnections() {
        for(int i = 0; i< Main.ROWS; i++) {
            for(int j = 0; j< Main.ROWS-i; j++) {
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
                if(j< Main.ROWS-i-1){
                    cell.getAdj().add(getCell(i, j+1));
                }

                //Down
                if(i< Main.ROWS-1){
                    //Left down
                    if(j>0) {
                        cell.getAdj().add(getCell(i+1, j-1));
                    }

                    //Right down
                    if(j< Main.ROWS-i-1) {
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
        int score = Main.DEFAULTSCORE;
        for(Cell cell: adj){
            if(cell.getCoin().getColor() == color) {
                score += cell.getCoin().getValue();
            } else if(cell.getCoin().getColor() == Color.BLUE || cell.getCoin().getColor() == Color.RED){
                score -= cell.getCoin().getValue();
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