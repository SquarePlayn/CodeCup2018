import java.util.ArrayList;

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
