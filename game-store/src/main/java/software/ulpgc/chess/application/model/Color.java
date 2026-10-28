package software.ulpgc.chess.application.model;

public enum Color {

    White('w'), Black('w');

    private final char symbol;

    Color(char symbol) {
        this.symbol = symbol;
    }

    public char symbol() {
        return this.symbol;
    }

}
