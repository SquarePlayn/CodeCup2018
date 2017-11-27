import java.util.ArrayList;

class Board {

    int TOTALCELLS = 36;
    int BROWNCOINS = 5;
    int ROWS = 8;
    int COINS = 15;

    private ArrayList<Cell> allCells = new ArrayList<>();
    private ArrayList<ArrayList<Cell>> cells = new ArrayList<>();
    private Coin[] redCoins = new Coin[COINS+1];
    private Coin[] blueCoins = new Coin[COINS+1];
    private Coin[] brownCoins = new Coin[BROWNCOINS];

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
        for(int i=1; i<=COINS; i++) {
            redCoins[i] = new Coin(Color.RED, i);
            blueCoins[i] = new Coin(Color.BLUE, i);
        }

        for(int i=0; i<BROWNCOINS; i++) {
            String spot = Main.scanner.nextLine();
            brownCoins[i] = new Coin(Color.BROWN, 0);
            brownCoins[i].setSpot(getCell(spot));
            getCell(spot).setCoin(brownCoins[i]);
        }
    }

    private void buildCells() {

        for(int i=0; i<ROWS; i++) {
            ArrayList<Cell> newRow = new ArrayList<>();
            for(int j=0; j<ROWS-i; j++) {
                Cell newCell = new Cell(i, j);
                newRow.add(newCell);
                allCells.add(newCell);
            }
            cells.add(newRow);
        }
    }

    private void buildConnections() {
        for(int i=0; i<ROWS; i++) {
            for(int j=0; j<ROWS-i; j++) {
                Cell cell = getCell(i, j);

                //Up
                if(i>0) {
                    cell.adj.add(getCell(i-1, j));
                    cell.adj.add(getCell(i-1, j+1));
                }

                //Left
                if(j>0) {
                    cell.adj.add(getCell(i, j-1));
                }

                //Right
                if(j<ROWS-i-1){
                    cell.adj.add(getCell(i, j+1));
                }

                //Down
                if(i<ROWS-1){
                    //Left down
                    if(j>0) {
                        cell.adj.add(getCell(i+1, j-1));
                    }

                    //Right down
                    if(j<ROWS-i-1) {
                        cell.adj.add(getCell(i+1, j));
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

    public ArrayList<Cell> getEmptyCells() {
        ArrayList<Cell> emptyCells = new ArrayList<>();
        for(Cell cell: allCells) {
            if(cell.getCoin() == null) {
                emptyCells.add(cell);
            }
        }
        return emptyCells;
    }

}
