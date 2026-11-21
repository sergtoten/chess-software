package software.ulpgc.chess.application.model.board;

public enum Color {

    White('w'), Black('b');

    private final char symbol;

    Color(char symbol) {
        this.symbol = symbol;
    }

    public char symbol() {
        return this.symbol;
    }

    public Color oppositeColor() {
        return this == White ? Black : White;
    }

}
