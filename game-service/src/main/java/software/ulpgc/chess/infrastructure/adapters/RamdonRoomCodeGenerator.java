package software.ulpgc.chess.infrastructure.adapters;

import software.ulpgc.chess.application.ports.CodeGenerator;

import java.util.concurrent.ThreadLocalRandom;

public class RamdonRoomCodeGenerator implements CodeGenerator {

    static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    static final int LENGTH = 6;
    @Override
    public String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < LENGTH; i++) {
            code.append(ALPHABET.charAt(ThreadLocalRandom.current().nextInt(ALPHABET.length())));
        }
        return code.toString();
    }
}
