import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.Scanner;

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
        if(turn < 7) {
            return  computeOutputNeuralNet();
        } else if(turn < 12) {
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
        return board.getAllCells().get(node).getName() + "=" + board.getHighestRemainingCoin(ourColor).getValue();
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

    public Judge(Strategy stratRed, Strategy stratBlue, String[] brownCells, NeuralNetwork neuralNetwork) {
        gameRed = new GameHandler(stratRed, neuralNetwork);
        gameBlue = new GameHandler(stratBlue, neuralNetwork);

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

class NeuralNetwork {

    private Random random;

    public int score;

    int[] layerSizes;

    private int numLayers;

    private int numInput;
    private int numL1;
    private int numOutput;

    private double[] inputs;
    public double[][] w0;
    private double[] layer1;
    public double[][] w1;
    private double[] outputs;

    NeuralNetwork (int[] layerSizes) {
        this.layerSizes = layerSizes;
        numLayers = layerSizes.length;

        numInput = layerSizes[0];
        numL1 = layerSizes[1];
        numOutput = layerSizes[numLayers-1];

        inputs = new double[numInput];
        layer1 = new double[numL1];
        outputs = new double[numOutput];

        w0 = new double[numL1][numInput];
        w1 = new double[numOutput][numL1];

        random = new Random(123456789);
    }

    public void initializeWeights() {
        for(int x=0; x<numInput; x++) {
            for(int y=0; y<numL1; y++) {
                w0[y][x] = random.nextFloat() * 2 - 1;
            }
        }

        for(int x=0; x<numL1; x++) {
            for(int y=0; y<numOutput; y++) {
                w1[y][x] = random.nextFloat() * 2 - 1;
            }
        }

        w0 = new double[][]{{0.7787755727767944, -1.617670829499958, -0.5141955614089966, -0.4859122037887573, 0.8985010385513306, 0.18454432487487793, -0.7122752666473389, -0.726078987121582, -0.11724567413330078, -0.6389147043228149, -0.1540064184915373, -0.2393484115600586, -0.8024879693984985, 0.006883859634399414, -0.6317768096923828, -0.44973623752593994, -0.7314032316207886, -0.6110284328460693, 0.6267056465148926, -0.024631619453430176, -0.5955696105957031, 0.20659852027893066, -0.9361821413040161, 0.3259226083755493, 0.3874709049541736, -0.7691302299499512, 0.6388695240020752, -0.9844948053359985, -0.014120936393737793, 0.9830877780914307, 0.9842431545257568, 0.17438983917236328, -0.8953449726104736, 0.39448750019073486, -0.4808098077774048, 0.8328396509644647, -0.9699834585189819, -0.4126788377761841, 0.09893035888671875, 0.17224180698394775, -0.9136810302734375, 0.4270068407058716, -0.49058258533477783, 0.6384423237456058, 0.3843475580215454, 0.31733131408691406, -0.668973445892334, -0.44633464679935875, 0.057506680488586426, -0.15454721450805664, 0.7198083400726318, 0.104164719581604, -0.7497988939285278, -0.6692767143249512, -0.5071560144424438, 0.39735425242646594, -0.5314227342605591, -0.9088901281356812, 0.5546290874481201, -0.7256273031234741, -0.7895665168762207, -0.6867363452911377, 0.16430306434631348, -0.685457706451416, 0.9102492332458496, -0.20295143127441406, -0.18107032775878906, 0.4090158939361572, 1.5026117736073001, 0.3746863365040297, 0.6159305572509766, 0.20886719226837158}, {-1.3746265548192635, -0.06151280305573421, 0.6828045845031738, 0.39207005500793457, 0.13421154022216797, 0.47933638095855713, -0.016450881958007812, -0.1634901762008667, -0.14657950401306152, -0.21330976486206055, -0.990283727645874, 0.4424141569045812, 0.9962154626846313, 0.5535688400268555, -0.3826425075531006, -0.17105889320373535, 0.30410807156828135, -0.5306342840194702, -0.05334365367889404, 0.11718201637268066, 0.6773291826248169, 0.6211003065109253, -0.43756771087646484, -0.24335050582885742, 0.9847791194915771, -0.13927924633026123, -0.4495875835418701, 0.7894521359918698, 0.46799755096435547, 0.890365481376648, -0.6225643157958984, 0.5055303573608398, -0.31554198265075684, 0.2873234748840332, -0.15866541862487793, -0.9331077062221007, -0.7335379123687744, 0.313556423528144, 0.5163182020187378, 0.8046836853027344, -0.6617413541668027, -0.05024909973144531, 0.7422559261322021, 0.4557081460952759, 0.4508172273635864, -0.3864654302597046, -0.5886573791503906, -0.4989558458328247, 0.6192111968994141, 0.9255207777023315, 0.573199987411499, -0.9993972778320312, -0.03494226932525635, -0.8794237375259399, -0.7904478311538696, -0.6561017036437988, -0.945534348487854, -0.26926088333129883, 0.42218637466430664, 0.5610542297363281, 0.9264773993416807, -0.7207267588738606, -0.2670077085494995, 0.6621021032333374, 0.5875793695449829, 0.9482759237289429, -0.2077921142374709, 0.8148460163708369, -0.3346278667449951, -0.07949519157409668, -0.26699960231781006, -0.183188796043396}, {0.10047305790452588, -0.012432108386873654, 0.21601760387420654, -0.3583664894104004, -0.28365910053253174, -0.5967000722885132, -0.8067155510127915, 0.20780622959136963, -0.41328251361846924, 0.06865763664245605, -0.6725682020187378, -0.6269837617874146, -0.8085764646530151, 0.8644664091804892, -0.44841063022613525, -0.17886555194854736, 0.9814385175704956, -0.6886250290720493, 0.8450818061828613, 0.37923972400541217, 0.4993717670440674, 0.37989234924316406, -0.05002570152282715, -0.35596313188909146, -0.4370429515838623, 0.500597357749939, -0.6371632814407349, -0.3766211271286011, -0.9120403528213501, 0.14065897464752197, 0.9846296588521419, 0.9963233470916748, 0.028342843055725098, -0.711817741394043, 0.35741025659166686, 0.8025697469711304, -0.7661991119384766, 0.048850417137145996, -0.27904295921325684, -0.4578383474688661, -0.6950343582659517, 8.637905120849609E-4, 0.9023584127426147, 0.04109944653637787, -0.6895754337310791, 0.282201886177063, 0.39085912704467773, 0.4243903160095215, -0.43853986263275146, -0.5997371673583984, -0.9635905027389526, -0.1713731288909912, -0.9313198328018188, -0.28318023681640625, -0.324460178613865, 0.17293226718902588, 0.3694794178009033, -0.6633726358413696, -0.8674818277359009, -0.17834997177124023, 0.4685342311859131, -0.2840970754623413, 0.5237266515548252, -0.34093332290649414, 0.21530044078826904, 0.7072926759719849, 0.24937927722930908, 0.7498921155929565, 0.10132110118865967, -0.029168128967285156, 0.8477623462677002, -0.7888991832733154}, {0.8471026526209033, 0.010206744596026359, 0.22133374214172363, -0.6389410495758057, 0.6024926900863647, 0.17567706108093262, -0.3168298006057739, -0.11510038375854492, 0.33860599994659424, 0.4355766773223877, -0.2508805990219116, 0.6791681051254272, -0.22691663862825373, -0.44746696949005127, 0.11014378070831299, 0.21732878684997559, 0.4386996030807495, -0.6984773190923966, -0.3508082628250122, -0.9621856212615967, -0.839458703994751, 0.15897107124328613, -0.7395237684249878, -0.6470053195953369, 0.41603875160217285, -0.31476831436157227, -0.21305012702941895, 0.37090182304382324, -0.4897737503051758, 0.9761223793029785, -0.8405014995077305, -0.8728663921356201, -0.5856344699859619, 0.130418062210083, 0.6087685823440552, 0.5903551354907666, -0.8161273002624512, 0.37267887592315674, -0.8171732425689697, 0.7038196325302124, -0.636449933052063, 0.744671106338501, 0.36639654636383057, -0.9952061176300049, -0.49774348735809326, -0.6073691844940186, -0.4841853380203247, 0.46383023262023926, 0.2136009931564331, -0.41529202461242676, -0.48860251903533936, -0.6313647130623046, 0.596194863319397, -0.4519919157028198, -0.3588123321533203, -0.25678062438964844, -0.7335447072982788, -0.26777493953704834, 0.892448902130127, -0.5858947973379391, -0.2410959005355835, -0.8130605220794678, 0.9302654266357422, 0.617910623550415, 0.6133569478988647, 0.38223230838775635, 0.8883081674575806, 0.9995627403259277, -0.9046448469161987, -0.9926578998565674, -0.9949696063995361, -0.7791560531998856}, {-0.3488358526061861, -0.3008807897567749, 0.2890828847885132, -0.595999077682827, -0.2725323438644409, -0.22127974033355713, 0.920563817024231, 0.5183759927749634, -0.004625201225280762, 0.890779972076416, 0.0854111909866333, 0.5619841814041138, -0.5709983110427856, 0.15621242124286752, -0.8207806348800659, -0.22344030058610911, -0.38913118839263916, 0.9981825351715088, -0.5331499576568604, 0.8857662677764893, 0.6264935731887817, 0.7114015817642212, 0.4400622844696045, 0.4895426034927368, 0.9506669044494629, -0.056441664695739746, 0.6350283582632987, -0.21649831520895146, 0.37159526348114014, 0.8466769456863403, -0.9206459522247314, 0.2519195079803467, -0.5921945571899414, -0.7647702693939209, 0.9039342403411865, -0.5433284044265747, -0.48940420150756836, 0.4223407506942749, 0.7809736728668213, 0.9135428667068481, 0.9952113628387451, -1.8632244254485315, 0.8663218021392822, -0.8963738679885864, 0.10680949687957764, 0.2969193458557129, -0.3247338533401489, 0.7502707703231305, 0.5437328562792709, -0.113372802734375, 0.6871044392226167, -0.8107584714889526, -0.27998030185699463, 0.9260097742080688, 0.875963568687439, -0.23340296745300293, -0.10085296630859375, 0.14040601253509521, -0.39784061908721924, 0.39408981800079346, 0.14454030990600586, -0.6960304975509644, 0.641739010810852, -0.3202296495437622, -0.5680339336395264, 0.9529825448989868, -0.10859438284105544, -0.7237474918365479, 0.7074589729309082, -0.5332353115081787, 0.8889578580856323, 0.14170378110073825}, {0.18416154384613037, 0.046919941902160645, 0.8703879117965698, -0.46257615089416504, -0.8745038509368896, 0.21054887771606445, -0.5670522267957963, -0.6291235685348511, 0.4805663824081421, 0.6733357906341553, -0.6259254325703301, -0.4394643306732178, -0.1677640676498413, 0.28906759418048944, 0.21293067932128906, 0.8259869813919067, 0.6678262948989868, 0.6687885522842407, -0.42885053157806396, -0.05508065223693848, 0.9746772050857544, -0.9168208837509155, -0.37374722957611084, -0.9687665700912476, 0.013396263122558594, 0.586378812789917, -0.7890113592147827, -0.6942346096038818, -0.6964713335037231, 0.8696870803833008, -0.41550105442236035, -0.5045686960220337, 0.6239537000656128, -0.43599236011505127, -0.7255264520645142, -0.25145208835601807, 0.1603926420211792, -0.8767589330673218, 0.12198734283447266, -0.23146164417266846, -0.17500734329223633, 0.8364936113357544, -0.19084125948685354, 0.7451869249343872, 0.2280268669128418, -0.1264574818687172, 0.47630584239959717, 0.6020573377609253, 0.7993307113647461, -0.4182029962539673, 0.6198558735331767, 0.12738112472408814, -0.862826943397522, -0.289642333984375, 0.5241917371749878, -0.7537212371826172, -0.19516265392303467, 0.23416197299957275, 0.8182443380355835, -0.8722339868545532, 0.5002658367156982, -0.20519757270812988, 0.44543778896331787, -0.6078222990036011, 0.7917008399963379, 0.8969917297363281, 0.6191191673278809, -0.8911839842201474, -0.06440846016849067, 0.21259479198594655, -0.37748241424560547, 0.7657754421234131}, {-0.3160381317138672, 0.80420982837677, 0.8771666288375854, 0.2993593215942383, 0.7818393707275391, 1.2902422361389305, 0.23047125339508057, 0.32732927799224854, 0.41575539112091064, 0.4873000941062915, 0.5013843774795532, 0.2528723478317261, -0.8179551362991333, 0.35643064975738525, 0.1297924518585205, 0.819069504737854, -0.9487353563308716, -0.7966208457946777, -0.3829103708267212, -0.4428138732910156, 0.7199298577952713, 0.5127921104431152, -0.513404130935669, 0.7538230419158936, -0.46727088768774433, 0.6436964273452759, -1.5051001425667103, -0.11816239356994629, 0.046051621437072754, 0.8315811157226562, -0.4705702066421509, -0.6086630821228027, -0.38103675842285156, -1.5296631640907419, 0.6512371301651001, -0.2800670862197876, -0.025957465171813965, 0.40861940383911133, -0.2760910987854004, 0.156843900680542, -1.1009102420585721, -0.3128465414047241, 0.89533531665802, 0.78331458568573, -0.21794354915618896, 0.12216484546661377, 0.8059877157211304, -0.887782096862793, 0.2551347017288208, 0.02860414981842041, -0.32895374298095703, -0.737404465675354, 0.11172783374786377, 0.9805014133453369, -0.3247796297073364, -0.4697219133377075, -0.8036788702011108, 0.3201594352722168, -0.45322155952453613, -0.026133789982082715, -0.9331306219100952, 0.9889215922134096, -0.06293320655822754, -0.076194167137146, -0.7277064323425293, -0.9248102903366089, 0.46503753997673614, 0.551945686340332, 0.02991950511932373, 0.9290353059768677, 0.38331031799316406, -0.6191648244857788}, {-0.14171922206878662, -0.3112577999144315, -0.10997903347015381, 0.5321695804595947, -0.5331161022186279, 0.532300591468811, -0.43647003173828125, 0.9482518434524536, 0.9120712280273438, -0.383031964302063, 0.5336905717849731, 0.6985143423080444, 0.6984319686889648, 0.5495539386810566, 0.839211106300354, 0.2889885902404785, 0.1920785903930664, -0.03863537311553955, -0.671673059463501, -0.7404459273584645, 0.1621323823928833, 0.006796360015869141, 0.618262767791748, -0.2881098985671997, 0.8674063682556152, -0.44463921654749505, -0.8973902463912964, -0.32696378231048584, 0.3965574066023516, 0.6450760364532471, 0.9744505882263184, -0.8183802366256714, 0.32188093662261963, -0.2812952995300293, 0.6100221872329712, -0.004052639007568359, -0.2615751028060913, -0.02696704864501953, 0.09715497493743896, 0.49508559703826904, -0.2257983684539795, -0.5959243774414062, -0.5119872819883746, 0.846961259841919, -0.6762676239013672, 0.5160858414111491, -0.42652249336242676, -0.24643456935882568, -0.10374081134796143, -0.8828555345535278, 0.8166842460632324, 0.08924198150634766, -0.2685363292694092, -0.2252582311630249, 0.3462512493133545, 0.5156161785125732, 0.06243550777435303, -0.42470216751098633, -0.8052786588668823, 0.11309136612179098, 0.5943096876144409, 0.2615715265274048, -0.09078265676224467, 0.8347530189405845, -0.6373022794723511, -1.8721077254397005, 0.32386696338653564, -0.4895857572555542, 0.2942386865615845, 0.5701647996902466, -0.6269800662994385, 0.20825538166423074}, {-0.6802173852920532, -0.018381476402282715, -0.9084231853485107, 0.9761463403701782, 0.05941846114799645, -0.9269955158233643, -0.30042552947998047, 0.029739975929260254, -0.9187242984771729, 0.08069705963134766, -0.9422602653503418, -0.7227528095245361, -0.047933101654052734, -0.04339611530303955, -0.391934871673584, -0.002313801501557265, -0.6104680299758911, 0.10370779037475586, 0.5190280675888062, 0.0707390308380127, -0.5133689641952515, -0.39919888973236084, 0.07468271255493164, -0.5344240665435791, 0.3665449619293213, -0.2718663215637207, 0.992486834526062, -0.6338534355163574, -0.9730652570724487, -0.6227142810821533, 0.257571816444397, 0.11893439292907715, 0.02355170249938965, 0.6258459091186523, 0.1490548849105835, 0.4237058162689209, 0.1301955945296458, 0.7672373056411743, 0.034380555152893066, -0.9617081880569458, 0.7665196657180786, -0.5916969776153564, -0.7794276475906372, 0.872812032699585, 0.5118125677108765, -0.42674119334051297, -0.32333648204803467, 0.40529119968414307, -0.1104511022567749, -0.829981803894043, -0.22053170204162598, 0.7041254043579102, 0.015717790415332056, 0.7892067432403564, 0.3852449655532837, -0.0014913770065942949, 0.03275275230407715, 0.6693543195724487, 0.49269163608551025, -0.20060396194458008, 0.5974984169006348, -1.2415286566476347, -0.46189165115356445, 0.5742578506469727, 0.0811777114868164, 1.1383039596071653, 0.9514482021331787, -0.26940214210761343, -0.5787981748580933, 0.4374995231628418, -0.997628927230835, 0.3131699562072754}, {0.35010862350463867, 0.036426663398742676, 0.46143078804016113, -0.013365387916564941, 0.24404847621917725, -0.16165566444396973, 1.3178929321337167, -0.7965855598449707, -0.5899630784988403, 0.05819892883300781, -0.7751829624176025, -0.14632904529571533, 0.20326614379882812, -0.5091797113418579, -0.25135064125061035, -0.40462982654571533, 0.38854193687438965, 0.031241893768310547, -0.491935133934021, 0.7236700057983398, 0.8594184383341459, -0.5963470935821533, -0.3388272544636725, -0.6000442504882812, -0.41020238399505615, 0.9388740062713623, 0.058867812156677246, -0.8076279163360596, -0.957411527633667, 0.45022475719451904, 0.3371496569789423, -0.02912163734436035, 0.7426453828811646, 0.2595996856689453, -0.5880917310714722, -0.5169376134872437, -0.08471465110778809, -0.5099737644195557, 0.03968179225921631, -0.7173529863357544, 0.6993122100830078, 0.08083685752836453, -0.6623955965042114, -0.909170389175415, 0.7129026651382446, -0.5322216749191284, -0.6836053133010864, 0.14129745960235596, 0.25436675548553467, -0.9162434339523315, -0.6494491100311279, 0.5929619073867798, -0.9319808483123779, 0.7290316689328725, 0.7251524925231934, 0.6901628971099854, -0.47077274322509766, 0.1281142234802246, -0.1520366668701172, 0.008293390274047852, -0.4109722375869751, 0.5494850873947144, -0.20367133617401123, -0.1565004587173462, -0.13246595859527588, -0.2568943500518799, -0.0515592098236084, 0.5883134603500366, 0.896682858467102, 0.41753995418548584, -0.9088778495788574, 0.7807817459106445}, {-0.6045079231262207, 0.5449540615081787, 0.7627819776535034, 0.969444751739502, -0.6122680783326024, -0.8664706945419312, 0.41060948371887207, 0.6640673875808716, 0.4786999225616455, 0.6149470806121826, 0.8241088390350342, -0.4196528196334839, 0.2012108564376831, -0.5716358423233032, 0.7633905410766602, 0.09784889221191406, -0.4808121919631958, -0.31320512294769287, -0.025987625122070312, 0.77837073802948, -0.29633665084838867, 0.9698324203491211, 0.9903280735015869, 0.41547536849975586, 0.2779958248138428, -1.3321541254765044, -0.8237223717987414, 0.35238659381866455, -0.15413296222686768, -0.9242227077484131, 0.14716660976409912, 0.3522481918334961, 0.7863564491271973, -0.02848207950592041, -0.6675320863723755, -0.8261170387268066, -0.10841083526611328, -0.09060075552686074, -0.7952486276626587, -0.31217122077941895, 0.4697272777557373, 0.6623409986495972, 0.5232933759689331, -0.1384294033050537, 0.19583308696746826, 0.5554664134979248, -0.6690102815628052, -0.7504715919494629, -0.9345964193344116, -0.39277172088623047, 0.3096812963485718, -0.25750184059143066, -0.809861421585083, 0.9868043661117554, 0.6363136768341064, -0.7371106147766113, 0.872771143913269, 0.3895021677017212, 0.7425791025161743, -0.16376197338104248, -0.5924454927444458, 0.13911521434783936, -0.4342489909981835, -0.9738831520080566, 0.3752650022506714, -0.6198835488744483, -0.5454274415969849, 0.088706374168396, -0.16365265846252441, -0.9207972288131714, -0.1627082910816446, -0.6150728464126587}, {0.44900333881378174, -0.11249113082885742, -0.09848546981811523, -0.27799856662750244, 0.0855100154876709, 0.04380476474761963, 0.14468741416931152, -0.7972971200942993, -0.9606623649597168, 0.48385796318091334, 0.3004652261734009, -0.20913457870483398, -0.5011705160140991, 0.4312760829925537, -0.6918240785598755, -0.8417649269104004, 0.4403733015060425, 0.6256643533706665, 0.8562756776809692, -0.17655584073010355, -0.08189904689788818, -0.9565163850784302, 0.7364048957824707, -0.8783208131790161, 0.34803736209869385, -0.26159173265429514, 0.7927517890930176, 0.6700043678283691, 0.168593168258667, 0.291414737701416, -0.3457145690917969, 0.4915221929550171, -0.1137474775314331, 0.03800344467163086, -0.6543772220611572, -0.8564142535136632, 0.20475876331329346, -0.2490067481994629, -0.15700459480285645, 0.2896914482116699, 0.6880548000335693, -0.7708518505096436, 0.6749886274337769, 0.27771270275115967, -0.0020858916763661917, -0.36755846613061943, -0.23657572269439697, -0.595454852305201, -0.5856668949127197, -0.06259254686244144, 0.8579661846160889, 0.05268871784210205, -0.979419469833374, 0.7941670417785645, 0.1387253999710083, -0.8266732692718506, -0.5319961309432983, 0.8371379375457764, -0.15564978122711182, 0.469157212211033, 0.6360838413238525, -0.9406590461730957, 0.12878680229187012, 0.5836704969406128, 0.16143202781677246, 0.2029249668121338, -0.24190235137939453, 0.6509162900879065, 0.7332508563995361, 0.9257208108901978, -0.24299477809919484, -0.9594911087184093}, {-0.41755545139312744, -0.18115830421447754, 0.12754391816589283, 0.8583530187606812, 0.6954586505889893, -0.661400556564331, -0.47551286220550537, -0.2911389317770068, 0.6665985584259033, -0.8156124987635698, 0.9369416236877441, -0.6534101963043213, -0.24709011862858388, -0.6142494678497314, 0.09596610069274902, 0.1169658899307251, 0.10445511341094971, 0.98402710591348, 0.14518952369689941, -0.17857885360717773, 0.5817080736160278, -0.319873571395874, 0.9903225898742676, 0.8248523473739624, 0.5513424873352051, 0.9204273223876953, -0.759650707244873, -0.3213430643081665, -0.12350833415985107, -0.9384828805923462, 0.3281562328338623, 0.3351053046560641, -0.3374446630477905, -0.1673755645751953, 0.8426423072814941, 0.9251565933227539, -0.5335345268249512, 0.9475500583648682, -0.08900535106658936, -0.14143943786621094, -0.35876786708831787, -0.3592662811279297, -0.00347026908259243, -0.6459293365478516, -0.23715054988861084, 0.12065708637237549, 0.6485341787338257, 0.6245832443237305, 0.6745365858078003, 0.9213593006134033, -0.6031945943832397, 0.10052929672619001, 0.08367657661437988, 0.048853158950805664, -0.7840176820755005, -0.8111894130706787, -0.07427941478311272, 0.047746896743774414, -0.6798428772163142, -0.4248267412185669, -0.793386697769165, -0.34560680389404297, -0.5819339752197266, 0.2907083034515381, -0.27845799922943115, -0.17013506779956303, -0.32803094387054443, -0.5604124069213867, 0.0014913082122802734, 0.9443658590316772, 0.23626720905303955, 0.22408711910247803}, {-0.6027145385742188, -0.828237771987915, -0.5424107313156128, 0.9499866962432861, -0.7002435922622681, -0.2527362108230591, -0.9480627775192261, -1.4302773107032734, -0.5754315853118896, -0.47715146057599583, 0.356462010343396, 0.8179279565811157, 0.24465882778167725, -0.5180761814117432, 0.9495858443611218, -0.6951911094276988, 0.006884455680847168, -0.1042764326325718, 0.44756853580474854, -1.0591113900006874, -0.840965747833252, -0.9667491912841797, 0.280603289604187, 0.06479442119598389, 0.9799846410751343, 0.1361759901046753, -0.2789795398712158, 0.7852296829223633, -0.8315709829330444, 0.08386409282684326, -0.6724766492843628, 0.5498781204223633, -0.003914780483539576, -0.058551788330078125, -0.11622512340545654, -0.0714028130962665, 0.8607137203216553, -0.8325851046658532, 0.42649269104003906, -0.21112549304962158, -0.3996177911758423, -0.29991471767425537, 0.5887788534164429, 0.019473910331726074, -0.9427530848948351, 0.48405373096466064, -0.0708470344543457, 0.1941758394241333, -0.12370848655700684, -0.9773801565170288, -0.47191059589385986, -0.20894479751586914, -0.938346266746521, 0.9620978832244873, 0.3076857328414917, -0.9505963751713948, 0.5159413814544678, 0.5627732276916504, -0.7316724611832017, -0.9093830585479736, -0.015964627265930176, -0.08836126327514648, -0.8802357912063599, -0.6472941637039185, 0.10001528263092041, -0.1003713607788086, -0.3011831045150757, 0.13955931854298803, -0.8349144458770752, 0.6899739503860474, -0.7083176599906879, -0.3890575170516968}, {0.10731065273284912, 0.7341585159301758, 0.5878349542617798, 0.34892189502716064, 0.5446804761886597, -0.19438910484313965, 0.7856605052947998, -0.7746646404266357, -0.5269879102706909, 0.1981428861618042, -0.6202682256698608, 0.5455214977264404, -0.09486758708953857, 0.9226057529449463, -0.9826483726501465, 0.604209303855896, 0.7919243574142456, 0.5019153356552124, -0.45142316818237305, -0.6454297304153442, 0.6378767490386963, -0.22955517951678273, 0.9862953424453735, -0.9262253046035767, -0.3655799060126298, -0.3881577253341675, -0.3434414863586426, -0.25151896476745605, -0.5549181699752808, -0.9817193746566772, 0.550217866897583, -0.19492173194885254, -0.5377280712127686, 0.06342196464538574, -0.7283228635787964, 0.08488631248474121, -0.26798510551452637, 0.45035600662231445, 0.4541822302557899, 0.05241274833679199, -0.6354069709777832, -0.8699592351913452, 0.3101005554199219, 0.12515723705291748, -0.7494240999221802, -0.502647876739502, -0.9555280208587646, 0.7263389825820923, 0.8374795913696289, -0.946136474609375, -0.9802448749542236, -0.8126788139343262, 0.8469570875167847, 0.48150861263275146, -0.1491379737854004, -0.8573783093919609, -1.2339335675843204, 0.06542026996612549, 0.39421510696411133, -0.2150583267211914, -0.2725088596343994, -0.7891203165054321, 0.9043917655944824, -0.6989823579788208, -0.03148353099822998, -0.5815485715866089, -0.6940486431121826, 0.6035223007202148, -0.8432496786117554, 0.5130187273025513, -0.49835121631622314, -0.46165549755096436}, {0.1642158031463623, -0.849605917930603, 0.5291135311126709, -0.5682958364486694, -0.25165343284606934, -0.3491036891937256, 0.34313297271728516, 0.17304551601409912, -0.6863007545471191, -0.1511380672454834, 1.0215423351306598, 0.01992940902709961, -0.3890615701675415, -0.05023372173309326, -0.6530986568386633, -0.7824338674545288, -0.019989644807560047, -0.6060899278149676, -0.5307822227478027, 0.7503998445965165, -0.4796419143676758, -0.6116718053817749, -0.7445583343505859, -0.5066020488739014, 0.21813833713531494, 0.5604274272918701, 0.7506310939788818, 0.2355290651321411, -0.10356998443603516, -0.051913823658865654, -0.8005943298339844, 0.530271053314209, -0.26610636711120605, 0.7090656908554805, -0.09395051002502441, -0.9165300130844116, -0.3065923978369609, -0.6897631883621216, 0.48476922512054443, 0.1786259412765503, 0.8427682227482025, 0.5111705102439392, -0.793487548828125, 0.37936085235583583, -0.4520763158798218, -0.05790293216705322, 0.2480703592300415, -0.8459359407424927, 0.2692216724667038, 0.0038902759552001953, -0.16135704517364502, -0.7999016046524048, -0.6029120511489909, -0.26104618028970794, -0.44900083541870117, -0.864867484826277, -0.28223989918368564, 0.8054128885269165, 0.29881489276885986, 0.8022862672805786, -0.9146578311920166, 0.0656389424592316, 0.9787273406982422, -0.5957332849502563, -0.47467613220214844, 0.27536702156066895, -0.8649026155471802, 0.5962077544083333, 0.7692840099334717, 0.803636908531189, -0.47257840633392334, -0.15633416175842285}, {-0.3456498384475708, -0.3941455176037054, 0.8215051889419556, -0.5117447376251221, 0.861092250842064, 0.46609604358673096, 0.40792620182037354, 0.3687105178833008, -0.05200779438018799, -0.4154324531555176, 0.9057748317718506, 0.6451071500778198, 0.44855237007141113, 0.722681999206543, 0.17340409755706787, 0.5677496073378149, 0.805304765701294, 0.052857279777526855, -0.907113790512085, -0.23377513885498047, -0.49269306659698486, -0.8384184837341309, 0.049231529235839844, 0.8247896432876587, -0.27426302433013916, 0.13520455360412598, -0.557835578918457, -0.5053898096084595, -0.3110743761062622, 0.4789912700653076, 0.9918992836274327, 0.15974056720733643, 0.7730188369750977, -0.7497060166165392, -0.5610113143920898, 0.52196204662323, -0.09523177146911621, 1.9678158432342419, 0.8379926513730163, -0.5179893970489502, 0.7420876026153564, 0.9269341230392456, -0.6271917819976807, 0.9403400421142578, 0.7796521186828613, 0.6273161172866821, -0.3477928638458252, 0.1935044527053833, -0.9790410995483398, 0.6255215406417847, 0.6073695421218872, -0.2317335605621338, -0.8350913524627686, 0.06066298484802246, -0.014212250709533691, 0.014043331146240234, 0.3741072416305542, 0.43734824657440186, 0.594718337059021, 0.5653296709060669, -0.3762713670730591, -0.8289692401885986, -0.25743121595725116, -0.11075818538665771, 0.3739182949066162, -0.8030930757522583, 0.9845384359359741, -0.4506497383117676, 0.8626507640655912, 0.22372078895568848, 0.48353254795074463, -0.4545201063156128}, {0.27403175830841064, 0.3330378532409668, 0.11255651625196239, 0.685136435664957, -0.7850675582885742, -0.7462595701217651, -0.16318273544311523, 0.04816269874572754, -0.9247722066815611, 0.3006932735443115, -0.21134495735168457, -0.8574809901740245, -0.2718862295150757, 0.2493664026260376, -0.3718186616897583, 0.15129876136779785, -0.9131805896759033, 0.09903427201459929, -0.39032459259033203, -0.4056488275527954, -0.522436261177063, 0.8743630647659302, -0.9481096267700195, 0.7217525243759155, -0.6583720445632935, 0.1688612699508667, -0.28539836406707764, -0.6701127290725708, 0.8767589330673218, 0.6373140811920166, 0.4646003246307373, 0.11008155345916748, -0.37187862396240234, -0.44557785987854004, 0.1857854127883911, -0.20175402847663593, -0.29820406436920166, 0.5073003768920898, -0.9069205522537231, -0.9523065090179443, 0.074027419090271, -0.6827945709228516, -0.6225210428237915, -0.8183979988098145, -0.8513293828682615, -0.48740482330322266, -0.13648707351891587, -0.39187824726104736, -0.46669328212738037, -0.819269061088562, -0.07493024620011252, 0.5351721048355103, 0.07988393306732178, -0.09801733493804932, 0.7237130403518677, -0.6264997720718384, -0.08103716373443604, 0.4104565382003784, 0.883823037147522, -0.8593816757202148, -0.6056510210037231, 0.6265095472335815, 0.4302685260772705, 0.020889864346870013, -0.3868744373321533, -0.22882282733917236, -0.37633133099519317, -0.4754143953323364, 0.6477096109121878, -0.6005147695541382, -0.4652547836303711, -0.6115995645523071}, {-0.40501630306243896, 0.806147575378418, -0.8030111789703369, 0.9983267784118652, -0.8058754205703735, -0.6270185708999634, 0.7227886915206909, -0.6713278293609619, 0.33257293701171875, 0.2150794267654419, -0.34869371914892183, 0.8679262399673462, -0.8652869452089437, 0.8396363258361816, 0.4505718946456909, 0.20065152645111084, 0.6794182062149048, 0.18243122100830078, -0.6443222761154175, 0.4372810125350952, -0.30599725246429443, -0.016037702560424805, -0.2674429416656494, 0.3079270111078527, 0.28307271003723145, 0.29982292652130127, -0.8492594957351685, 0.9300252199172974, 0.34330081939697266, 0.6529476642608643, 0.3006289005279541, 0.4677877426147461, 0.03845226764678955, -0.1326699627602192, -0.2407098964675698, -0.7148188352584839, 0.11198508739471436, 0.9080557823181152, 0.3624234199523926, -0.3143404722213745, 0.7998777581357488, 0.3871530294418335, 0.2032557725906372, 0.3406254053115845, -0.15129530429840088, 0.9891171455383301, 0.6350563612840598, 0.5266801118850708, -0.009649395942687988, 0.30602729320526123, 0.11605632305145264, -0.10490548610687256, -0.5011858940124512, -0.11093697702466754, 0.14510655403137207, -0.780393123626709, -0.13167786598205566, 0.21945368718160904, -0.5617825984954834, -0.3747060298919678, 0.5510385036468506, -0.5416891574859619, -0.24214780241073325, 0.6704028844833374, -0.21098101139068604, 0.9766677618026733, 0.11481940746307373, -0.6653286218643188, 0.17615461349487305, 0.18862152099609375, 0.6512544156532327, 0.4618043899536133}, {-0.6404772996902466, -0.3795124292373657, 0.005858421325683594, -0.9933767538113805, -0.8508671522140503, -0.8605233012928557, -0.19668543338775635, 0.2708547115325928, -0.311611533164978, 0.35106992721557617, -0.869171142578125, 0.03184521198272705, 0.7624056339263916, -1.0512154152459006, -0.29015612602233887, -0.08700859546661377, -0.0729137659072876, 0.703252911567688, 0.012716889381408691, 0.18518400192260742, 0.24817657470703125, 0.7049870491027832, -0.5603018999099731, 0.9133381843566895, -0.5183879137039185, -0.22926604747772217, 0.44067443690014807, 0.6695399284362793, -0.9678770303726196, -0.5615184307098389, -0.46216797828674316, -0.8802241086959839, -0.7834702730178833, -0.5053806304931641, 0.5470291376113892, 0.39690446853637695, 0.6101087331771851, -0.5533429384231567, -0.797080186859229, 0.5338548421859741, -0.4311361312866211, 0.30131959915161133, 0.23863697052001953, -0.8451296091079712, -0.602509617805481, 0.6436842679977417, -0.8674057722091675, -0.8910626173019409, -0.9277275800704956, 0.5306055545806885, 0.8357853889465332, -0.22162818908691406, -0.21164605962197403, -0.20825016498565674, -0.2734414339065552, 0.406862735748291, 0.8274811690685495, -0.025860071182250977, 0.6401188373565674, 0.9651002883911133, -0.32522690296173096, -0.628808856010437, -0.026372551918029785, -0.19155681133270264, 0.7295881509780884, 0.3447927236557007, -0.821540996474747, -0.32008957862854004, -0.792934775352478, -0.5209274291992188, 0.9622650146484375, 0.4731069803237915}, {-0.6826256513595581, -0.19841539859771729, 0.2630643844604492, 0.6359137296676636, -0.5668917894363403, -0.4752897024154663, -0.3905220031738281, 0.0537863828744795, -0.4001779556274414, 0.2889796495437622, 0.5650681257247925, 0.21812951564788818, 0.644356369972229, 0.2631082534790039, -0.8739638328552246, -0.33789587020874023, -0.20557725429534912, 0.32079648971557617, -0.1086893081665039, -0.8714064359664917, -0.6220364725644925, 0.31468772888183594, 0.8056045012511147, -0.9717811346054077, 0.31821867249483804, -0.8971303701400757, -0.26679110527038574, 0.5070484932164594, 0.10241198539733887, 0.16025376319885254, 0.9018863439559937, 0.1733022928237915, 0.12700701226019007, -0.22681283950805664, 0.585014820098877, -0.08906888961791992, -0.7982903718948364, 0.2941610813140869, -0.5184099674224854, 0.35629403591156006, 0.9225528240203857, 0.7245030403137207, 0.2074817419052124, -0.4243961572647095, 0.16897964477539062, 0.9964487552642822, 0.04440629482269287, 0.07922923564910889, -0.6082404851913452, -1.2701491144700907, -0.34711694717407227, -0.6631180047988892, -0.6423360109329224, 0.6018422522595872, -0.17581355571746826, -0.46627962589263916, 1.3401555014543929, 0.8701419830322266, 0.5279461145401001, -0.6129918098449707, 0.4034900909051265, 0.6687761545181274, -0.4938100576400757, -0.8972630500793457, -0.9197413921356201, 0.42255961894989014, -0.5287371873855591, -0.8761157989501953, -0.5029679536819458, -0.004770517349243164, -0.41580426692962646, 0.20674479007720947}, {-0.08857964658394235, 0.8004953861236572, 0.8908281326293945, -0.5281319618225098, -0.04883754253387451, 0.24887442588806152, -0.1363513469696045, 0.9324772357940674, 0.04950714111328125, -0.8232959508895874, 0.8995896577835083, 0.277011513710022, 0.2634488344192505, -0.27619731426239014, 0.2449718713760376, 0.7367242574691772, -1.0531093593692191, -0.8348246812820435, 0.03321374161997247, 0.48910415172576904, 0.6930495500564575, 0.558592677116394, 0.3045370578765869, -0.17085587978363037, -0.4650862216949463, -0.37854886054992676, -0.1306450366973877, -0.9195799827575684, 0.79777991771698, -0.4696664810180664, -0.10267484188079834, -0.39556241035461426, 0.7595443725585938, -0.5676062107086182, -0.3968623876571655, 0.14366090297698975, -0.6296371221542358, -0.7686567395040625, -0.6657732725143433, -0.32691311836242676, -0.5195587873458862, 0.5079172849655151, 0.9405301809310913, -0.06281375885009766, -0.16079556941986084, 0.9505068063735962, -0.758828186096852, -0.08651864528656006, -0.007627010345458984, 0.7266810518399273, -0.5693707466125488, 0.5709879398345947, -0.09145355224609375, -0.6803078651428223, 0.7680380344390869, -0.5188034772872925, -0.5655195713043213, 0.24675655364990234, -0.8374093770980835, -0.011226335518682351, 0.5083134174346924, -0.9326982498168945, 0.7912172079086304, -0.21676015853881836, 0.41529899650792346, -0.020515799522399902, 0.2790430784225464, -0.3113209437239366, -0.3760364055633545, -0.19747424125671387, 0.7242056131362915, 0.9250732660293579}, {-0.10167884826660156, 0.9826815128326416, 0.6856776475906372, -0.2733460038692222, -0.3982137441635132, 0.8696808815002441, -0.32838165760040283, 0.034552574157714844, -0.6593126058578491, -0.8849831810479076, -0.5902162790298462, 0.7752864360809326, -0.2113792896270752, -0.4304697513580322, 0.9495762586593628, -0.6070003509521484, 0.02819835971363438, -0.8845827579498291, -0.031758016456095155, -0.49644362926483154, 0.6820967197418213, 0.5139337778091431, 0.22144174575805664, 0.7667217254638672, 0.30146849155426025, -0.531084418296814, -0.23886752128601074, -0.331579327583313, 0.20932594872492483, -0.1912379510619475, -0.1266704797744751, -0.4359316825866699, -0.14769864082336426, 0.5386446714401245, -0.9466778039932251, 0.09710865727734719, 0.8782427459186337, 0.4380829334259033, -0.3619018843470773, 0.6239899396896362, 0.19986653327941895, -0.11288011074066162, 0.19222843647003174, -0.41876673698425293, -0.5768624544143677, -0.07500076293945312, -0.3387678861618042, 0.5725699663162231, 0.7161293029785156, 0.865662693977356, -0.9911001920700073, 0.020853281021118164, 0.8162996768951416, 0.46913933753967285, -0.3226051330566406, -0.30323243141174316, -0.41483139991760254, -0.36277830600738525, 0.39115655422210693, -0.00921177864074707, 0.32068074150332415, -0.9868732690811157, 0.04038722576091735, -0.8130651712417603, 0.28692030906677246, -0.2035156488418579, 0.5846250057220459, -0.5548310279846191, 0.06499731540679932, 0.8291324377059937, 0.6321462392807007, 0.09967172145843506}, {-0.3826850652694702, -0.5970057249069214, -0.38878440856933594, 0.5391178131103516, -0.5834156274795532, -0.5568777322769165, -0.20283198356628418, -0.8389073610305786, -0.9792990684509277, 0.8174958229064941, 0.3324151039123535, 0.5244263410568237, -0.04066169261932373, -0.389005184173584, -0.1981675624847412, -0.18321442604064941, -0.10111474990844727, -0.3698751926422119, 0.8391278982162476, -0.9213203191757202, 0.532353401184082, 0.5767253637313843, 0.4396700859069824, -0.9883841276168823, -0.4512145519256592, -0.7458637952804565, -0.5800131559371948, 0.3773345947265625, -0.3548851013183594, 0.7712136507034302, -0.5355759859085083, 0.28262269496917725, 0.264133095741272, -0.24158215522766113, 0.7014081478118896, 0.7242746353149414, 0.34178174498723907, 0.14428534796057807, 0.31620943546295166, 0.41135406494140625, 0.6618008613586426, 0.8323206901550293, -0.6659433841705322, 0.08698368072509766, -0.6642162891483621, -0.8079622983932495, 0.39690566062927246, 0.3394724130630493, 0.029138430440618264, -0.09802162647247314, 0.10911893844604492, -0.7773382663726807, 0.41508936882019043, -0.6748198067988285, 1.0920264881315591, 0.8979589939117432, -0.6945641853616962, 0.8434289693832397, -0.23780035972595215, -0.034404754638671875, -0.8442021608352661, -0.4632150705912699, 0.7950483560562134, -0.4087789821163341, 0.7973926067352295, -0.7621495723724365, 0.22265207767486572, -0.7039541725308495, -0.979642391204834, -0.31850719451904297, -0.017932331547392932, -0.19052863121032715}, {0.5443657020323028, 0.8259247541427612, -0.18451988697052002, -0.7290428876876831, 0.05613033975694326, 0.182564377784729, -0.09536087512969971, -0.697639837733639, 0.6741976737976074, -0.5381896495819092, -0.5720360279083252, -0.838847279548645, -0.5888970048216129, -0.34587883949279785, 0.3663572072982788, 0.6727026700973511, 0.9061614274978638, -0.33075928688049316, -0.6363515853881836, 0.5648635625839233, -0.9865564107894897, -0.9763104915618896, 0.8729071617126465, 0.11065685749053955, 0.20786774158477783, -0.34502530097961426, 0.683273434638977, -0.04932141304016113, -0.7039556503295898, 0.43411386013031006, -0.06545567512512207, 0.44512641429901123, -0.48668909072875977, -0.7296701669692993, -0.5603519678115845, -0.5446881055831909, 0.14326775074005127, 0.3365447521209717, -0.3394770622253418, 0.2678513526916504, 0.08628356456756592, -0.10843682289123535, 0.5922424689597777, 0.7282469272613525, 0.034012675285339355, -0.565399169921875, 0.3891878128051758, 0.03800792999156921, 0.7136087417602539, 0.8702775201810699, -0.6878071068215441, 0.8766007423400879, 0.284460186958313, -0.7146081924438477, 0.09308862686157227, 3.4606456756591797E-4, 0.14724128349099508, 0.5015426874160767, 0.3036067485809326, -0.32572948932647705, -0.29342377185821533, 0.3464639186859131, 0.9106826782226562, 0.7084693908691406, -0.44550812244415283, 0.3229098320007324, 0.16974186897277832, -0.30057525634765625, -0.3275027275085449, 0.7577308416366577, -0.34381425380706787, -0.18244624137878418}, {0.5858554216016005, -0.0067147016525268555, 0.519522583496437, 0.11446404457092285, 0.6399438381195068, 0.38758742809295654, -0.8673336510499894, -0.12481260299682617, -0.7715880870819092, 0.7950031757354736, -0.1568673849105835, 0.048798203468322754, 0.31163036823272705, 0.614726185798645, 0.924017071723938, 0.8740444183349609, 0.8510814905166626, 0.5464953184127808, 0.9231654405593872, -0.47861649424706854, -0.6674442291259766, 0.8987431526184082, -0.21466028690338135, 0.4487422704696655, 0.6137257814407349, -0.604413628578186, 0.4817277193069458, -0.9941061203254564, 0.905921459197998, -0.7448185682296753, 0.1570804324245283, -0.4779934883117676, -0.16988861560821533, -0.5610824823379517, 0.10919511318206787, 0.37456393241882324, -0.27657997608184814, -0.8396203517913818, -0.5511138439178467, -0.6794219017028809, 0.1713395118713379, -0.005041837692260742, 0.20477068424224854, 0.25274336338043213, 0.10212326049804688, -0.7545565366744995, -0.07785987854003906, -0.5360084772109985, 0.7036749124526978, -0.40410947799682617, -0.1536247730255127, -0.7512626647949219, -0.0477120177035476, 0.575474342464939, 0.25588417053222656, 0.2937082052230835, -0.750261664390564, 0.4561784267425537, 0.17335522174835205, -0.14353597164154053, 0.4724271297454834, -0.8279834985733032, 0.7537213563919067, 0.5754197835922241, 0.5183757543563843, -0.32436704635620117, -0.8622556286479583, 0.6633843183517456, -0.8361481428146362, -0.6639113426208496, -0.9556987285614014, 0.20733249187469482}, {-0.3466470241546631, 0.8906959295272827, -0.8652619123458862, 0.6521635055541992, -0.23353219032287598, -0.6511703729629517, 0.3184430365993908, -0.5667033167004891, 0.04609525203704834, -0.5332762002944946, -0.22005295753479004, 0.3894917964935303, -0.8690471649169922, -0.8981465101242065, -0.8851645554705032, -0.32150413856224885, -0.8155845403671265, 0.314835786819458, -0.3062070813095545, 0.375980019569397, -0.97447669506073, -0.5901502370834351, -0.3732431712179147, -0.6677819490432739, -0.09852206707000732, -0.32008397579193115, 0.20481526851654053, -0.755389928817749, -0.7416216135025024, -0.84432053565979, -0.21102404594421387, -0.020238280296325684, 0.31421327590942383, -0.5057680606842041, -0.3308548927307129, -0.8814781904220581, -0.8414461561957975, -0.20510053634643555, 0.6679954528808594, 0.0978032172995587, 0.8903456926345825, 0.5918141603469849, -0.33562195399360495, -0.4112701416015625, -0.8114354610443115, -0.5122884845028324, -0.5276608467102051, -0.0935293958456509, 0.41055989265441895, -0.5326071977615356, -0.31278228759765625, 0.7774105072021484, -0.43081462383270264, 0.1283254623413086, 0.734121760003424, -0.9899307489395142, 0.542754798657294, 0.1814512014389038, -0.20529329776763916, -0.2324708947625391, -0.41782093048095703, -0.1527174711227417, 0.22114765644073486, 0.19900619983673096, -0.17772603034973145, -0.48703908920288086, 0.333882212638855, 0.745932936668396, 0.9632893800735474, -0.5350081920623779, 0.0529094934463501, -0.7087017297744751}, {0.6644411087036133, -0.4944208860397339, -0.5074448585510254, 0.05201137065887451, 0.93467116355896, -0.09031355381011963, -0.2935042381286621, -0.9627183675765991, 0.8232020139694214, -0.93101966381073, 0.49456894397735596, 0.21601343154907227, 0.6313920021057129, -0.28424322605133057, 1.1273544799826762, 0.4765216879676064, -0.6238651275634766, -0.8279653787612915, 0.8722356557846069, 0.6698987483978271, 0.650212287902832, 0.9401590824127197, 0.24559783935546875, -0.804054856300354, 0.7789738178253174, 0.7079710960388184, 0.23977649211883545, -0.21657073497772217, 0.6278979778289795, -0.4603966474533081, 0.7792208194732666, -0.36622118949890137, 0.1675405502319336, 0.35303616523742676, 0.4691934369137639, 0.4733232259750366, -0.28317081928253174, 0.6781265735626221, -0.7901701927185059, -0.27430784702301025, -0.33738231658935547, -0.8560965061187744, 0.5399258136749268, -0.7813153266906738, -0.6071356534957886, -0.8385027647018433, -0.48476314544677734, 0.36170709133148193, 0.18275535106658936, 0.04999359625983755, 0.060765981674194336, -0.15879809856414795, -0.16245687007904053, -0.6116565465927124, 0.601032018661499, -0.44467806816101074, -0.9771745204925537, 0.8629385947674113, 0.0337545900198093, 0.01277532255900038, 0.4024447202682495, -0.3616831302642822, -0.529706597328186, -0.9191721677780151, 0.6550153726161181, -0.41354429721832275, 0.8800561428070068, -1.103585959779634, -0.8364890813827515, 0.017605900764465332, 0.06334102153778076, 0.051175832748413086}, {0.1350950090597534, -0.09585678577423096, 0.5293674468994141, -0.4426891803741455, 0.7020329236984253, -0.6671720743179321, 0.510124683380127, 0.03611302375793457, -0.544568657875061, 0.8268264532089233, 0.35450172424316406, -0.804180383682251, -0.11844478276862953, 0.17332899570465088, 0.6291998624801636, 0.7777488435565423, 0.5108140707015991, -0.06498706340789795, -0.7285230159759521, 0.02428996369868408, 0.49151456356048584, 0.045284152030944824, 0.8816819190979004, -0.2546877861022949, -0.39348816871643066, 0.8521674871444702, 0.181371808052063, 0.17735648155212402, -0.059458136558532715, 0.49982237815856934, -0.49305403232574463, 0.224573016166687, -0.7781376838684082, 0.5151772499084473, 0.8278719186782837, 0.005251646041870117, -0.7903620004653931, -0.24181056022644043, 0.6776475850593771, 0.8253237009048462, -0.3628612756729126, -0.5022432804107666, 0.8903435468673706, -0.49028265476226807, 0.17736029624938965, -0.708682656288147, 0.18026375770568848, 0.5519598722457886, 0.11057615280151367, 0.5746966600418091, -0.7047101259231567, -0.40497303009033203, 0.3987114429473877, 0.40976858139038086, -0.123740553855896, -0.7962936162948608, -0.34633623153374216, -0.308979868888855, 0.47275376319885254, 0.648505449295044, 0.5823980569839478, 0.1625724461515996, 0.5603675842285156, -0.5065979957580566, 0.395920991897583, 1.087188720703125E-4, 0.7842302322387695, 0.16450083255767822, 0.061864256858825684, -0.7321537733078003, 0.0526198148727417, -0.6260758638381958}, {-0.8039584159851074, 0.9867172241210938, -0.04672205448150635, -0.9774398803710938, -0.23990869522094727, 0.865547776222229, 0.6518187522888184, -0.2524780035018921, -0.47605228424072266, -0.003705143928527832, 0.03475630283355713, -0.7798027992248535, -0.2687782049179077, -0.5864347219467163, -0.4498990774154663, -0.5867038109687849, 0.9137778282165527, 0.1322809508884213, -0.12959516048431396, 0.580308198928833, -0.8936525583267212, -0.08295547962188721, 0.2035682201385498, 0.2942479914304195, -0.22582542896270752, -0.44136321544647217, 0.6589921712875366, -0.9402182102203369, -0.6619727611541748, 0.5966057777404785, -0.7893587350845337, 0.8241792917251587, -0.8640046119689941, 0.9416877031326294, -0.7150899171829224, 0.9428983926773071, -0.8209326267242432, 0.8872432708740234, 0.06642372485458536, 0.7434310913085938, -0.8624411821365356, -0.875278115272522, -0.905045747756958, 0.9583971500396729, -0.3619556427001953, -0.8740198612213135, 0.18882405960282167, -0.13485024976759308, 0.15552127361297607, -0.017516030369856272, 0.9164386987686157, 0.0371945537609395, 0.5531131029129028, 0.3080035448074341, 0.2876548869150326, 0.9290283918380737, -0.34830403327941895, -0.565956711769104, -0.48804497718811035, -0.9721664190292358, 0.10441458225250244, 0.044095516204833984, 0.3875258635109458, -0.7614991664886475, 0.30612635612487793, -0.975421667098999, -0.7003539742448556, -0.060877959249278746, -0.9146056175231934, -0.6478314399719238, -0.442010760307312, 0.7373747825622559}, {-0.23352652508027982, -0.6162158250808716, -0.09095156192779541, 0.8586330413818359, 0.45060932636260986, 0.7536389646557335, 0.3378760814666748, 0.12827885150909424, 0.16992878913879395, -0.27520740032196045, -0.5112693309783936, 0.02543020248413086, -0.09435342510293276, 0.667400598526001, -0.8976651430130005, 0.4853169918060303, -0.8948501348495483, -0.9710992574691772, -0.45807480812072754, 0.20490431785583496, -0.5397497415542603, 0.5799674987792969, -0.7269076108932495, 0.8092007637023926, -0.7102038860321045, -0.9531334638595581, -0.378597617149353, 0.9197502136230469, 0.28932368755340576, -0.27393531799316406, 0.1821269989013672, 0.6446586847305298, -0.20877492427825928, -0.2612130641937256, 0.26617705821990967, 0.9098929166793823, 0.9109851140252996, 0.396776740167718, -0.9114004373550415, -0.23551952838897705, -0.3934530019760132, -1.253415200740076, -0.6738633246131522, -0.6823678016662598, 0.8069887161254883, -0.9528838396072388, 0.33462440967559814, 0.30369675159454346, 0.4389089345932007, -0.8822777271270752, 0.5672279596328735, 0.4212489128112793, 0.9465622901916504, 0.9653486013412476, -0.6741304397583008, -0.4444969892501831, -0.8009999990463257, 0.10659682750701904, -0.16089677810668945, -0.23136460781097412, -0.19086551666259766, 0.13820171356201172, 0.8760493993759155, 0.5304875373840332, -0.604161262512207, 0.7803606986999512, 0.6062238216400146, 0.013288167647724287, 0.12336695194244385, -0.0793306827545166, -0.04562997817993164, -0.1192706823348999}, {0.3670387268066406, 0.5105122327804565, 0.6100640296936035, -0.8043338060379028, 0.09988821684115035, 0.04424384701644617, -0.36968476699295777, 0.3223358392715454, -0.6719754934310913, 0.35860931873321533, -0.025020718574523926, 0.19176232814788818, 0.020706772804260254, -0.5925247669219971, 0.8577255010604858, -0.8241989612579346, -0.5192595720291138, -0.550608632553812, 0.036420464515686035, 0.048140645027160645, -0.15846688211979806, 0.38126039505004883, -0.6967786550521851, -0.9817409515380859, 0.5407935380935669, 0.13556326426675186, -0.5074102878570557, 0.28230297565460205, 0.7674952745437622, 0.28037304238805927, 0.41871464252471924, -0.08140945434570312, 0.11316204071044922, -0.23413395881652832, -0.02441863287117067, -0.9019904136657715, -0.9845505952835083, 0.45058250427246094, -0.5667641162872314, 0.28466880321502686, 0.8745384216308594, -0.39886200428009033, -0.13305091857910156, -0.6302677392959595, -0.24860882759094238, 0.19663763046264648, 0.4805361032485962, -0.17629894842271465, 0.3593125343322754, -0.23078441619873047, 0.18379104137420654, 0.846956729888916, 0.7473173141479492, -0.06208051563758841, -0.2664511203765869, 0.1965552568435669, -0.2679964303970337, 0.16560101509094238, -0.09895825386047363, 0.398645281791687, 0.8553369045257568, 0.0011310423212344003, 0.14053750038146973, -0.006295323371887207, 0.42895495891571045, -0.39583635275804013, 0.8599096536636353, 0.6181732416152954, -0.9286984205245972, 0.882827639579773, 0.020738601684570312, -0.32576537132263184}, {0.24559485912322998, 0.38872790336608887, -0.12335312366485596, 0.9582360982894897, 0.24618709087371826, -0.4884655475616455, 0.183648487259863, -0.7589395399197305, -0.8782789707183838, 0.4916113615036011, 0.6491992660994061, 0.4637361764907837, -0.9861589670181274, -0.7012978792190552, -0.9441169514587071, -0.5579845905303955, 0.3653777837753296, -0.6341246366500854, 0.796493411064148, -0.023178935050964355, -0.013183832168579102, 0.8496737480163574, -0.6466397047042847, -0.03960371017456055, -0.5825031995773315, 0.14765715599060059, -0.6817355155944824, 0.5799233913421631, -0.7572821679622823, -0.2921750545501709, 0.800969123840332, -0.8873997926712036, -0.370324969291687, -0.7008308172225952, -0.9765870571136475, -0.28845930099487305, -0.037380218505859375, 0.35987401008605957, 0.5389167070388794, -0.3601642847061157, -0.9198516607284546, 0.7994426488876343, 0.48186981678009033, -0.4238743782043457, -0.7363123893737793, 0.703299880027771, 0.4102158546447754, 0.09355318546295166, -0.6262270212173462, -0.22681713104248047, -0.42155683040618896, 0.2049790620803833, 0.3981602191925049, -0.47071077729735356, -0.4333195686340332, -0.02849310014840145, 0.1525799036026001, 0.34679675102233887, -0.9081164598464966, -0.4177953004837036, 0.4650076627731323, -0.4074488878250122, -0.6258494890870101, 0.05953264236450195, -0.4565392732620239, -0.9793446063995361, 0.2833482027053833, -0.2502087354660034, 0.4089745283126831, -0.7897573709487915, 0.6652155857936544, 0.15668237209320068}, {0.7970566749572754, -0.3022456169128418, 0.587503430737933, -0.892780065536499, 0.24609112739562988, 0.465887188911438, -0.24099230766296387, 0.49184489250183105, 0.25192999839782715, 0.7440774440765381, 0.3840336799621582, -0.006106987057296218, -0.6318069696426392, 0.6226900815963745, 0.13156564556470307, 0.8191632032394409, 0.991761326789856, -0.8678690195083618, -0.29329991340637207, 0.7294663190841675, -0.307088301021188, 0.4930288791656494, 0.7307752370834351, -0.9900081157684326, 0.2849907875061035, 0.011147226811488053, 0.9112889768448926, -0.11011815071105957, -0.5345523357391357, -0.9393429756164551, 0.8366122245788574, -0.5608773231506348, -0.2401355504989624, 0.4247310161590576, -0.5602065324783325, -0.9919669044842827, -0.09420153490154819, 0.7426199913024902, 0.7335705757141113, 0.8900752822406128, 0.3523578643798828, -0.5334843397140503, -0.3063298463821411, -0.661076545715332, -0.002318538505460488, -0.6632932424545288, -0.8167687654495239, -0.8217500448226929, -0.05144214630126953, -0.5411381721496582, 0.6533733606338501, -0.16009902954101562, 0.16327762603759766, 0.6018342971801758, -0.4434483051300049, 0.772333025932312, -0.08318555355072021, 0.08226096630096436, -0.2757774591445923, -0.5168598945941667, -0.3070833683013916, 0.17712797007842185, -0.33457446098327637, -0.43524983588394534, -0.7750236988067627, -0.10515081188088031, 0.010462522506713867, -0.4583073854446411, 0.33375322818756104, 0.46187424659729004, -0.8460646867752075, 0.9297462701797485}, {-0.1702103614807129, 0.6430405378341675, -0.3304736614227295, -0.3258328437805176, 0.9414361715316772, -0.7174767255783081, 0.33830034732818604, 0.4093945026397705, -0.7922961711883545, 0.8199542760848999, 0.8149285316467285, -0.04214119911193848, -0.3611551523208618, 0.05863475799560547, 0.02149486541748047, 0.4792139530181885, -0.503508448600769, -0.2520568370819092, 0.021910667419433594, -0.2790340185165405, 0.30603623390197754, 0.7536786794662476, 0.5114033222198486, -0.12733524118075754, -0.667417049407959, 0.18552100658416748, 0.15083253383636475, 0.7085071802139282, 0.46511101722717285, -0.6162307262420654, 0.21750055923335956, 0.8393971920013428, 0.7132594585418701, -0.18209171295166016, 0.2205033302307129, -0.21441171083333765, 0.09340143203735352, 0.9124466180801392, -0.67909836769104, -0.48187875747680664, -0.43454980850219727, -0.40914177894592285, -0.7394218444824219, -0.056632399559020996, -0.12090349197387695, 0.9163500070571899, -0.3585531711578369, 0.4792060852050781, -0.30204927921295166, 0.608569860458374, 0.8852676153182983, 0.28430092334747314, 0.1923205852508545, -0.571739673614502, -0.3534485101699829, -0.14272820949554443, -0.5738720893859863, 0.4472503662109375, -0.4579349727088544, 0.2317489996424774, -0.6892709732055664, -0.9409260749816895, 0.03302264213562012, -0.6449156999588013, 0.27322278356241825, 0.23588931560516357, -0.4279404878616333, 0.6973868608474731, -0.06131744384765625, 2.6786327362060547E-4, -0.6143785715103149, 0.03636932668008892}, {-0.007749676704406738, -0.6943814754486084, -0.4435853958129883, 0.6963165998458862, -0.35582368883826665, -0.5698292255401611, 0.3745688199996948, -0.3099750280380249, -0.7731306552886963, -0.9703800678253174, -0.6544612646102905, -0.9808077803566022, -0.18083620071411133, 0.41373562812805176, 0.5638947486877441, -0.8439168930053711, -0.37620489014582037, -0.8876619338989258, 0.03317904472351074, 1.0642362326242378, -0.0668940544128418, 0.985891580581665, 0.2359853982925415, -0.31213676929473877, -0.37054574489593506, -0.784429669380188, -0.25759851932525635, 0.8546042442321777, -0.8334155082702637, -0.2729421854019165, 0.6908594369888306, 0.4058568477630615, 0.8627592325210571, 0.9949171543121338, -0.428857684135437, -0.6939042806625366, 0.023328661918640137, -0.8705507516860962, -0.6025011539459229, -0.9882830381393433, 0.9943466186523438, -0.3344658613204956, 0.08052146434783936, 0.39623260498046875, -0.7190523147583008, -0.4469735622406006, 0.5896447896957397, -0.18300795555114746, -0.7787329118963279, 0.49085819721221924, -0.1120013497385286, 0.24780797958374023, 0.4625511169433594, -0.07454732987613437, 0.1761702299118042, 0.13819313049316406, 0.224371075630188, -0.9853812456130981, 0.18897247314453125, -0.04126012325286865, 0.9881795644760132, 0.4427295923233032, 0.8488386869430542, 0.25350677967071533, -0.8110636472702026, 0.3564486503601074, 0.7672674655914307, -0.5559005737304688, -0.7361437082290649, 0.28174638748168945, -0.22973573207855225, 0.6164658069610596}, {0.9205141067504883, -0.015423178672790527, -0.208118240171695, 0.8124634027481079, -0.6117515563964844, -0.9697879552841187, 0.3304644203609075, -0.9894789457321167, 0.6104140281677246, -0.28162622451782227, -0.508802056312561, -0.9222317934036255, -0.4425323009490967, -0.5412755012512207, 0.32964011023904427, 0.6331665515899658, 0.06141364574432373, -0.765535831451416, 0.1993502378463745, 0.13656651973724365, -0.316644549369812, 0.44201207160949707, 0.7615300416946411, 0.5371779203414917, 0.9992243051528931, -0.5522909164428711, -0.3007398843765259, -0.7967232210921436, -0.22774970531463623, 0.909431806004115, -0.3458901643753052, 0.16182124614715576, 0.7561362981796265, 0.22208230296714718, -0.19564926624298096, -0.7825294733047485, -0.14301614318390365, -0.042897582054138184, -0.34209489822387695, 0.8431605100631714, 0.11278009414672852, -0.17413067817687988, 0.2270558383123714, -0.33446383476257324, -0.24412751197814941, -0.23257935047149658, -0.7108042240142822, 0.31620240211486816, -0.03821408748626709, 0.25228452682495117, 0.22747266292572021, 0.583266877626927, -0.040040016174316406, -0.580761194229126, 0.7234494686126709, 0.8348405361175537, 0.14490580558776855, 0.4807215929031372, -0.623142023316267, -0.5259882211685181, -0.11709034442901611, 0.8948847055435181, -0.057702121607221324, -0.8013408184051514, 0.31516003608703613, 0.931545615196228, 0.06732368469238281, 0.125380277633667, 0.9422717094421387, 0.8628144264221191, 0.1702486276626587, -0.524340033531189}, {-0.24963247776031494, -0.5326290597999557, 0.3464862108230591, -0.1172705888748169, 0.2507666349411011, 0.634035587310791, 0.629426121711731, 0.48937535285949707, -0.9448301792144775, 0.9857424277582214, 1.2056981821295343, -0.45341727730314496, 0.2763807536152447, 0.5472972393035889, -0.34528051076212857, -0.8145846204142688, -0.2886117348877144, 0.11498034000396729, 0.26078474521636963, -0.1624692678451538, -0.8653862476348877, -0.48262906074523926, -0.6236201524734497, -0.11215460300445557, 0.9580618143081665, 0.35138297658092976, -0.0043424261587833145, 0.44992876052856445, -0.05089008808135986, 0.043689727783203125, 0.10373425483703613, -0.7797297239303589, -0.8093647326407407, 0.9796743392944336, -0.42265594005584717, 0.09733295440673828, 0.4896373748779297, -0.7507818937301636, 0.25754737854003906, -0.7565714120864868, 0.3907376527786255, 0.8358472585678101, -0.7944538593292236, 0.10258913040161133, -0.7249761819839478, -0.513374924659729, -0.6290568113327026, 0.5883611440658569, -0.8242615461349487, -0.5304487943649292, -0.8700644969940186, 0.9074054956436157, -0.2293184995651245, 0.6730943918228149, 0.5750147660038261, 0.7992373704910278, 0.9046286344528198, -0.9911412000656128, 0.6435539722442627, 0.3493926525115967, 0.9499025344848633, 0.009704129728226561, -0.1492694616317749, 0.9727623462677002, 0.7058389186859131, -0.9486106634140015, -0.9424209594726562, 0.6657891273498535, -0.3820263147354126, -0.9436466693878174, 0.2956506013870239, -0.20094166121125978}, {0.07657993517989325, -0.9685134412319212, 0.6504546403884888, 0.5738078355789185, -0.6114317178726196, -0.17758393287658691, -0.32982897758483887, -0.6655716896057129, -0.01044808554990468, -0.3273043632507324, -0.07918787002563477, 0.8094865083694458, 0.457943320274353, -0.5940555334091187, -0.5676472187042236, 0.5747807025909424, 0.8185926675796509, 0.8378438949584961, -0.6181547673274523, 0.7327398061752319, 0.08491457979005498, -0.9900457859039307, 0.5526843070983887, 0.963109016418457, -0.31115448474884033, -0.32456767559051514, 0.6388254165649414, -0.9730929136276245, -0.3486708402633667, -0.4732503890991211, -0.7215611934661865, -0.5909799337387085, -0.7268327474594116, 0.3697571533907751, -0.19577932357788086, 0.9976837635040283, -0.020648479461669922, -0.7952345609664917, 0.4079707860946655, 0.7245029211044312, 0.48942387104034424, 0.6538538669331315, 0.42044874156806733, -0.07346057891845703, 0.33162784576416016, 0.6618057489395142, -0.9306497573852539, -0.15760135650634766, -0.8272875547409058, -0.5514175891876221, 0.699397087097168, 0.11712920665740967, 0.8777559995651245, 0.5072920322418213, -0.4378913640975952, -0.5338559150695801, -0.994605541229248, 1.2817892017170804, 0.056578874588012695, 0.7362613677978516, 0.5473523139953613, -0.6999186277389526, 0.007562617242391451, -0.49764692783355713, -0.8628090620040894, 0.0329362154006958, 0.8687870502471924, 0.050982117652893066, -0.3182961940765381, 0.06842005252838135, 0.401938796043396, -0.8722034692764282}, {-0.47150731086730957, 0.11145021778650245, 0.509839653968811, 0.03252899646759033, 0.04158914089202881, 1.6367435455322266E-4, 0.5859289293747456, 0.677107572555542, 0.30955469608306885, 0.0808490514755249, -0.7111903429031372, -0.2747681140899658, -0.7529617547988892, -0.5633848905563354, 0.46262288093566895, 0.7570003918849506, -0.6528937816619873, -0.7345695495605469, -0.10636568069458008, 0.6242977380752563, -0.38877570629119873, -0.2260061502456665, 0.19292068481445312, -0.8713376522064209, 0.850853681564331, -0.7864453792572021, -0.20532774925231934, 0.1642206907272339, 0.983768105506897, 0.1754244565963745, 0.7239106204699168, -0.8451122045516968, 0.8663453867990802, -0.6481920480728149, -0.409878134727478, -0.8291671276092529, 0.5317707061767578, 0.7039070129394531, 0.2808483839035034, 0.20142829418182373, -0.29690873622894287, 0.2780839204788208, -0.11048576201324867, 0.1563650369644165, 0.42667508125305176, -0.2278064489364624, -0.1408400535583496, 0.7136478424072266, 0.8380637168884277, -0.6219747066497803, 0.43581247329711914, -0.41173744201660156, -0.8261728286743164, 0.4121232032775879, -0.9834868907928467, 0.8128831386566162, -0.6079778671264648, 0.7960797548294067, 0.1731829531779494, 0.20206916332244873, 1.052329379201656, -0.4412723779678345, 0.6281514167785645, 0.31397712230682373, 0.846843957901001, 0.2064828872680664, 0.7551017999649048, -0.07130992412567139, -0.045459747314453125, -0.9555811882019043, -0.8811867237091064, -0.6701503992080688}, {0.6832060813903809, 0.6845265626907349, -0.18892526626586914, 0.58830726146698, 0.33201515674591064, 0.06486010551452637, 0.03553414344787598, -0.1346442699432373, 0.6107673645019531, -0.28558433055877686, -0.6545232534408569, -1.1946330242636785, 1.0789440634744805, -0.9287003405652798, -0.3729259967803955, -0.4005305767059326, -0.8411291837692261, 0.017944763103749855, -0.2122807502746582, 0.5576425790786743, -0.0791400671005249, -0.365397572517395, -0.7753804517078602, -0.5993216037750244, 0.13975739479064941, -0.5846720933914185, 0.9184404611587524, 0.012323260307312012, 0.32447123527526855, -0.39735519886016846, 0.5471111536026001, -0.49733781751575057, 0.30119431018829346, -0.03239119052886963, -0.3627816438674927, -0.3117414712905884, -0.9235711097717285, -0.055925965309143066, -0.6847264766693115, -0.9487564563751221, -0.2449408769607544, -0.45611846446990967, 0.46845483779907227, 0.146081805229187, 0.6275463104248047, -0.4643303155899048, -0.7423957586288452, 0.761286735534668, 0.9719623327255249, -0.44149303436279297, -0.7319345474243164, 0.5088514089584351, 0.36071622371673584, 0.3513185977935791, -0.6100207567214966, 0.5578065987953273, -0.6815270185470581, -0.8986579179763794, -0.26746225357055664, 0.639581561088562, 1.2990212304182647, -0.9503904581069946, 0.9020953178405762, -0.9212276935577393, 0.613510251045227, -0.06127345561981201, 0.7083630104845837, 0.9736795425415039, -0.50394606590271, 0.6479761600494385, -0.15823984146118164, -0.4054957653497895}, {0.424984335899353, 0.8164752721786499, 0.7867735624313354, 0.007294654846191406, 0.409099817276001, 0.2904191017150879, -0.22501834177424623, -0.2493072748184204, 0.7126883268356323, 0.36285516410501856, -0.09587713539024734, -0.8443311452865601, 0.4214353561401367, 0.8140523433685303, -0.6455379724502563, -0.05370232686175895, 0.054184913635253906, 0.5053269863128662, 0.3085198402404785, 0.736100435256958, 0.3380800485610962, -0.8283414840698242, 0.9777833223342896, -0.7639340162277222, -0.6637696027755737, 0.2935457594495624, -0.260634183883667, 0.23357117176055908, -0.4861180977646913, 0.944557785987854, 0.3790395125216433, -0.23617148399353027, -0.8992785215377808, 0.5697802305221558, 0.5076224062948376, 0.735434889793396, -0.4366154670715332, 0.5695973634719849, 0.17922248062962542, 0.71392822265625, 0.7358443737030029, 0.14327597618103027, -0.5731489658355713, -0.7307207584381104, -0.12053787708282471, -0.7690366389158758, -0.16630029678344727, 0.25847458839416504, -0.19691205024719238, -0.8540241718292236, 0.2118295431137085, -0.8024501800537109, 0.8800384998321533, -1.4229631564523888, -0.4501243829727173, 0.7231310606002808, -0.05456709861755371, -0.7081495523452759, -0.352392315864563, 0.05175125598907471, -0.2804338604581443, -0.3898289203643799, -0.19971466064453125, -0.8437734187030441, 0.32203149795532227, -0.13311874866485596, 0.9543460607528687, 0.6890337467193604, 0.13192224502563477, 0.40202486515045166, 0.7343852519989014, -0.6927510499954224}, {0.7891077995300293, 0.24586236476898193, 0.46399080753326416, -0.04735279083251953, 0.5853909254074097, 0.7776864767074585, 0.9527902603149414, -0.3486450695268937, 0.5681555271148682, 0.16263818740844727, 0.025588154792785645, -0.7709560394287109, -4.628896713256836E-4, -0.1213943846248346, 0.4147946834564209, 0.8321979279090916, -0.04449260234832764, 0.09817337989807129, 0.8100985288619995, 0.9054017066955566, -0.06717789173126221, -0.5066272020339966, -0.8125483410375705, -0.5605043466354633, -0.9048608541488647, 0.5607854127883911, 0.9749865531921387, -0.08466036069817012, -0.9966620206832886, 0.18441239120244454, 0.7604089975357056, 0.9430954456329346, 0.7473373413085938, 0.5833453472199603, -0.9102505445480347, -0.12999629974365234, 0.7814759016036987, 0.6531322002410889, -0.2601590156555176, -0.25065433979034424, -0.16910628016199158, 0.37468624114990234, 0.31723177433013916, 0.860323429107666, 0.3012278079986572, -0.10699188709259033, -0.7311567068099976, -0.17998719215393066, -0.7466690540313721, 0.8176422119140625, 0.7420345544815063, -0.6858326196670532, 0.025221943855285645, -0.18941593170166016, 0.4447668790817261, -0.5687130689620972, -0.7014248371124268, 0.09325110912322998, 0.01008152961730957, -0.8055038452148438, -0.9474424123764038, -0.06538116931915283, 0.03511643409729004, -0.3075838088989258, -0.27278268337249756, -0.1452864408493042, 0.9434056282043457, -0.1706458330154419, -0.10519432956938765, -1.7577747932105432, 0.11021900177001953, 0.14677584171295166}, {-0.8193445205688477, 0.4159814119338989, 0.5144577026367188, 0.8433829545974731, 0.8328392505645752, -0.5486541060001893, 0.5175794363021851, -0.7977378368377686, 0.8147910833358765, -0.2692375183105469, 0.5048147439956665, -0.9826879501342773, 0.08076469254742698, -0.7775758504867554, -0.5526351928710938, -0.09126114845275879, -0.19685256481170654, -0.10106878542043085, -0.6250396966934204, -0.4443739652633667, 0.5393606424331665, -0.07564210891723633, -0.38622546195983887, -0.07649528980255127, 0.7991026639938354, -0.06247079372406006, -0.5773767232894897, -0.022990640232494183, -0.2696722745895386, -0.9065203666687012, -0.464674711227417, -0.2574552297592163, 0.09341585636138916, -0.12088894844055176, 0.11615827309438546, -0.6587224006652832, -0.654617428779602, -0.663215160369873, 0.39698450354060255, 0.6234718561172485, 0.025666475296020508, 0.5615081787109375, -0.050570130348205566, 0.5691695213317871, 0.9035145044326782, 0.5794243812561035, -0.6983623504638672, 0.7559235095977783, -0.5260584354400635, -0.5668559819534374, 0.880396842956543, 0.20703354172146918, 0.5786916017532349, 0.365564227104187, 0.3883446455001831, 0.7396982908248901, -0.8194178342819214, 0.5290440320968628, 1.3919882520985307, 0.49155449867248535, -0.36593329906463623, 0.6290934085845947, 0.5100425929138255, -0.18405687808990479, -0.8281980752944946, -0.3703266382217407, 0.8873761892318726, 0.23184109890279972, -0.18057664860362785, 0.7544933557510376, -0.054213523864746094, 0.7967060804367065}, {0.7505493843226914, -2.96627132600546E-4, -0.5495612621307373, 0.7404640913009644, 0.630154013633728, -0.21670126914978027, 0.720178484916687, -0.33412861824035645, 0.9575885534286499, 0.07123851776123047, -0.5771158933639526, -0.9902479648590088, 0.7169927358627319, -0.025353312492370605, -0.7916174655859924, -0.4177696704864502, -0.39319801330566406, 0.41147029399871826, -0.6049894094467163, 0.8256858587265015, 0.3990563154220581, 0.6637012958526611, -0.10313760125693983, -0.4476805026092203, -0.2811252708159162, -0.8299876373474444, -0.022319912910461426, -0.9936638300302554, 0.21895837783813477, 0.8788071211657658, -0.3096722364425659, 0.23655712604522705, 0.7528443336486816, -0.7970255613327026, -0.21345770359039307, 0.2400672435760498, -0.4656403064727783, 0.20624470710754395, -0.7764588594436646, 0.33201706409454346, -0.49555540084838867, 0.037393808364868164, 0.6450921297073364, 0.03942835330963135, -0.8647592067718506, 0.34500133991241455, -0.3384188413619995, 0.5386669635772705, -0.25243616104125977, 0.9198393821716309, 0.7951750755310059, 0.12126612663269043, 0.0658876895904541, -0.6385937929153442, 0.6943877935409546, 0.36300671100616455, 0.19815969467163086, 0.031122684478759766, -0.6585004329681396, -0.5454331636428833, -0.09269868299851704, -0.1899275779724121, 0.9079194068908691, 0.8493199348449707, -0.10405528545379639, 0.2826855182647705, -0.34327757358551025, 0.39362454414367676, -0.9574562311172485, -0.6377052404874768, 0.8482332229614258, -0.5727822780609131}, {-0.04176068305969238, 0.19000756740570068, 0.1630876064300537, -0.7475817203521729, -0.37458550930023193, 0.8880199193954468, 0.6558753252029419, 0.4108215570449829, 0.21603119373321533, -0.42945539951324463, 0.29465484619140625, 0.09422791004180908, 0.2744588851928711, -0.6640793085098267, -0.43846094608306885, 0.5225993394851685, -0.18980145454406738, 0.6045879125595093, 0.7437331676483154, 0.2886710133334136, 0.9585450887680054, 0.7949164498393542, 0.34719038009643555, 0.8487900495529175, -0.44051826000213623, -0.2699776887893677, 0.9446402788162231, -0.17605936527252197, 0.48451073891077745, 0.08890533447265625, -0.05903780460357666, 0.7624969482421875, 0.0894995927810669, 0.23596429824829102, -0.649795651435852, 0.6000032424926758, -0.32481563091278076, 0.3117532730102539, -0.0629899355068823, 0.06278955936431885, 0.7023405639322098, 0.6135625839233398, 0.13444912433624268, -0.2405921220779419, 0.8705302476882935, -0.8536496162414551, 0.8189188241958618, -0.9178116321563721, 0.48097944259643555, -0.7083120346069336, -0.8555774688720703, 0.5273780822753906, -0.1493896245956421, -0.3661576509475708, -0.013029694557189941, -0.08072805404663086, 0.36634892788774076, 0.16454780593787532, 0.7964859008789062, -0.03679537773132324, 0.8269180059432983, -0.8850840330123901, 0.5722798109054565, 0.5542185306549072, 0.9189455509185791, -0.37554049491882324, 0.11237168312072754, 0.3326840400695801, -0.0733480453491211, 0.4462621212005615, 0.44541823863983154, 0.10509395599365234}, {0.2901191711425781, -0.24787449836730957, -0.029399514198303223, -0.20049023628234863, -0.5311316251754761, -0.7459776401519775, -0.7763082981109619, 0.27599334716796875, -0.3828132152557373, 0.9668747186660767, -0.4038550853729248, 0.7367125749588013, -0.17747271060943604, 0.2227039337158203, 0.0533909797668457, 0.6086428165435791, 0.6989595890045166, 0.2780414819717407, 0.165793776512146, 0.7118417024612427, 0.794812798500061, -0.5597118139266968, 0.49228858947753906, 0.35936903953552246, -0.026863694190979004, -0.33687806129455566, 0.7433862686157227, -0.2545685909402515, -0.6976063251495361, 0.06682729721069336, 0.711156964302063, 0.6950505971908569, -0.27667975425720215, 0.09527597760229534, 0.5885641574859619, -0.26373231410980225, 0.7988883256912231, 0.10918521881103516, 0.2477949857711792, -0.9308609962463379, 0.20784413814544678, -0.9624733924865723, -0.693010687828064, -0.9380195140838623, 0.30829107761383057, 0.24544327555910805, -0.612957239151001, -0.8564416170120239, 0.1201420315202661, 0.37798917293548584, 0.8449207544326782, 0.0976417064666748, 1.9038746201495207, 0.5240074396133423, 0.46718692779541016, 0.536410927772522, 0.23793447017669678, 0.4975607395172119, 0.11721527576446533, 0.9568394422531128, 0.8290565013885498, 0.02116239070892334, -0.8481786251068115, -0.0029494762420654297, 0.8456816673278809, 0.7317632436752319, -0.44218742847442627, -0.6822538059123481, 0.21027262009952974, 0.4216651916503906, 0.20622360706329346, 0.11008715629577637}, {0.7373062372207642, 0.6721007823944092, 0.959182620048523, -0.044991493225097656, 0.6674404144287109, -0.319293737411499, 0.5979062514220856, -0.01986706256866455, 0.2864781618118286, -0.04760396480560303, -0.64207923412323, 0.018583522024393312, -0.7414381504058838, 0.9087040424346924, 1.152869162118613, -0.19902312755584717, -0.821942925453186, -0.05601084232330322, -0.3341294527053833, -0.7191156148910522, -0.38805222511291504, -0.8105190992355347, 0.2996504306793213, 0.20091712474822998, -0.7746686935424805, -0.46444761753082275, 0.506826639175415, 0.45597732067108154, 0.5369524955749512, 0.32097184658050537, -0.11735686840947857, -0.809623122215271, -0.7400866746902466, -0.6657865047454834, -0.4771413803100586, -0.9736926555633545, -0.3465993404388428, 0.5707364082336426, 0.6837098598480225, 0.16744863986968994, -0.15551996231079102, 0.5514686107635498, 0.0282442569732666, 0.449554443359375, -0.32529985904693604, -0.06843960285186768, 0.49931561946868896, 0.36493372917175293, -0.46986954620150273, -0.38519835472106934, 0.6361799240112305, 0.6053190231323242, -0.6311873197555542, -0.3620781898498535, 0.7409691634826828, -0.2523021697998047, 0.8215010166168213, 0.7003248929977417, -0.1339174509048462, -0.649744987487793, 0.3298921585083008, 0.38863396644592285, -0.6180192232131958, 0.9306572675704956, -0.6085318326950073, -0.1689239895868756, 0.2392944097518921, 0.018545923922795412, -0.8065946102142334, 0.2209775447845459, -0.2901045083999634, -0.8122931718826294}, {0.4640982151031494, 0.17849159240722656, 0.7290284633636475, -0.5252765417098999, 0.6840039491653442, 0.6527694463729858, -0.35086655616760254, -0.346376895904541, 0.6146441698074341, -0.8518410495310682, -0.01381778112606985, 0.5304169654846191, -0.41425371170043945, -0.0025010108947753906, -0.6884106397628784, -0.5726336240768433, -0.8934377431869507, 0.9453660249710083, -0.557140588760376, -0.05755363359870123, -0.8914353847503662, -0.23423409461975098, -0.17085011696671687, -0.8121792078018188, 0.7491103410720825, 0.9019128084182739, 0.38998186588287354, 0.3611569404602051, 0.9858509302139282, -0.981547474861145, -0.9432969093322754, 0.4233365058898926, -0.37419235706329346, -0.8738703727722168, -0.4063519239425659, -0.04754305724725595, -0.14032983779907227, -0.9916555881500244, 0.5928797557914849, 0.7763921022415161, 0.41101861000061035, 0.02625804075218961, 0.7196950912475586, 0.556540846824646, -0.6381902694702148, -0.6203972101211548, -0.7201777696609497, -0.09344995021820068, 0.4596076011657715, -0.15119171142578125, 0.06326925754547119, -0.7511769533157349, 0.2686805725097656, 0.4889413118362427, -0.37100160121917725, -0.8744935989379883, 0.8962273597717285, -0.272552490234375, 0.16951704025268555, -0.6710202693939209, 0.20663368701934814, 0.3875685930252075, -0.2923920154571533, 0.968298077583313, -0.5691953897476196, 0.35776352882385254, 0.8777387142181396, 0.454315185546875, 0.8842546939849854, -0.49703896045684814, 0.3388444185256958, -0.6352708339691162}, {-0.4436814785003662, 0.8113405704498291, 0.46677303314208984, -0.9188898801803589, 0.18219542503356934, 0.23654958533572928, 0.6727629899978638, 0.39119529724121094, -0.75003981590271, 0.2583162784576416, -0.18373441696166992, 0.845343828201294, -0.3355318307876587, -0.14624524116516113, 0.010171651840209961, -0.9131847620010376, 0.6997761726379395, 0.391806960105896, 0.5919625759124756, 0.222997784614563, 0.9117411603146255, -0.9598557949066162, -0.4428141098874996, -0.8656045198440552, 0.18113696575164795, -0.7756674919459144, 0.24037694931030273, -0.24394071102142334, -0.857872486114502, -0.6284216642379761, 0.4450838565826416, 0.306133508682251, 0.9914522171020508, 0.7521476745605469, 0.09917140007019043, -0.4681103229522705, 3.1500416795417766E-4, -0.9700663089752197, -0.9912961375971105, 0.2018275260925293, 0.902123212814331, 0.883476972579956, 0.12746131420135498, 0.2077326368605663, 0.4887261390686035, 0.6123931407928467, -0.6065787076950073, -0.393119215965271, 0.3127642869949341, 0.43276894092559814, 0.09034109115600586, 0.8894612789154053, -0.25103628635406494, 0.5137103796005249, -0.9712444543838501, -0.053870320320129395, -0.19699060916900635, -0.9060302972793579, 0.1693665878372863, 0.7042605876922607, -0.12953054200447509, 0.327414343668695, 0.47568511962890625, 0.7163103818893433, 0.3657498273007021, -0.6424840688705444, -0.740633487701416, 0.18858087062835693, -0.6286109685897827, -0.22956001510850355, 0.026702046394348145, -0.7200241088867188}};
        w1 = new double[][]{{0.7787755727767944, 0.9835952520370483, -0.5141955614089966, 0.19959062120462842, 0.8985010385513306, 0.18454432487487793, 0.0902885884300501, -0.726078987121582, -0.11724567413330078, -0.6389147043228149, 0.5136678218841553, -0.2393484115600586, -0.8024879693984985, 0.006883859634399414, -0.6317768096923828, -0.44973623752593994, -0.7314032316207886, -0.6110284328460693, 0.6267056465148926, -0.4639700554686892, 0.8323603312505302, 0.20659852027893066, -0.9361821413040161, 0.3259226083755493, 0.3874709049541736, -0.7691302299499512, 0.6388695240020752, -0.9844948053359985, -0.014120936393737793, 1.71303544265772, 0.9842431545257568, -0.6998262733807183, -0.8953449726104736, -1.2925548923081402, -0.4808098077774048, 0.42167484760284424, 0.02277046694622821, -0.4126788377761841, 0.09893035888671875, 0.17224180698394775, -0.9136810302734375, 0.4270068407058716, -0.49058258533477783, -0.2496645450592041, 0.3843475580215454, 0.31733131408691406, -0.668973445892334, 0.8100672960281372, 0.057506680488586426, -0.15454721450805664}, {0.4782137870788574, -0.09189260005950928, 0.6828045845031738, 0.39207005500793457, 0.13421154022216797, 0.47933638095855713, -0.016450881958007812, -0.1634901762008667, -0.8197601562440292, -0.21330976486206055, -0.990283727645874, -0.013857722282409668, 0.9962154626846313, 0.5535688400268555, -0.3826425075531006, 0.5765368658396777, -0.9944251775741577, 0.07635508388884182, -0.05334365367889404, 0.8656909923680749, 0.6773291826248169, 0.6211003065109253, -0.43756771087646484, -0.24335050582885742, 0.9847791194915771, -0.13927924633026123, -0.4495875835418701, -0.16899359226226807, 0.46799755096435547, 0.890365481376648, -0.6225643157958984, 0.5055303573608398, 0.9894777983291546, 0.2873234748840332, -0.15866541862487793, -0.22487938404083252, -0.7335379123687744, 0.19238817691802979, 0.5163182020187378, 0.8046836853027344, 0.7421633005142212, -0.7981223458159941, 0.7422559261322021, 0.4557081460952759, 0.4508172273635864, -0.3864654302597046, -0.5886573791503906, -0.4989558458328247, 0.6192111968994141, 0.9255207777023315}, {-0.791214108467102, -0.7440993785858154, 0.043934666749808594, -0.3583664894104004, -0.8109684828027584, -0.5967000722885132, -0.42079552341308113, 0.20780622959136963, 0.49693983273297976, 0.06865763664245605, -0.6725682020187378, -0.6269837617874146, -0.8085764646530151, -0.050728797912597656, -0.44841063022613525, -0.17886555194854736, 0.9814385175704956, 0.44574272632598877, 0.6549715762002224, -0.2338186502456665, 0.4993717670440674, 0.37989234924316406, -0.05002570152282715, 0.7464778423309326, -0.4370429515838623, 0.500597357749939, 0.08957217349399695, -0.3766211271286011, -0.9120403528213501, 0.14065897464752197, -0.02814185619354248, 0.9963233470916748, 0.028342843055725098, -0.711817741394043, -0.8016661405563354, 0.8025697469711304, -0.7661991119384766, -0.7983108218814854, -0.27904295921325684, -0.7608687877655029, 0.6595370769500732, 0.6786047581337965, 0.9023584127426147, -0.6739376783370972, -0.6895754337310791, -0.9808102016258031, 0.39085912704467773, 0.4243903160095215, -0.43853986263275146, -0.5997371673583984}, {0.37548579945968696, 0.010206744596026359, 0.22133374214172363, -0.6389410495758057, 0.6024926900863647, 0.17567706108093262, 0.012145783181449248, -0.11510038375854492, 0.33860599994659424, 0.4355766773223877, -0.2508805990219116, 0.7995419389747757, 0.31091034412384033, -0.44746696949005127, 0.11014378070831299, 0.21732878684997559, 0.4386996030807495, 0.5870120525360107, -0.3508082628250122, -0.9621856212615967, -0.839458703994751, 0.15897107124328613, -0.7395237684249878, -0.6470053195953369, 0.41603875160217285, -0.31476831436157227, -0.21305012702941895, 0.37090182304382324, -0.4897737503051758, 0.9761223793029785, -0.8405014995077305, -0.8728663921356201, -0.5856344699859619, -0.06971167682466585, 0.6087685823440552, 0.29818952083587646, -0.8161273002624512, 0.37267887592315674, -0.8171732425689697, 0.7038196325302124, -0.636449933052063, 0.744671106338501, 0.36639654636383057, -0.9952061176300049, -0.49774348735809326, 0.19859258573286587, 0.43711187907360416, 0.46383023262023926, 0.5089942405250227, -0.41529202461242676}, {0.2678431272506714, 0.7166711850787661, 0.2890828847885132, -0.3985262743625939, -0.2725323438644409, -0.22127974033355713, 0.920563817024231, 0.5183759927749634, -0.004625201225280762, 0.890779972076416, 0.0854111909866333, 0.5619841814041138, -0.5709983110427856, -0.7895370721817017, -0.8207806348800659, -0.5612421035766602, -0.38913118839263916, 0.6502694722383602, -0.5331499576568604, 0.8857662677764893, 0.6264935731887817, 0.7114015817642212, 0.6822191787886348, 0.4895426034927368, 0.9506669044494629, -0.056441664695739746, 0.9677400588989258, -0.812018871307373, 0.37159526348114014, -0.9611224355571153, -0.9206459522247314, 0.2519195079803467, -0.5921945571899414, -0.005633727059703018, -0.08321809281420478, -0.5433284044265747, -0.48940420150756836, 0.4223407506942749, 0.7809736728668213, 0.9135428667068481, 0.9952113628387451, -1.8632244254485315, 0.8663218021392822, -0.8963738679885864, 0.10680949687957764, 0.2969193458557129, -0.3247338533401489, -0.21709932068608184, 0.9026730060577393, -0.113372802734375}, {0.18416154384613037, 0.046919941902160645, -0.8842565300320233, -0.46257615089416504, -0.8745038509368896, 0.21054887771606445, -0.711289505828542, -0.6291235685348511, 0.4805663824081421, 0.6733357906341553, 0.809789776802063, 0.028846240064518483, -0.1677640676498413, 0.7373287465100111, 0.33105772251076226, 0.6448279273409241, 0.6678262948989868, 0.6687885522842407, -0.42885053157806396, 0.5248034978534715, 0.9746772050857544, -0.9168208837509155, -0.37374722957611084, -0.9687665700912476, 0.013396263122558594, 0.586378812789917, -0.7890113592147827, -0.5746016746116942, -0.6964713335037231, 0.8696870803833008, 0.33746933937072754, -0.5045686960220337, 0.6239537000656128, -0.43599236011505127, -0.7255264520645142, -0.25145208835601807, 0.1603926420211792, -0.8767589330673218, 0.12198734283447266, -0.23146164417266846, -0.17500734329223633, 0.8364936113357544, -0.1848233938217163, 0.7451869249343872, 0.2280268669128418, 0.7601797580718994, 0.47630584239959717, 0.6020573377609253, 0.7993307113647461, -0.4182029962539673}, {-0.3160381317138672, 0.80420982837677, 0.7750235022605261, 0.3715871731283902, 0.7818393707275391, -0.767594575881958, 0.9987921190038203, 0.32732927799224854, 0.027357117276209042, 0.9202498184163883, 0.5013843774795532, 0.2528723478317261, 0.9025341052856638, 0.35643064975738525, 0.1297924518585205, 0.819069504737854, -0.19246497812800636, -0.7966208457946777, -0.3829103708267212, 0.4584110415651561, -0.11634790897369385, 0.5127921104431152, -0.513404130935669, 0.7538230419158936, -0.7701581716537476, 0.6436964273452759, 0.5144921541213989, 0.3762837636555838, 0.8629596105227211, 0.5602721739701801, -0.4705702066421509, -0.6086630821228027, -0.38103675842285156, 1.7408987764127837, 0.6512371301651001, -0.2800670862197876, -0.025957465171813965, 0.4891071496651682, -0.2760910987854004, 0.156843900680542, 0.7724156370634099, -0.3128465414047241, 0.89533531665802, 0.78331458568573, -0.21794354915618896, 0.12216484546661377, 0.8059877157211304, -0.887782096862793, 0.2551347017288208, 0.18807796144778627}, {-0.14171922206878662, -0.3267826910578102, -0.10997903347015381, 0.5321695804595947, -0.5331161022186279, 0.532300591468811, -0.43647003173828125, 0.9482518434524536, 0.9120712280273438, -0.383031964302063, 0.5336905717849731, 0.6985143423080444, -0.8519296228570907, -0.40810704231262207, 0.839211106300354, 0.2889885902404785, 0.1920785903930664, -0.03863537311553955, -0.671673059463501, 0.4891588061478278, 0.1621323823928833, 0.006796360015869141, 0.618262767791748, -0.19221643012631673, 0.8674063682556152, -0.6514182550039528, -0.8973902463912964, -0.32696378231048584, 0.2410823106765747, 0.6450760364532471, 0.9744505882263184, -0.8183802366256714, 0.32188093662261963, -0.2812952995300293, 0.6100221872329712, -0.004052639007568359, -0.2615751028060913, -0.02696704864501953, 0.09715497493743896, 0.49508559703826904, -0.2257983684539795, -0.5959243774414062, 0.19468402862548828, 0.29769081372054307, -0.6762676239013672, -0.792038083076477, -0.42652249336242676, -0.24643456935882568, -0.10374081134796143, -0.6875715491791861}, {0.5334254279473591, -0.018381476402282715, -0.9084231853485107, 0.9761463403701782, -0.07058191299438477, -0.9269955158233643, -0.30042552947998047, 0.029739975929260254, -0.9187242984771729, 0.08069705963134766, 0.43668454068731366, -0.7227528095245361, -0.047933101654052734, -0.04339611530303955, -0.029951416253339858, -0.14644519144315402, -0.6104680299758911, 0.10370779037475586, 0.5190280675888062, 0.0707390308380127, -0.5133689641952515, -0.39919888973236084, 0.07468271255493164, -0.5344240665435791, 0.3665449619293213, 0.049995157437226496, 0.992486834526062, -0.6338534355163574, -0.9730652570724487, -0.6227142810821533, 0.257571816444397, 0.11893439292907715, 0.02355170249938965, 0.6258459091186523, 0.1490548849105835, 0.4237058162689209, -0.4825718402862549, 0.7672373056411743, 0.034380555152893066, -0.9617081880569458, 0.7665196657180786, -0.34573704683654194, -0.7794276475906372, 0.872812032699585, 0.5118125677108765, 0.8820250034332275, -0.32333648204803467, 0.40529119968414307, -0.1104511022567749, -0.829981803894043}, {0.35010862350463867, 0.036426663398742676, -0.2340607484352779, -0.013365387916564941, 0.24404847621917725, -0.16165566444396973, 0.699299693107605, -0.7965855598449707, -0.5899630784988403, 0.05819892883300781, -0.7751829624176025, -0.14632904529571533, 0.20326614379882812, -0.5091797113418579, -0.25135064125061035, -0.35652874672606144, 0.38854193687438965, -0.3704608052017282, -0.491935133934021, 0.7236700057983398, 0.5725220441818237, -0.3394384267778414, 0.45769771924151215, -0.6000442504882812, -0.41020238399505615, 0.9388740062713623, 0.058867812156677246, -0.8076279163360596, -0.04035105657615487, 0.45022475719451904, -0.2260735034942627, -0.2043430602007008, 0.7426453828811646, 0.2595996856689453, -0.5880917310714722, -0.005918065318886902, -0.011528604628564766, -0.5099737644195557, 0.03968179225921631, -0.7173529863357544, 0.6993122100830078, -0.1320711374282837, -0.6623955965042114, -0.909170389175415, 0.7129026651382446, -0.5322216749191284, -0.6836053133010864, 0.14129745960235596, 0.25436675548553467, -0.9162434339523315}, {-0.6045079231262207, 0.5449540615081787, 0.7627819776535034, 0.969444751739502, 0.43128907680511475, 0.7101127211947509, 0.41060948371887207, 0.6640673875808716, -0.6351262878882966, 0.6149470806121826, 0.8241088390350342, -0.4196528196334839, 0.2012108564376831, -0.5716358423233032, 0.7633905410766602, 0.09784889221191406, -0.4808121919631958, -0.31320512294769287, -0.025987625122070312, 0.77837073802948, -0.29633665084838867, 0.9698324203491211, 0.9903280735015869, 0.41547536849975586, 0.2779958248138428, -0.9508672952651978, -0.8237223717987414, 0.35238659381866455, -0.15413296222686768, -0.9242227077484131, 0.14716660976409912, 0.3522481918334961, 0.7863564491271973, -0.02848207950592041, 1.2647177010958122, -0.8261170387268066, -0.10841083526611328, -0.20870113372802734, -0.7952486276626587, -0.31217122077941895, 0.4697272777557373, 0.6623409986495972, 0.5232933759689331, -0.1384294033050537, 0.19583308696746826, 0.5554664134979248, -0.6690102815628052, 0.8329178506255737, -0.9345964193344116, 0.3019945411721968}, {0.44900333881378174, -0.11249113082885742, -0.09848546981811523, -0.27799856662750244, 0.0855100154876709, 0.04380476474761963, -0.626591383503212, -0.7972971200942993, -0.9606623649597168, 0.11680411394859679, 0.7330686210315056, -0.20913457870483398, -0.5011705160140991, 0.4312760829925537, -0.6918240785598755, -0.8417649269104004, 0.4403733015060425, 0.6256643533706665, 0.8562756776809692, 0.6659814119338989, 0.026321475952688713, -0.9565163850784302, -0.49408211137228364, -0.8783208131790161, 0.34803736209869385, -0.24549825417105997, -0.11753101562404256, 0.6700043678283691, 0.168593168258667, 0.291414737701416, -0.3457145690917969, 0.4915221929550171, -0.1137474775314331, 0.03800344467163086, -0.6543772220611572, 0.720273494720459, 0.20475876331329346, -0.2490067481994629, 0.5277755167894251, 0.2896914482116699, 0.6880548000335693, -0.7708518505096436, 0.6749886274337769, 0.27771270275115967, -0.7783136367797852, 0.3258768320083618, -0.23657572269439697, 0.7457702159881592, -0.5856668949127197, 0.31498692110029053}, {-0.41755545139312744, -0.18115830421447754, 0.12754391816589283, 0.8583530187606812, 1.7461415674060887, -0.661400556564331, -0.47551286220550537, -0.7377121448516846, 0.6665985584259033, 0.9936492443084717, 0.9369416236877441, -0.6534101963043213, 0.25303685665130615, -0.2849409652420969, 0.09596610069274902, 0.1169658899307251, 0.10445511341094971, -0.4000293016433716, 0.14518952369689941, -0.17857885360717773, 0.5817080736160278, 0.19005052696456806, 0.9903225898742676, 0.8248523473739624, 0.5513424873352051, 0.9204273223876953, -0.759650707244873, -0.3213430643081665, -0.12350833415985107, -0.9384828805923462, 0.3281562328338623, 0.2362520694732666, -0.3374446630477905, -0.1673755645751953, 0.8426423072814941, 0.9251565933227539, -0.5335345268249512, 0.9475500583648682, -0.08900535106658936, -0.14143943786621094, -0.35876786708831787, -0.3592662811279297, 0.08895385265350342, 0.38909087248823365, -0.23715054988861084, -0.6355600420389418, 0.6485341787338257, 0.6245832443237305, 0.8324943620615011, 0.9213593006134033}, {-0.6027145385742188, -0.828237771987915, 0.4237144230576073, 0.9499866962432861, -0.7002435922622681, -0.2527362108230591, -0.9480627775192261, 0.053924374570193834, -0.5754315853118896, 0.302898645401001, -0.9551289081573486, 0.8179279565811157, 0.24465882778167725, -0.5180761814117432, -0.44633352756500244, -0.9888334274291992, 0.006884455680847168, -0.4053612947463989, 0.44756853580474854, 0.6863957643508911, -0.840965747833252, -0.9667491912841797, 0.280603289604187, 0.06479442119598389, 0.9799846410751343, 0.1361759901046753, -0.2789795398712158, 0.7852296829223633, -0.8315709829330444, 0.08386409282684326, -0.6724766492843628, 0.5498781204223633, -0.003914780483539576, -0.058551788330078125, 0.46386486861261145, 0.8656744956970215, 0.8607137203216553, -0.9726177782984116, 0.42649269104003906, -0.21112549304962158, -0.3996177911758423, -0.29991471767425537, -0.04758508835362574, 0.019473910331726074, 0.1480883126649939, 0.48405373096466064, -0.0708470344543457, 0.1941758394241333, -0.12370848655700684, -0.9773801565170288}, {0.10731065273284912, 0.7341585159301758, 0.5878349542617798, 0.34892189502716064, 0.5446804761886597, -0.19438910484313965, 0.7856605052947998, 0.47836183791831477, -0.5269879102706909, 0.1981428861618042, -0.6202682256698608, 0.5455214977264404, 0.18819222484212905, 0.9226057529449463, -0.9826483726501465, 0.604209303855896, 0.7919243574142456, 0.5019153356552124, -0.45142316818237305, -0.6454297304153442, 0.6378767490386963, -0.22955517951678273, 0.9427066180805086, -0.9262253046035767, -0.2301245927810669, -0.17982063849749158, -0.3434414863586426, -0.25151896476745605, -0.5549181699752808, -0.1438292681621829, 0.550217866897583, -0.19492173194885254, -0.5377280712127686, 0.06342196464538574, -0.1466741869154963, 0.06706097740564004, -0.26798510551452637, 0.45035600662231445, 0.6778408288955688, 0.05241274833679199, -0.6354069709777832, -0.8699592351913452, 0.3101005554199219, 0.12515723705291748, -0.7494240999221802, -1.1752425468135037, -0.9555280208587646, 0.7263389825820923, 0.8374795913696289, -0.946136474609375}, {0.1642158031463623, -0.36995859785278074, 0.5291135311126709, -0.5682958364486694, -0.25165343284606934, -0.3491036891937256, 0.34313297271728516, 0.17304551601409912, -0.6863007545471191, -0.1511380672454834, -0.8478473424911499, 0.01992940902709961, -0.3890615701675415, -0.05023372173309326, -0.6530986568386633, -0.7824338674545288, 0.7411351203918457, -0.09804797172546387, -0.5307822227478027, 0.5398584604263306, -0.4796419143676758, -0.6116718053817749, -0.7445583343505859, -0.5066020488739014, 0.21813833713531494, 0.5604274272918701, 0.7506310939788818, 0.2355290651321411, -0.10356998443603516, -0.20923960208892822, -0.8005943298339844, 0.530271053314209, -0.26610636711120605, 0.7090656908554805, -0.09395051002502441, -0.9165300130844116, -0.058966755867004395, -0.28078958824023537, 0.48476922512054443, -0.47580866668599286, -0.26283907177523075, -0.8737026453018188, -0.793487548828125, -0.3225773572921753, -0.4520763158798218, -0.05790293216705322, 0.2480703592300415, -0.8459359407424927, -0.7068567276000977, 0.0038902759552001953}, {-0.3456498384475708, 0.7807280908520557, 0.29601666591671183, -0.5117447376251221, -0.7025676965713501, 0.46609604358673096, 0.40792620182037354, 0.3687105178833008, -0.05200779438018799, -0.7198190985075108, 0.9057748317718506, 0.23565353406028006, 0.44855237007141113, 0.722681999206543, 0.17340409755706787, -0.25083655542939054, -1.1412610332540951, 0.052857279777526855, -0.907113790512085, -0.10243995107787907, -0.49269306659698486, -0.9935286808480328, 0.049231529235839844, -1.6343494302164914, -0.27426302433013916, 0.13520455360412598, -0.557835578918457, -0.5053898096084595, -0.3110743761062622, 0.4789912700653076, 0.6716792583465576, 0.15974056720733643, 0.7730188369750977, -0.7497060166165392, -0.5610113143920898, 0.52196204662323, -0.5280779484692515, 0.7282106324853123, 0.41737067699432373, -0.5179893970489502, 0.7420876026153564, 0.9269341230392456, -0.6271917819976807, 0.9403400421142578, 0.7796521186828613, 0.6273161172866821, -0.3477928638458252, 0.1935044527053833, -0.9790410995483398, 0.6255215406417847}, {0.27403175830841064, 0.21634010797702755, -0.8163129091262817, -0.34373414516448975, -0.7850675582885742, -0.7462595701217651, -0.24203239150422404, 0.18101994964389054, 0.011346578598022461, 0.6315081329194587, -0.21134495735168457, -0.35074457566235456, -0.2718862295150757, 0.2493664026260376, -0.3718186616897583, 0.15129876136779785, -0.9131805896759033, -0.5916178226470947, -0.817543529202148, 0.7415449209225147, -0.522436261177063, 0.8743630647659302, -0.9481096267700195, 0.7217525243759155, 0.6938069545917835, 0.5342379900086773, -0.10751456962578221, -0.6701127290725708, 0.8767589330673218, 0.6373140811920166, 0.4646003246307373, 0.11008155345916748, -0.37187862396240234, -0.44557785987854004, 0.1857854127883911, 0.20169830322265625, -0.29820406436920166, 0.5073003768920898, -0.9069205522537231, 0.38949992530774585, 0.12366365006704053, -0.6827945709228516, -0.6225210428237915, -0.8183979988098145, -0.8513293828682615, -0.48740482330322266, 0.43962156772613525, -0.39187824726104736, -0.46669328212738037, -0.819269061088562}, {-0.40501630306243896, 0.806147575378418, -0.8030111789703369, 0.9983267784118652, -0.8058754205703735, -0.6270185708999634, 0.7227886915206909, -0.6713278293609619, 0.33257293701171875, 0.2150794267654419, -0.8117480278015137, 0.8679262399673462, -0.8815631866455078, 0.22180528104278285, 0.4505718946456909, 0.20065152645111084, 0.6794182062149048, 0.18243122100830078, -0.6443222761154175, 0.4372810125350952, -0.30599725246429443, -0.016037702560424805, -0.2674429416656494, -0.17060697078704834, 0.28307271003723145, 0.3549155424268551, -0.8492594957351685, 0.9300252199172974, 0.34330081939697266, 0.6529476642608643, 0.3006289005279541, -0.2000526063777186, 0.03845226764678955, -0.23022224796179103, 0.8973598480224609, -0.7148188352584839, 0.11198508739471436, 0.9080557823181152, 0.3624234199523926, -0.3143404722213745, 0.9441062211990356, 0.3871530294418335, 0.2032557725906372, 0.3406254053115845, -0.15129530429840088, 0.9891171455383301, -0.010448098182678223, -0.5138478662667232, -0.009649395942687988, 0.30602729320526123}, {-0.6404772996902466, -0.3795124292373657, 0.005858421325683594, 0.19975852966308594, -0.8508671522140503, -0.4387718439102173, -0.19668543338775635, -0.2203195559083886, -0.311611533164978, 0.35106992721557617, -0.869171142578125, 0.03184521198272705, 0.7624056339263916, -0.7319859266281128, -0.29015612602233887, -0.4188501371828295, -0.0729137659072876, 0.703252911567688, 0.012716889381408691, 0.18518400192260742, 0.24817657470703125, 0.7049870491027832, -0.5603018999099731, 0.9133381843566895, -0.5183879137039185, -0.22926604747772217, 0.19528412818908691, 0.6695399284362793, -0.9678770303726196, 0.7337276623062134, -0.46216797828674316, -0.8802241086959839, -0.7834702730178833, -0.5053806304931641, 0.5470291376113892, 0.39690446853637695, 0.6101087331771851, 0.02431679174323419, -0.9129875898361206, 0.5338548421859741, -1.565995886186089, 0.30131959915161133, 0.23863697052001953, -0.8451296091079712, -0.602509617805481, 0.6436842679977417, 0.47578508275232245, -0.8910626173019409, -0.9277275800704956, 0.5306055545806885}, {-0.6826256513595581, -0.19841539859771729, 0.2630643844604492, 0.6359137296676636, -0.5668917894363403, -0.4752897024154663, -0.3905220031738281, -0.1211327314376831, -0.4001779556274414, 0.2889796495437622, 0.5650681257247925, 0.21812951564788818, 0.644356369972229, 0.2631082534790039, -0.8739638328552246, -0.33789587020874023, -0.20557725429534912, 0.32079648971557617, -0.1086893081665039, -0.8714064359664917, 0.17553043365478516, 0.31468772888183594, 0.8059999942779541, -0.9717811346054077, -0.1670980453491211, -0.8971303701400757, 0.6473033150917671, 0.5070484932164594, 0.10241198539733887, 0.16025376319885254, 0.9018863439559937, 0.1733022928237915, 0.12700701226019007, -0.22681283950805664, 0.585014820098877, -0.08906888961791992, -0.7982903718948364, 0.2941610813140869, -0.5184099674224854, 0.35629403591156006, 0.9332463781649406, 0.7245030403137207, 0.2074817419052124, -0.4243961572647095, 0.16897964477539062, 0.9964487552642822, 0.04440629482269287, 0.07922923564910889, -0.3722425834788085, 0.7420103549957275}, {-0.08857964658394235, 0.8004953861236572, 0.8908281326293945, -0.5281319618225098, -0.04883754253387451, 0.24887442588806152, -0.1363513469696045, 0.9324772357940674, 0.04950714111328125, -0.8232959508895874, -0.39458661560410924, 0.277011513710022, 0.2634488344192505, -0.27619731426239014, -0.006936539126419736, 0.7367242574691772, -0.7738082408905029, -0.8348246812820435, 0.03228151798248291, 0.7961469313251286, 0.6930495500564575, 0.558592677116394, 0.3045370578765869, -0.17085587978363037, -0.4650862216949463, -0.37854886054992676, -0.1306450366973877, -0.9195799827575684, 0.79777991771698, -0.4696664810180664, -0.10267484188079834, -0.39556241035461426, 0.7595443725585938, -0.5676062107086182, -0.3968623876571655, 0.14366090297698975, -0.6296371221542358, 0.6158932447433472, -0.6657732725143433, -0.32691311836242676, -0.5195587873458862, 0.5079172849655151, 0.9906804592619096, -0.06281375885009766, -0.16079556941986084, -0.8787342308045714, 0.4102874994277954, -0.08651864528656006, 0.8706156539753611, -0.44786202907562256}, {-0.10167884826660156, 0.9826815128326416, 0.6856776475906372, -0.4545421956904889, -0.3982137441635132, 0.8696808815002441, -0.32838165760040283, 0.034552574157714844, -0.5703596517761844, -0.19765400886535645, -0.5902162790298462, 0.8919122921238345, -0.2113792896270752, -0.4304697513580322, 0.9495762586593628, -0.6070003509521484, 0.21727311611175537, -0.8845827579498291, -0.17701148986816406, -0.49644362926483154, 0.6820967197418213, 0.5139337778091431, 0.7450650724173116, 0.7667217254638672, 0.5834038613960517, -0.531084418296814, -0.23886752128601074, -0.331579327583313, 0.6843281984329224, -0.4834026098251343, 0.667618147883462, -0.4359316825866699, -0.14769864082336426, 0.5386446714401245, 0.8858488052440815, 0.736696720123291, 0.8858111862717837, 0.4380829334259033, -0.28850996494293213, 0.8763139860597674, 0.19986653327941895, -0.11288011074066162, 0.19222843647003174, -0.41876673698425293, -0.5768624544143677, -0.07500076293945312, -0.3387678861618042, 0.5725699663162231, 0.7161293029785156, 0.865662693977356}, {-0.3826850652694702, -0.5970057249069214, -0.38878440856933594, -0.31242181947567077, -0.5834156274795532, -0.5568777322769165, -0.20283198356628418, -0.8389073610305786, -0.9792990684509277, 0.8174958229064941, 0.3324151039123535, 0.5244263410568237, -0.04066169261932373, -0.389005184173584, -0.1981675624847412, -0.3152353384051594, -0.10111474990844727, -0.3698751926422119, 0.8053077912481321, -0.9213203191757202, 0.4250730051096321, 0.5767253637313843, 0.4396700859069824, -0.9883841276168823, -0.4512145519256592, -0.7458637952804565, -0.5800131559371948, 0.4361984635773429, -0.09494433404395983, 0.7712136507034302, -0.5355759859085083, 0.28262269496917725, 0.264133095741272, -0.24158215522766113, 0.7014081478118896, 0.7242746353149414, 0.5254199504852295, -0.7784775495529175, 0.31620943546295166, -0.4314886282872256, 0.6618008613586426, 0.8323206901550293, -0.6659433841705322, 0.08698368072509766, 0.18209314346313477, 0.9273211228056284, 0.4507854905236208, 0.3394724130630493, -0.04839050769805908, -0.03539312025815258}, {0.20659650472189114, 0.8259247541427612, -0.18451988697052002, -0.7290428876876831, -0.009404540061950684, 0.182564377784729, -0.09536087512969971, -0.697639837733639, 0.19243762569888442, -0.5381896495819092, -0.5720360279083252, -0.873844353304442, -0.825332760810852, -0.34587883949279785, 0.3663572072982788, 0.6727026700973511, 0.9061614274978638, -0.33075928688049316, -0.6363515853881836, 0.5648635625839233, -0.9865564107894897, -0.9763104915618896, 0.8729071617126465, 0.11065685749053955, 0.20786774158477783, -0.34502530097961426, 0.683273434638977, -0.04932141304016113, -0.7039556503295898, 0.43411386013031006, -0.06545567512512207, 0.44512641429901123, -0.48668909072875977, -0.7296701669692993, -0.5603519678115845, -0.5446881055831909, 0.14326775074005127, 0.3365447521209717, -0.3394770622253418, -0.4851680369820459, 0.08628356456756592, -0.10843682289123535, -0.9791857004165649, 0.7282469272613525, 0.034012675285339355, -0.4525507634217696, 0.3891878128051758, -0.6834138631820679, 0.7136087417602539, -0.49991822242736816}, {0.2317412059259647, -0.0067147016525268555, 0.03269648551940918, 0.11446404457092285, 0.6399438381195068, 0.38758742809295654, 0.4860694937024328, -0.12481260299682617, -0.5855265371164774, 0.7950031757354736, -0.1568673849105835, 0.048798203468322754, 0.31163036823272705, 0.614726185798645, 0.924017071723938, 0.8740444183349609, 0.8510814905166626, -0.970263653762024, 0.7837046592011392, 0.7711107730865479, -0.6674442291259766, 0.8987431526184082, -0.21466028690338135, 0.4487422704696655, 0.6137257814407349, -0.604413628578186, 0.4817277193069458, -0.756118893623352, 0.905921459197998, -0.7448185682296753, 0.7300467491149902, -0.4779934883117676, -0.16988861560821533, -0.5610824823379517, 0.10919511318206787, 0.37456393241882324, 0.8739333682510919, -0.8396203517913818, -0.5511138439178467, -0.6794219017028809, 0.1713395118713379, -0.061647798587354134, 0.20477068424224854, 0.25274336338043213, 0.10212326049804688, -0.7545565366744995, -0.07785987854003906, -0.5360084772109985, 0.7036749124526978, -0.40410947799682617}, {-0.3466470241546631, 0.8906959295272827, -0.8652619123458862, 0.6521635055541992, -0.23353219032287598, -0.6511703729629517, -0.688795804977417, 0.2727210521697998, 0.04609525203704834, -0.5332762002944946, -0.22005295753479004, 0.3894917964935303, 0.7574733128375462, -0.8981465101242065, 0.36386537551879883, -0.39029888834163895, -0.8155845403671265, 0.314835786819458, 0.23928213119506836, 0.375980019569397, -0.97447669506073, -0.5901502370834351, -0.26615071296691895, -0.6677819490432739, -0.09852206707000732, -0.32008397579193115, 0.20481526851654053, -0.755389928817749, -0.7416216135025024, -0.84432053565979, -0.21102404594421387, -0.020238280296325684, 0.25431860517011096, -0.5057680606842041, -0.3308548927307129, -0.8814781904220581, -1.3562766465499778, -0.20510053634643555, 0.7206644755946637, -0.2004854679107666, 0.8903456926345825, 0.5918141603469849, 1.0292376915624828, -0.4112701416015625, -0.8114354610443115, 0.5484740734100342, -0.5276608467102051, 0.09275937080383301, 0.41055989265441895, -0.5326071977615356}, {0.6644411087036133, -0.4944208860397339, -0.5074448585510254, 0.05201137065887451, 0.93467116355896, -0.09031355381011963, -0.2935042381286621, -0.9627183675765991, 1.9227208695772096, -0.93101966381073, 0.49456894397735596, 0.21601343154907227, 0.6313920021057129, -0.28424322605133057, 0.9264473915100098, -0.49643027782440186, -0.6238651275634766, -0.8279653787612915, -5.226147213081909E-4, 0.6698987483978271, 0.650212287902832, 0.9401590824127197, 0.24559783935546875, -0.804054856300354, 0.44658050618055767, 0.7079710960388184, 0.23977649211883545, -0.21657073497772217, 0.3305001464944901, -0.4603966474533081, 0.7792208194732666, -0.36622118949890137, 0.1675405502319336, 0.35303616523742676, 0.44274353981018066, 0.4733232259750366, -0.28317081928253174, 0.6781265735626221, -0.7901701927185059, -0.27430784702301025, -0.33738231658935547, -0.8560965061187744, -0.4498132689826049, -0.7813153266906738, -0.6071356534957886, -0.8385027647018433, -0.48476314544677734, 0.36170709133148193, 0.3241109969698214, 0.8491512537002563}, {0.13730216026306152, -0.09585678577423096, 0.5293674468994141, -0.4426891803741455, 0.7020329236984253, 1.0906973576661678, 0.510124683380127, 0.03611302375793457, -0.544568657875061, 0.8268264532089233, 0.35450172424316406, -0.804180383682251, -0.6746026277542114, 0.17332899570465088, 0.6291998624801636, -0.2722296714782715, 0.5108140707015991, -0.06498706340789795, -0.7285230159759521, 0.10644829273223877, 0.8789301244639973, 0.045284152030944824, 0.8816819190979004, 0.06408754765066452, -0.39348816871643066, 0.5367708800036569, 0.181371808052063, 0.17735648155212402, -0.059458136558532715, 0.49982237815856934, -0.49305403232574463, 0.224573016166687, -0.7781376838684082, 0.5151772499084473, 0.8278719186782837, 0.005251646041870117, -0.7903620004653931, -0.24181056022644043, 0.21021687984466553, 0.8253237009048462, -0.3628612756729126, -0.5022432804107666, 0.8903435468673706, -0.49028265476226807, 0.17736029624938965, 0.1038884145207446, 0.18026375770568848, 0.06096030706084332, 0.11057615280151367, 0.5746966600418091}, {-0.8039584159851074, 0.9867172241210938, -0.04672205448150635, -0.9774398803710938, -0.23990869522094727, 0.865547776222229, 0.6518187522888184, -0.2524780035018921, -0.47605228424072266, 0.6250496618613515, 0.8450569869265396, -0.7798027992248535, -0.2687782049179077, -0.5864347219467163, -0.4498990774154663, 0.355546236038208, 0.9137778282165527, 0.6200439929962158, -0.12959516048431396, 0.580308198928833, -0.6194043108729959, -0.08295547962188721, 0.2035682201385498, 0.5446927547454834, -0.22582542896270752, -0.44136321544647217, 0.6589921712875366, -0.9402182102203369, -0.6619727611541748, 0.5966057777404785, -0.7893587350845337, 0.8241792917251587, -0.8640046119689941, 0.9416877031326294, -0.7150899171829224, 0.9428983926773071, -0.8209326267242432, 0.4022182696351619, 0.06642372485458536, 0.7434310913085938, -0.8624411821365356, -0.875278115272522, -0.905045747756958, -0.9417048021484973, -0.3619556427001953, -0.8740198612213135, -0.6400643587112427, 0.971122145652771, 0.15552127361297607, -1.147163655269807}, {-0.23352652508027982, -0.6162158250808716, -0.09095156192779541, 0.8586330413818359, 0.45060932636260986, 0.543351411819458, 0.3378760814666748, 0.12827885150909424, 0.16992878913879395, -0.27520740032196045, -0.43290759330765716, 0.02543020248413086, 0.6277670264308923, 0.667400598526001, 0.6836397278653952, 0.4853169918060303, 0.3059704304219091, -0.9710992574691772, 0.7466017011921815, 0.20490431785583496, 0.045426185920285356, 0.5799674987792969, -0.7269076108932495, 0.8092007637023926, -0.7102038860321045, 0.7951094314835534, -0.378597617149353, 0.9197502136230469, 0.28932368755340576, -0.27393531799316406, 0.1821269989013672, 0.6446586847305298, -0.20877492427825928, -0.2612130641937256, 0.26617705821990967, -0.1069571414680901, 0.9109851140252996, 0.396776740167718, -0.9114004373550415, -0.23551952838897705, -0.3934530019760132, -0.8534101247787476, 0.04076337814331055, -0.6823678016662598, 0.8069887161254883, -0.9528838396072388, 0.33462440967559814, 0.30369675159454346, 0.4389089345932007, -0.8822777271270752}, {0.3670387268066406, 0.5105122327804565, 0.6100640296936035, -0.8043338060379028, 0.4832495450973511, -0.6365728495159093, -0.5326732397079468, 0.3223358392715454, -0.6719754934310913, 0.35860931873321533, -0.025020718574523926, 0.19176232814788818, 0.020706772804260254, -0.5925247669219971, 0.8577255010604858, -0.8241989612579346, -0.5192595720291138, 0.4413310985017328, 0.036420464515686035, 0.048140645027160645, -0.24981796741485596, 0.38126039505004883, -0.6967786550521851, -0.9817409515380859, 0.5407935380935669, -0.2017134428024292, -0.5074102878570557, 0.28230297565460205, 0.7674952745437622, 0.8028253316879272, 0.41871464252471924, -0.08140945434570312, 0.11316204071044922, -0.23413395881652832, -0.08562219142913818, -0.9019904136657715, -0.9845505952835083, 0.45058250427246094, -0.7065304777863841, 0.28466880321502686, 0.8745384216308594, -0.39886200428009033, -0.13305091857910156, -0.6302677392959595, -0.24860882759094238, 0.519021038885898, 0.4805361032485962, -0.488552451133728, -0.7290288283744655, -0.23078441619873047}, {0.24559485912322998, 0.38872790336608887, -0.12335312366485596, 0.9582360982894897, 0.24618709087371826, -0.4884655475616455, 0.8709585666656494, -1.5627294726341079, -0.8782789707183838, 0.4916113615036011, -0.21818315982818604, -0.6355643040355927, 0.4039714190340804, -0.7012978792190552, -0.6380733251571655, -0.5579845905303955, 0.3653777837753296, -0.6341246366500854, 0.796493411064148, -0.023178935050964355, -0.013183832168579102, 0.8496737480163574, -0.6466397047042847, -0.03960371017456055, -0.5825031995773315, 0.14765715599060059, 0.34764843399133993, 0.5799233913421631, -0.8327113106698816, -0.2921750545501709, 0.800969123840332, -0.8873997926712036, -0.370324969291687, -1.15724843408272, -0.9765870571136475, -0.28845930099487305, -0.037380218505859375, 0.35987401008605957, 0.5389167070388794, -0.3601642847061157, -0.9198516607284546, 0.7994426488876343, 0.48186981678009033, -0.4238743782043457, -0.7363123893737793, 0.703299880027771, 0.4102158546447754, 0.09355318546295166, -0.6262270212173462, -0.649180577742032}, {0.7970566749572754, -0.3022456169128418, 0.7164900302886963, -0.1304110897939509, 0.24609112739562988, 0.026352122492798755, -0.24099230766296387, 0.49184489250183105, 0.25192999839782715, 0.2244453751733947, 0.3840336799621582, 0.0038262605667114258, -0.6318069696426392, 0.6226900815963745, 0.921674370765686, 0.8191632032394409, 0.991761326789856, -0.9180350848346781, -0.29329991340637207, 0.710058464709362, 0.2315455675125122, 0.23489020348361755, 0.7307752370834351, -0.9900081157684326, 0.2849907875061035, -0.6722769737243652, 0.7389816045761108, 0.12889386248285242, -0.5345523357391357, 0.7495979358045632, 0.8366122245788574, -0.5608773231506348, -0.2401355504989624, -0.24041533849515778, -0.5602065324783325, 0.6574417352676392, 0.09508419036865234, 0.7426199913024902, 0.7335705757141113, -0.41644060611724854, -0.7063692025595221, -0.5334843397140503, -0.3063298463821411, -0.661076545715332, -0.4064849615097046, -0.6632932424545288, -0.8167687654495239, -0.8217500448226929, -0.05144214630126953, 0.38053683549202627}, {-0.1702103614807129, 0.6430405378341675, -0.3304736614227295, -0.3258328437805176, 0.9414361715316772, -0.7174767255783081, 0.33830034732818604, 0.9793049535631408, -0.7922961711883545, -0.501196001751673, 0.8149285316467285, -0.04214119911193848, -0.3611551523208618, 0.05863475799560547, 0.02149486541748047, 0.4792139530181885, -0.503508448600769, -0.2520568370819092, 0.021910667419433594, 0.8173091594914221, 0.30603623390197754, 0.7536786794662476, 0.5114033222198486, 0.42289090156555176, -0.667417049407959, 0.18552100658416748, 0.15083253383636475, 0.7085071802139282, 0.46511101722717285, -0.6162307262420654, -0.2187035083770752, 0.8393971920013428, 0.7132594585418701, -0.18209171295166016, -0.4234032622876651, -0.4851902723312378, 0.09340143203735352, 0.9124466180801392, -0.67909836769104, -0.48187875747680664, -0.43454980850219727, -0.40914177894592285, -0.7394218444824219, -0.056632399559020996, -0.12090349197387695, 0.9163500070571899, -0.3585531711578369, 0.4792060852050781, -0.30204927921295166, 0.608569860458374}, {-0.007749676704406738, -0.6943814754486084, -0.4435853958129883, -1.0238514113391184, 0.045285701751708984, -0.5698292255401611, 0.3745688199996948, -0.3099750280380249, -0.7731306552886963, -0.9703800678253174, 0.5937430187222392, 0.3680771945297745, -0.18083620071411133, 0.41373562812805176, 0.5638947486877441, -0.8439168930053711, 0.6900634765625, -1.0030436963667184, 0.03317904472351074, 0.8048687532425116, -0.0668940544128418, 0.985891580581665, 0.2359853982925415, -0.31213676929473877, -0.33979185681073915, -0.784429669380188, -0.25759851932525635, 0.8546042442321777, -0.8334155082702637, -0.2729421854019165, 0.6908594369888306, 0.4058568477630615, -0.8336286538093056, 0.9949171543121338, -0.428857684135437, -0.6939042806625366, -0.29206113850218607, -0.8705507516860962, -0.6025011539459229, -0.9882830381393433, 0.9943466186523438, -0.3344658613204956, 0.08052146434783936, 0.39623260498046875, -0.46917916552500544, -0.4469735622406006, 0.9248060352922682, -0.18300795555114746, -0.6376192569732666, 0.49085819721221924}};

    }

    public void caluculateOutput() {
        multiply(inputs, w0, layer1);
        sigmoid(layer1);
        multiply(layer1, w1, outputs);
        sigmoid(outputs);
    }

    private void multiply(double[] in, double[][] weights, double[] out) {
        int m = weights.length;
        int n = weights[0].length;

        if (in.length != n) throw new RuntimeException("Illegal matrix in dimensions.");
        if (out.length != m) throw new RuntimeException("Illegal matrix out dimensions.");

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                out[i] += weights[i][j] * in[j];
            }
        }
    }


    private void sigmoid(double[] array) {
        for(int i=0; i<array.length; i++) {
            array[i] = sigmoid(array[i]);
        }
    }

    private double sigmoid(double x) {
        return (1/( 1 + Math.pow(Math.E,(-1*x))));
    }

    public void setInputs(ArrayList<Cell> cells, Color oppColor) {

        int totalCells = SuperNova.TOTALCELLS;

        for(int i=0; i < totalCells; i++) {
            Coin coin = cells.get(i).getCoin();

            if(coin == null || coin.getColor() == Color.BROWN) {
                inputs[i] = 0;
                inputs[totalCells+i] = -1;
            } else{
                int value = coin.getValue();
                if(coin.getColor() == oppColor) {
                    value = -value;
                }

                inputs[i] = sigmoid(value);
                inputs[totalCells + i] = 1;
            }
        }
    }

    public int getOutput(ArrayList<Cell> cells) {
        double bestScore = Double.MIN_VALUE;
        int bestNode = 0;

        for(int i=0; i<SuperNova.TOTALCELLS; i++) {
            Coin coin = cells.get(i).getCoin();
            if(coin == null) {
                if (outputs[i] > bestScore) {
                    bestScore = outputs[i];
                    bestNode = i;
                }
            }
        }

        return bestNode;
    }

    public void print() {
        System.out.println("w0 = new double[][]" + Arrays.deepToString(w0).replace('[', '{').replace(']', '}') + ";");
        System.out.println("w1 = new double[][]" + Arrays.deepToString(w1).replace('[', '{').replace(']', '}') + ";");
    }

}

class SuperNova {

    public final static boolean DEBUG = true; // Contest: true (doesn't matter a lot)
    public final static boolean PRINTDEBUGTOSTERR = true; // Contest: true
    public final static boolean SINGLEMODE = true; // Contest: true
    public static final String[] BROWNCELLS = {"H1", "F2", "A3", "C4", "D5"}; //Only needed for non single mode
    private static final boolean RANDOM_BROWNCELLS = true; // Only for non single mode

    private static final Strategy STRAT_ONE = Strategy.COMBINE_MAIN;    // Compare mode Red Strat
    private static final Strategy STRAT_TWO = Strategy.COMBINE_TEST;       // Compare mode Blue Strat
    private static final Strategy STRAT_SINGLE = Strategy.COMBINE_MAIN; // Single mode Strat
    private static final int TESTCASES = 35; // Amount of testcases in experimental mode

    private final static boolean TRAIN = false; //true if trian, false if experiment

    public static final int DEFAULTSCORE = 75;
    public static final int TOTALCELLS = 36;
    public static final int BROWNCOINS = 5;
    public static final int ROWS = 8;
    public static final int COINS = 15;

    public final static int TURNS = 15;

    public static void main(String[] args) {
        if(SINGLEMODE) {
            NeuralNetwork neuralNetwork = new NeuralNetwork(new int[]{72, 50, 36});
            neuralNetwork.initializeWeights();
            new GameHandler(STRAT_SINGLE, neuralNetwork).run();
        } else {
            if(TRAIN) {
                trainNN();
            } else {
                NeuralNetwork neuralNetwork = new NeuralNetwork(new int[]{72, 50, 36});
                neuralNetwork.initializeWeights();
                experiment(true, neuralNetwork);
            }
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

    private static int experiment(boolean output, NeuralNetwork neuralNetwork) {
        int blueScore = 0;
        int redScore = 0;
        long blueTime = 0;
        long redTime = 0;

        for(int i=0; i<TESTCASES; i++) {
            String[] brownCells = RANDOM_BROWNCELLS ? getRandomBrownCells() : BROWNCELLS;
            Judge judge = new Judge(STRAT_ONE, STRAT_TWO, brownCells, neuralNetwork);
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

        if(output) {
            System.out.println("Scores: [" + redScore + "|" + blueScore + "]");
            System.out.println("Times: [" + redTime + "|" + blueTime + "]");
        }

        return redScore - blueScore;
    }

    private static void trainNN() {
        int TIMES = 200;
        int POOLSIZE = 500;
        double keepAlive = 0.05;
        int[] layers = new int[]{72, 50, 36};

        NeuralNetwork[] testSet = new NeuralNetwork[POOLSIZE];
        for(int i=0; i<POOLSIZE; i++) {
            testSet[i] = new NeuralNetwork(layers);
            testSet[i].initializeWeights();
        }

        for(int t=0; t<TIMES; t++) {
            System.out.println("Starting iteration "+t+"     ----------------------");
            for (int i = 0; i < POOLSIZE; i++) {
                testSet[i].score = experiment(false, testSet[i]);
            }

            Arrays.sort(testSet, new Comparator<NeuralNetwork>() {
                @Override
                public int compare(NeuralNetwork o1, NeuralNetwork o2) {
                    return -Integer.compare(o1.score, o2.score);
                }
            });

            int keep = (int)(POOLSIZE * keepAlive);

            int mutate = 15;
            double KEEPMUTATION = 0.90;


            Random random = new Random();

            for(int i= 0; i<mutate; i++) {
                NeuralNetwork daddyOne = testSet[i];
                NeuralNetwork daddyTwo = testSet[i+1];
                NeuralNetwork one = testSet[keep + 2*i];
                NeuralNetwork two = testSet[keep + 2*i + 1];


                for(int k=0; k<daddyOne.w0.length; k++) {
                    for(int j=0; j<daddyOne.w0[0].length; j++) {
                        if(random.nextBoolean()) {
                            one.w0[k][j] = daddyOne.w0[k][j];
                            two.w0[k][j] = daddyTwo.w0[k][j];
                        } else {
                            two.w0[k][j] = daddyOne.w0[k][j];
                            one.w0[k][j] = daddyTwo.w0[k][j];
                        }
                    }
                }

                for(int k=0; k<daddyOne.w1.length; k++) {
                    for(int j=0; j<daddyOne.w1[0].length; j++) {
                        if(random.nextBoolean()) {
                            one.w1[k][j] = daddyOne.w1[k][j];
                            two.w1[k][j] = daddyTwo.w1[k][j];
                        } else {
                            two.w1[k][j] = daddyOne.w1[k][j];
                            one.w1[k][j] = daddyTwo.w1[k][j];
                        }
                    }
                }

            }

            for(int i= keep + mutate; i<POOLSIZE; i++) {
                int mod = (int) (i % keep * 1.5);
                if(mod >= keep) {
                    testSet[i].initializeWeights();
                } else {
                    NeuralNetwork daddy = testSet[mod];
                    NeuralNetwork baby = testSet[i];

                    for(int k=0; k<daddy.w0.length; k++) {
                        for(int j=0; j<daddy.w0[0].length; j++) {
                            if(random.nextDouble() < KEEPMUTATION) {
                                baby.w0[k][j] = daddy.w0[k][j];
                            } else if(random.nextBoolean()) {
                                baby.w0[k][j] = (random.nextDouble() * 4 - 2) * daddy.w0[k][j];
                            } else {
                                baby.w0[k][j] = random.nextDouble() * 2 - 1;
                            }
                        }
                    }

                    for(int k=0; k<daddy.w1.length; k++) {
                        for(int j=0; j<daddy.w1[0].length; j++) {
                            if(random.nextDouble() < KEEPMUTATION) {
                                baby.w1[k][j] = daddy.w0[k][j];
                            } else if(random.nextBoolean()) {
                                baby.w1[k][j] = (random.nextDouble() * 4 - 2) * daddy.w1[k][j];
                            } else {
                                baby.w1[k][j] = random.nextDouble() * 2 - 1;
                            }
                        }
                    }
                }
            }

            System.out.println("Score: " +testSet[0].score);
            testSet[0].print();
        }

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

