import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

class SuperNova {

    public final static boolean DEBUG = false; // Contest: true (doesn't matter a lot)
    public final static boolean PRINTDEBUGTOSTERR = true; // Contest: true
    public final static boolean SINGLEMODE = false; // Contest: true
    public static final String[] BROWNCELLS = {"H1", "F2", "A3", "C4", "D5"}; //Only needed for non single mode
    private static final boolean RANDOM_BROWNCELLS = true; // Only for non single mode

    private static final Strategy STRAT_ONE = Strategy.COMBINE_MAIN;    // Compare mode Red Strat
    private static final Strategy STRAT_TWO = Strategy.COMBINE_TEST;       // Compare mode Blue Strat
    private static final Strategy STRAT_SINGLE = Strategy.COMBINE_TEST; // Single mode Strat
    private static final int TESTCASES = 35; // Amount of testcases in experimental mode

    private final static boolean TRAIN = true; //true if trian, false if experiment

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

            Arrays.sort(testSet, (o1, o2) -> -Integer.compare(o1.score, o2.score));

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
