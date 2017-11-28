import java.util.ArrayList;

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
