import { GameService, GameServiceError } from "../ports/GameService.js";

const PROMOTION_LETTERS = { Queen: "q", Rook: "r", Bishop: "b", Knight: "n" };

/**
 * Doble de prueba del puerto: guarda las partidas en memoria y no usa la red.
 * Sirve para los tests de los casos de uso y para ver la UI sin backend
 * (abrir index.html?fake). Imita los códigos de error del backend, pero NO
 * valida las reglas del ajedrez: mueve la pieza a donde se le diga.
 * El creador juega siempre con blancas para que los tests sean deterministas.
 */
export class FakeGameService extends GameService {
    #games = new Map();
    #nextId = 1;

    async createGame(player) {
        const id = "game-" + this.#nextId++;
        const game = {
            id, roomCode: "ROOM" + id.slice(5).padStart(2, "0"),
            white: player, black: null,
            pieces: initialPieces(), turn: "white",
            moves: [], status: "Waiting", result: null,
        };
        this.#games.set(id, game);
        return structuredClone(game);
    }

    async joinGame(player, roomCode) {
        const game = [...this.#games.values()].find(g => g.roomCode === roomCode);
        if (!game) throw new GameServiceError(404, "room not found");
        if (game.status !== "Waiting") throw new GameServiceError(409, "game is not waiting for a player");
        if (game.white.id === player.id) throw new GameServiceError(409, "player is already in this game");
        game.black = player;
        game.status = "Ongoing";
        return structuredClone(game);
    }

    async getGame(gameId) {
        return structuredClone(this.#find(gameId));
    }

    async makeMove(player, gameId, move, promotion) {
        const game = this.#find(gameId);
        if (!isInProgress(game)) throw new GameServiceError(409, "game is not in progress");
        const color = colorOf(game, player);
        if (!color) throw new GameServiceError(403, "not a player of this game");
        if (color !== game.turn) throw new GameServiceError(409, "not your turn");
        const from = move.slice(0, 2), to = move.slice(2, 4);
        const piece = game.pieces[from];
        if (!piece || piece.color !== color) throw new GameServiceError(422, "illegal move");
        delete game.pieces[from];
        game.pieces[to] = promotion ? { color, type: promotion.toLowerCase() } : piece;
        game.moves.push(move + (promotion ? PROMOTION_LETTERS[promotion] : ""));
        game.turn = color === "white" ? "black" : "white";
        return structuredClone(game);
    }

    async resign(player, gameId) {
        const game = this.#find(gameId);
        if (!isInProgress(game)) throw new GameServiceError(409, "game is not in progress");
        const color = colorOf(game, player);
        if (!color) throw new GameServiceError(403, "not a player of this game");
        game.status = "Resigned";
        game.result = color === "white" ? "BlackWins" : "WhiteWins";
        return structuredClone(game);
    }

    #find(gameId) {
        const game = this.#games.get(gameId);
        if (!game) throw new GameServiceError(404, "game not found");
        return game;
    }
}

function isInProgress(game) {
    return game.status === "Ongoing" || game.status === "Check";
}

function colorOf(game, player) {
    if (game.white?.id === player.id) return "white";
    if (game.black?.id === player.id) return "black";
    return null;
}

function initialPieces() {
    const back = ["rook", "knight", "bishop", "queen", "king", "bishop", "knight", "rook"];
    const pieces = {};
    [..."abcdefgh"].forEach((file, i) => {
        pieces[file + "1"] = { color: "white", type: back[i] };
        pieces[file + "2"] = { color: "white", type: "pawn" };
        pieces[file + "7"] = { color: "black", type: "pawn" };
        pieces[file + "8"] = { color: "black", type: back[i] };
    });
    return pieces;
}
