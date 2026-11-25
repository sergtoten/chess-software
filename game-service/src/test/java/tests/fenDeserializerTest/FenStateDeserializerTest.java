package tests.fenDeserializerTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import software.ulpgc.chess.application.model.position.CastlingRight;
import software.ulpgc.chess.application.model.board.Color;
import software.ulpgc.chess.application.model.board.Square;
import software.ulpgc.chess.application.model.position.State;
import software.ulpgc.chess.infrastructure.adapters.deserializer.FenStateDeserializer;

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

class FenStateDeserializerTest {

    private FenStateDeserializer deserializer;

    @BeforeEach
    void setUp() {
        deserializer = new FenStateDeserializer();
    }

    @Test
    @DisplayName("Debe deserializar el estado inicial")
    void shouldDeserializeInitialState() {
        State state = deserializer.deserialize(
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        );

        assertThat(state.turn()).isEqualTo(Color.White);
        assertThat(state.castlings()).containsExactlyInAnyOrder(
                CastlingRight.WhiteShort,
                CastlingRight.WhiteLong,
                CastlingRight.BlackShort,
                CastlingRight.BlackLong
        );
        assertThat(state.onPassant()).isEmpty();
        assertThat(state.halfMoveClock()).isZero();
        assertThat(state.fullMoveNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("Debe reconocer el turno de las negras")
    void shouldDeserializeBlackTurn() {
        State state = deserializer.deserialize("8/8/8/8/8/8/8/8 b - - 0 1");

        assertThat(state.turn()).isEqualTo(Color.Black);
    }

    @Test
    @DisplayName("Debe deserializar los derechos de enroque")
    void shouldDeserializeCastlingRights() {
        State state = deserializer.deserialize("8/8/8/8/8/8/8/8 w Kq - 0 1");

        assertThat(state.castlings()).containsExactlyInAnyOrder(
                CastlingRight.WhiteShort,
                CastlingRight.BlackLong
        );
    }

    @Test
    @DisplayName("No debe haber derechos de enroque cuando aparece un guion")
    void shouldDeserializeNoCastlingRights() {
        State state = deserializer.deserialize("8/8/8/8/8/8/8/8 w - - 0 1");

        assertThat(state.castlings()).isEmpty();
    }

    @Test
    @DisplayName("Debe deserializar la casilla de captura al paso")
    void shouldDeserializeEnPassantSquare() {
        State state = deserializer.deserialize("8/8/8/8/8/8/8/8 b - e3 0 1");

        assertThat(state.onPassant()).isEqualTo(
                Optional.of(Square.at("e3"))
        );
    }

    @Test
    @DisplayName("Debe representar la ausencia de captura al paso")
    void shouldDeserializeNoEnPassantSquare() {
        State state = deserializer.deserialize("8/8/8/8/8/8/8/8 w - - 0 1");

        assertThat(state.onPassant()).isEmpty();
    }

    @Test
    @DisplayName("Debe deserializar el contador de medias jugadas")
    void shouldDeserializeHalfMoveClock() {
        State state = deserializer.deserialize("8/8/8/8/8/8/8/8 w - - 17 1");

        assertThat(state.halfMoveClock()).isEqualTo(17);
    }

    @Test
    @DisplayName("Debe deserializar el número de jugada completa")
    void shouldDeserializeFullMoveNumber() {
        State state = deserializer.deserialize("8/8/8/8/8/8/8/8 b - - 0 24");

        assertThat(state.fullMoveNumber()).isEqualTo(24);
    }

    @Test
    @DisplayName("Debe aceptar espacios adicionales entre los campos")
    void shouldAcceptExtraSpacesBetweenFields() {
        State state = deserializer.deserialize("8/8/8/8/8/8/8/8   w   -   -   0   1");

        assertThat(state.turn()).isEqualTo(Color.White);
        assertThat(state.castlings()).isEmpty();
        assertThat(state.onPassant()).isEmpty();
        assertThat(state.halfMoveClock()).isZero();
        assertThat(state.fullMoveNumber()).isEqualTo(1);
    }
}