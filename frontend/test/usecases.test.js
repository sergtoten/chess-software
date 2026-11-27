// Casos de uso del frontend probados con el doble (FakeGameService) en lugar del adaptador HTTP:
// demuestra que el adaptador de comunicación se sustituye sin tocar los casos de uso.
import { test } from "node:test";
import assert from "node:assert/strict";
import { FakeGameService } from "../public/js/adapters/FakeGameService.js";
import { GameSession } from "../public/js/application/GameSession.js";
import { CreateRoom, JoinRoom, MakeMove, RefreshGame, Resign } from "../public/js/application/usecases.js";

const ANA = { id: "p1", name: "" };
const BEA = { id: "p2", name: "" };

// Dos pestañas (dos sesiones) contra el mismo servicio.
async function ongoingGame() {
    const service = new FakeGameService();
    const white = new GameSession(ANA);
    const black = new GameSession(BEA);
    const created = await new CreateRoom(service, white).execute("  Ana ");
    await new JoinRoom(service, black).execute("Bea", created.roomCode.toLowerCase());
    await new RefreshGame(service, white).execute();
    return { service, white, black };
}

test("crear sala deja la partida en espera con el creador y su nombre sin espacios", async () => {
    const session = new GameSession(ANA);
    const game = await new CreateRoom(new FakeGameService(), session).execute("  Ana ");
    assert.equal(game.status, "Waiting");
    assert.equal(session.player.name, "Ana");
    assert.equal(session.myColor(), "white");
    assert.equal(session.isInProgress(), false);
});

test("unirse con el código (en minúsculas) empieza la partida para los dos", async () => {
    const { white, black } = await ongoingGame();
    assert.equal(black.game.status, "Ongoing");
    assert.equal(black.myColor(), "black");
    assert.equal(white.isMyTurn(), true);
    assert.equal(black.isMyTurn(), false);
});

test("unirse a un código inexistente propaga el 404 y no cambia la sesión", async () => {
    const session = new GameSession(BEA);
    await assert.rejects(new JoinRoom(new FakeGameService(), session).execute("Bea", "NOPE22"),
        { status: 404, message: "room not found" });
    assert.equal(session.game, null);
});

test("mover actualiza la partida y pasa el turno; el rival lo ve al refrescar", async () => {
    const { service, white, black } = await ongoingGame();
    await new MakeMove(service, white).execute("e2", "e4");
    assert.deepEqual(white.game.moves, ["e2e4"]);
    assert.equal(white.isMyTurn(), false);

    await new RefreshGame(service, black).execute();
    assert.equal(black.isMyTurn(), true);
    assert.deepEqual(black.game.pieces.e4, { color: "white", type: "pawn" });
});

test("mover fuera de turno propaga el 409", async () => {
    const { service, black } = await ongoingGame();
    await assert.rejects(new MakeMove(service, black).execute("e7", "e5"), { status: 409 });
});

test("needsPromotion solo es cierto para un peón que llega a la última fila", async () => {
    const { service, white } = await ongoingGame();
    const move = new MakeMove(service, white);
    white.game.pieces = { a7: { color: "white", type: "pawn" }, b7: { color: "white", type: "rook" } };
    assert.equal(move.needsPromotion("a7", "a8"), true);
    assert.equal(move.needsPromotion("b7", "b8"), false);
    assert.equal(move.needsPromotion("a7", "a6"), false);
});

test("rendirse termina la partida con victoria del rival", async () => {
    const { service, black } = await ongoingGame();
    const game = await new Resign(service, black).execute();
    assert.equal(game.status, "Resigned");
    assert.equal(game.result, "WhiteWins");
    assert.equal(black.isFinished(), true);
});
