import java.util.ArrayList;

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
