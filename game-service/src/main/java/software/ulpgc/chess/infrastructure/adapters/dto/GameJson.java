package software.ulpgc.chess.infrastructure.adapters.dto;
import java.util.List;

public record GameJson(
        String id,
        String roomCode,
        PlayerJson white,
        PlayerJson black,
        String position,
        List<String> moves,
        String status,
        String result,
        String createdAt,
        String lastActivity
        ) {
}
