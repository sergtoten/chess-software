package software.ulpgc.chess.application.usecases;

import software.ulpgc.chess.application.UseCase;
import software.ulpgc.chess.application.model.Player;

import java.util.Optional;

final class PlayerReader {

     static final String ID = "playerId";
     static final String NAME = "playerName";

     static Optional<Player> from(UseCase.Input input) {
         String name = input.get(NAME);
         String id = input.get(ID);
         return name == null || name.isBlank() || id == null || id.isBlank() ? Optional.empty() : Optional.of(new Player(id, name));
     }

}
