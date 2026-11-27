import { GameService, GameServiceError } from "../ports/GameService.js";
import { toGame } from "./GameJsonMapper.js";

/**
 * Adaptador real del puerto: habla con game-service por HTTP.
 * Es el único sitio del frontend que conoce rutas, métodos y parámetros.
 *
 * Contrato usado (los parámetros van como formulario, application/x-www-form-urlencoded):
 *   POST /        playerId, playerName              -> crear sala
 *   PUT  /join    playerId, playerName, roomCode    -> unirse
 *   GET  /?id=    id                                -> consultar partida
 *   PUT  /        id, playerId, playerName, move[, promotion] -> mover
 *   PUT  /resign  id, playerId, playerName          -> rendirse
 * Errores: {"error": "..."} con 400, 403, 404, 409 o 422.
 */
export class HttpGameService extends GameService {
    /**
     * @param {string} baseUrl  p. ej. "http://localhost:8080"
     * @param {typeof fetch} [fetchFn]  se inyecta en los tests para no hacer red
     */
    constructor(baseUrl, fetchFn = (...args) => fetch(...args)) {
        super();
        this.baseUrl = baseUrl;
        this.fetch = fetchFn;
    }

    createGame(player) {
        return this.#send("POST", "/", playerParams(player));
    }

    joinGame(player, roomCode) {
        return this.#send("PUT", "/join", { ...playerParams(player), roomCode });
    }

    getGame(gameId) {
        return this.#send("GET", "/?" + new URLSearchParams({ id: gameId }));
    }

    makeMove(player, gameId, move, promotion) {
        const params = { id: gameId, ...playerParams(player), move };
        if (promotion) params.promotion = promotion;
        return this.#send("PUT", "/", params);
    }

    resign(player, gameId) {
        return this.#send("PUT", "/resign", { id: gameId, ...playerParams(player) });
    }

    async #send(method, path, params) {
        let response;
        try {
            response = await this.fetch(this.baseUrl + path, {
                method,
                body: params ? new URLSearchParams(params) : undefined,
            });
        } catch {
            throw new GameServiceError(0, "No se puede conectar con el servidor");
        }
        const body = await response.json().catch(() => ({}));
        if (!response.ok) throw new GameServiceError(response.status, body.error ?? "Error " + response.status);
        return toGame(body);
    }
}

function playerParams(player) {
    return { playerId: player.id, playerName: player.name };
}
