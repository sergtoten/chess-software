/**
 * Puerto de servicio: lo que la aplicación necesita del backend.
 * La UI y los casos de uso solo conocen este contrato; no saben si detrás
 * hay HTTP (HttpGameService) o un doble en memoria (FakeGameService).
 *
 * JavaScript no tiene interfaces: esta clase hace de "interfaz" y los
 * adaptadores la extienden y sobrescriben todos sus métodos.
 *
 * @typedef {{ id: string, name: string }} Player
 * @typedef {'white' | 'black'} Color
 * @typedef {{ color: Color, type: 'king'|'queen'|'rook'|'bishop'|'knight'|'pawn' }} Piece
 * @typedef {'Queen'|'Rook'|'Bishop'|'Knight'} PromotionKind
 * @typedef {{
 *   id: string,
 *   roomCode: string,
 *   white: Player | null,
 *   black: Player | null,
 *   pieces: Object<string, Piece>,   // casilla ("e4") -> pieza; las vacías no aparecen
 *   turn: Color,
 *   moves: string[],                 // historial en UCI: "e2e4", "e7e8q"
 *   status: 'Waiting'|'Ongoing'|'Check'|'Checkmate'|'Stalemate'|'Resigned',
 *   result: 'WhiteWins'|'BlackWins'|'Draw'|null
 * }} Game
 */
export class GameService {
    /** @param {Player} player @returns {Promise<Game>} */
    createGame(player) { throw new Error("not implemented"); }

    /** @param {Player} player @param {string} roomCode @returns {Promise<Game>} */
    joinGame(player, roomCode) { throw new Error("not implemented"); }

    /** @param {string} gameId @returns {Promise<Game>} */
    getGame(gameId) { throw new Error("not implemented"); }

    /**
     * @param {Player} player @param {string} gameId
     * @param {string} move  "e2e4"
     * @param {PromotionKind} [promotion]
     * @returns {Promise<Game>}
     */
    makeMove(player, gameId, move, promotion) { throw new Error("not implemented"); }

    /** @param {Player} player @param {string} gameId @returns {Promise<Game>} */
    resign(player, gameId) { throw new Error("not implemented"); }
}

/** Error que devuelven los adaptadores: código HTTP (0 = sin conexión) y mensaje. */
export class GameServiceError extends Error {
    /** @param {number} status @param {string} message */
    constructor(status, message) {
        super(message);
        this.status = status;
    }
}
