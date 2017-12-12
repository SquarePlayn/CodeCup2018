import java.util.ArrayList;
import java.util.Scanner;

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
