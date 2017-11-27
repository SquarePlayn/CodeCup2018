import java.util.Scanner;

class Main {

    public final static boolean debug = true;

    public final static int TURNS = 15;

    public static Color ourColor;
    public static Color oppColor;

    public static Scanner scanner = new Scanner(System.in);

    public static Board board;

    public static void main(String[] args) {

        board = new Board();
        board.buildBoard();
        mainLoop();
    }

    private static void mainLoop() {
        //First line of input
        String nextLine = scanner.nextLine();
        if(nextLine.equals("Start")) {
            //We are red
            ourColor = Color.RED;
            oppColor = Color.BLUE;
        } else {
            ourColor = Color.BLUE;
            oppColor = Color.RED;
            computeInput(nextLine, oppColor);
        }

        for(int i=0; i<TURNS; i++) {
            System.out.println(computeOutput(i));
            computeInput(scanner.nextLine(), oppColor);
        }
        debug("[ERROR] We are after the mainloop, we are not supposed to be here!");
    }

    private static void computeInput(String input, Color player) {
        if(input.equals("Quit")) {
            endGame();
            return;
        }

        Cell cell = board.getCell(input.substring(0, 2));
        int value = Integer.parseInt(input.substring(3, input.length()));

        if(cell.getCoin() == null) {
            debug("Wanted to set a coin that was already set");
            endGame();
        } else if(value == 0 || value > 15) {
            debug("Invalid value");
            endGame();
        } else {

            //Set the coin
            cell.setCoin(board.getCoins(player)[value]);
            board.getCoins(player)[value].setSpot(cell);

        }
    }

    private static String computeOutput(int turn) {


        String output = "A4=5";
        //TODO Get output

        //Actually set the info (and check if it is correct)
        computeInput(output, ourColor);

        return "TODO";
    }

    private static void endGame() {
        debug("Game exiting");
        System.exit(0);
        return;
    }

    public static void debug(String message) {
        if(debug) {
            System.out.println("[D] "+message);
        }
    }
}
