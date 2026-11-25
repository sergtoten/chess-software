package software.ulpgc.chess.application.model.game;

import software.ulpgc.chess.application.model.board.Color;
import software.ulpgc.chess.application.model.board.Piece;
import software.ulpgc.chess.application.model.position.Move;
import software.ulpgc.chess.application.model.position.Position;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record Game(
        String id,
        String roomCode,
        Optional<Player> white,
        Optional<Player> black,
        Position position,
        List<PlayedMove> moves,
        GameStatus status,
        Optional<GameResult> result,
        Instant createdAt,
        Instant lastActivity
) {
    public Game {
        moves = List.copyOf(moves);
    }

    public static Game start(String  id, String roomCode, Player creator, Color creatorColor, Instant now) {
        Optional<Player> white = colorCreator(creatorColor, Color.White, creator);
        Optional<Player> black = colorCreator(creatorColor, Color.Black, creator);
        return new Game(id, roomCode, white, black, Position.initial(), List.of(), GameStatus.Waiting, Optional.empty(), now, now);
    }

    public Game join(Player guest, Instant now) {
        Optional<Player> white = colorGuest(white(), guest);
        Optional<Player> black = colorGuest(black(), guest);
        return new Game(id(), roomCode(), white, black, position(), moves(), GameStatus.Ongoing, result(), createdAt(), now);
    }

    public Game withMove(Move move, Position nextPosition, GameStatus newStatus, Instant now, Optional<Piece> promotion) {
        List<PlayedMove> newMoves = new ArrayList<>(moves);
        newMoves.add(new PlayedMove(move, promotion));
        Optional<GameResult> newResult = resultOf(newStatus, nextPosition.turn());
        return new Game(id(), roomCode(), white(), black(), nextPosition, newMoves, newStatus, newResult, createdAt(), now);
    }

    public Game resign(Color loser, Instant now) {
        Optional<GameResult> finalResult = resultOf(GameStatus.Resigned, loser);
        return new Game(id(), roomCode(), white(), black(), position(), moves(), GameStatus.Resigned, finalResult, createdAt(), now);
    }

    public Optional<Color> colorOf(String playerId) {
        if (sits(white, playerId)) return Optional.of(Color.White);
        if (sits(black, playerId)) return Optional.of(Color.Black);
        return Optional.empty();
    }

    private static boolean sits(Optional<Player> seat, String playerId) {
        return seat.map(player -> player.id().equals(playerId)).orElse(false);
    }

    private static Optional<GameResult> resultOf(GameStatus status, Color loser) {
        return switch (status) {
            case Resigned, Checkmate -> Optional.of(loser == Color.White ? GameResult.BlackWins : GameResult.WhiteWins);
            case Stalemate -> Optional.of(GameResult.Draw);
            default -> Optional.empty();
        };
    }

    private static Optional<Player> colorCreator(Color creatorColor, Color color, Player creator) {
        return creatorColor == color ? Optional.of(creator) : Optional.empty();
    }

    private static Optional<Player> colorGuest(Optional<Player> player, Player guest) {
        return player.isEmpty() ? Optional.of(guest) : player;
    }

}
