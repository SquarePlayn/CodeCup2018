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
