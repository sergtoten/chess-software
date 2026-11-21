package software.ulpgc.chess.infrastructure.adapters;

import software.ulpgc.chess.application.model.board.Color;
import software.ulpgc.chess.application.ports.ColorPicker;

import java.util.concurrent.ThreadLocalRandom;

public class RandomColorPicker implements ColorPicker {
    @Override
    public Color pick() {
        Color[] colors = Color.values();
        return colors[ThreadLocalRandom.current().nextInt(colors.length)];
    }
}
