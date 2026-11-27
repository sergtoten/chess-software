// Recuerda, por pestaña (sessionStorage), quién soy y en qué partida estoy.
// Por pestaña y no localStorage: así dos pestañas del mismo navegador son dos jugadores distintos.
// Mientras no haya login, el playerId es un UUID generado aquí (límite conocido: lo envía el cliente).

const PLAYER = "chess.player";
const GAME = "chess.gameId";

export class TabStorage {
    constructor(storage = sessionStorage) { this.storage = storage; }

    loadPlayer() {
        const saved = this.storage.getItem(PLAYER);
        if (saved) return JSON.parse(saved);
        const player = { id: crypto.randomUUID(), name: "" };
        this.savePlayer(player);
        return player;
    }

    savePlayer(player) { this.storage.setItem(PLAYER, JSON.stringify(player)); }

    loadGameId() { return this.storage.getItem(GAME); }

    saveGameId(gameId) {
        if (gameId) this.storage.setItem(GAME, gameId);
        else this.storage.removeItem(GAME);
    }
}
