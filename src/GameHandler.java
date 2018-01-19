import java.util.ArrayList;
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

enum Strategy {
    RANDOM, HIGHESTOPEN, LEASTLOSS, COMBINE_MAIN, COMBINE_TEST, NEURAL_NET, MINMAX
}

class GameHandler {

    private final Strategy strategy;

    private Color ourColor;
    private Color oppColor;

    private final Board board = new Board();

    private int turn = -1;

    private boolean canOutput = false;
    private boolean canInput = false;

    private NeuralNetwork neuralNetwork;

    public GameHandler(Strategy strategy, NeuralNetwork neuralNetwork) {
        this.strategy = strategy;
        board.buildBoard();

        if(neuralNetwork != null) {
            this.neuralNetwork = neuralNetwork;
        } else if(strategy == Strategy.NEURAL_NET) {
            this.neuralNetwork = new NeuralNetwork(new int[]{72, 50, 36});
            neuralNetwork.initializeWeights();
        }
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
            case NEURAL_NET:
                output = computeOutputNeuralNet();
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
        if(turn < 12) {
            return  computeOutputNeuralNet();
        } else if(turn < 14) {
            return computeOutputLeastLoss();
        } else {
            return computeOutputMinMax();
        }
    }

    private String computeOutputCombinedTest() {
        if(turn < 7) {
            return  computeOutputHighFree();
        } else if(turn < 14) {
            return computeOutputLeastLoss();
        } else {
            return computeOutputMinMax();
        }
    }

    private String computeOutputNeuralNet() {
        neuralNetwork.setInputs(board.getAllCells(), oppColor);
        neuralNetwork.caluculateOutput();
        int node = neuralNetwork.getOutput(board.getAllCells());
        ArrayList<Coin> coins = board.getRemainingCoins(ourColor);
        return board.getAllCells().get(node).getName() + "=" + board.getHighestRemainingCoin(ourColor).getValue();
    }
}
