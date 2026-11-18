package software.ulpgc.chess.application.model;

public enum CastlingRight {

    WhiteShort('K'), WhiteLong('Q'), BlackShort('k'), BlackLong('q');

    private final char symbol;

    CastlingRight(char symbol) {
        this.symbol = symbol;
    }

    public char symbol() {
        return this.symbol;
    }
}
