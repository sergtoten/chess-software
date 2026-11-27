// Casos de uso de interacción. Cada uno recibe el puerto (GameService) y la sesión,
// llama al backend a través del puerto y deja la partida actualizada en la sesión.
// No validan las reglas del ajedrez: eso lo hace el backend (si falla, el puerto lanza
// GameServiceError y la UI enseña el mensaje).

export class CreateRoom {
    constructor(service, session) { this.service = service; this.session = session; }

    /** @param {string} name */
    async execute(name) {
        this.session.player = { ...this.session.player, name: name.trim() };
        this.session.game = await this.service.createGame(this.session.player);
        return this.session.game;
    }
}

export class JoinRoom {
    constructor(service, session) { this.service = service; this.session = session; }

    /** @param {string} name @param {string} roomCode */
    async execute(name, roomCode) {
        this.session.player = { ...this.session.player, name: name.trim() };
        this.session.game = await this.service.joinGame(this.session.player, roomCode.trim().toUpperCase());
        return this.session.game;
    }
}

export class RefreshGame {
    constructor(service, session) { this.service = service; this.session = session; }

    /** @param {string} [gameId] por defecto, la partida actual */
    async execute(gameId = this.session.game.id) {
        this.session.game = await this.service.getGame(gameId);
        return this.session.game;
    }
}

export class MakeMove {
    constructor(service, session) { this.service = service; this.session = session; }

    /** ¿Este movimiento es un peón que llega a la última fila? (la UI pregunta entonces la pieza) */
    needsPromotion(from, to) {
        const piece = this.session.game.pieces[from];
        return piece?.type === "pawn" && (to[1] === "8" || to[1] === "1");
    }

    /** @param {string} from "e2" @param {string} to "e4" @param {string} [promotion] "Queen"... */
    async execute(from, to, promotion) {
        const { player, game } = this.session;
        this.session.game = await this.service.makeMove(player, game.id, from + to, promotion);
        return this.session.game;
    }
}

export class Resign {
    constructor(service, session) { this.service = service; this.session = session; }

    async execute() {
        this.session.game = await this.service.resign(this.session.player, this.session.game.id);
        return this.session.game;
    }
}
