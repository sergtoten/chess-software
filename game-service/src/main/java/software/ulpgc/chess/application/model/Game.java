package software.ulpgc.chess.application.model;

import software.ulpgc.chess.application.model.board.Color;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record Game(
        String id,
        String roomCode,
        Optional<Player> white,
        Optional<Player> black,
        Position position,
        List<Move> moves,
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

    private static Optional<Player> colorCreator(Color creatorColor, Color color, Player creator) {
        return creatorColor == color ? Optional.of(creator) : Optional.empty();
    }

    private static Optional<Player> colorGuest(Optional<Player> player, Player guest) {
        return player.isEmpty() ? Optional.of(guest) : player;
    }

}
