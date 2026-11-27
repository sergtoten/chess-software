// Adaptador HTTP probado con un fetch falso: comprueba la petición que construye
// (ruta, método, parámetros) y la traducción del JSON/FEN al modelo del frontend.
import { test } from "node:test";
import assert from "node:assert/strict";
import { HttpGameService } from "../js/adapters/HttpGameService.js";

const INITIAL = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
const PLAYER = { id: "p1", name: "Ana" };

function fakeFetch(status, body) {
    const calls = [];
    const fn = async (url, init) => {
        calls.push({ url, method: init.method, params: Object.fromEntries(init.body ?? []) });
        return { ok: status < 400, status, json: async () => body };
    };
    return { fn, calls };
}

test("createGame hace POST / con el jugador y traduce la partida (Gson omite los null)", async () => {
    const { fn, calls } = fakeFetch(200, {
        id: "g1", roomCode: "ABCDEF", black: { id: "p1", name: "Ana" },
        position: INITIAL, moves: [], status: "Waiting",
    });
    const game = await new HttpGameService("http://api", fn).createGame(PLAYER);

    assert.deepEqual(calls, [{ url: "http://api/", method: "POST", params: { playerId: "p1", playerName: "Ana" } }]);
    assert.equal(game.white, null);
    assert.equal(game.result, null);
    assert.equal(game.turn, "white");
    assert.equal(Object.keys(game.pieces).length, 32);
    assert.deepEqual(game.pieces.e1, { color: "white", type: "king" });
    assert.deepEqual(game.pieces.d8, { color: "black", type: "queen" });
    assert.equal(game.pieces.e4, undefined);
});

test("makeMove manda move y promotion; getGame va por query string", async () => {
    const body = { id: "g1", roomCode: "ABCDEF", position: "4k3/P7/8/8/8/8/8/4K3 b - - 0 1", moves: ["a7a8q"], status: "Ongoing" };
    const { fn, calls } = fakeFetch(200, body);
    const service = new HttpGameService("http://api", fn);

    const game = await service.makeMove(PLAYER, "g1", "a7a8", "Queen");
    await service.getGame("g1");

    assert.deepEqual(calls[0].params, { id: "g1", playerId: "p1", playerName: "Ana", move: "a7a8", promotion: "Queen" });
    assert.equal(calls[0].method, "PUT");
    assert.equal(calls[1].url, "http://api/?id=g1");
    assert.equal(calls[1].method, "GET");
    assert.equal(game.turn, "black");
    assert.deepEqual(game.pieces.a7, { color: "white", type: "pawn" });
});

test("un error HTTP se convierte en GameServiceError con el estado y el mensaje del backend", async () => {
    const { fn } = fakeFetch(422, { error: "illegal move" });
    await assert.rejects(new HttpGameService("http://api", fn).makeMove(PLAYER, "g1", "e2e5"),
        { name: "Error", status: 422, message: "illegal move" });
});

test("sin conexión se convierte en GameServiceError con estado 0", async () => {
    const offline = async () => { throw new TypeError("fetch failed"); };
    await assert.rejects(new HttpGameService("http://api", offline).getGame("g1"), { status: 0 });
});
