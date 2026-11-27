import { BoardView } from "./BoardView.js";
import { CreateRoom, JoinRoom, MakeMove, RefreshGame, Resign } from "../application/usecases.js";

const POLL_MS = 1500;
const COLOR_NAMES = { white: "blancas", black: "negras" };

// Mensajes del backend (en inglés, ver docs del contrato) traducidos para el usuario.
const ERROR_TEXTS = {
    "missing or malformed playerId or playerName": "Nombre no válido (máximo 30 caracteres).",
    "missing or malformed roomCode": "El código de sala no es válido.",
    "room not found": "No existe ninguna sala con ese código.",
    "game not found": "La partida ya no existe.",
    "game is not waiting for a player": "La sala ya está llena.",
    "player is already in this game": "Ya estás en esta sala.",
    "game is not in progress": "La partida no está en curso.",
    "not a player of this game": "No juegas en esta partida.",
    "not your turn": "No es tu turno.",
    "illegal move": "Movimiento ilegal.",
};

/**
 * Adaptador de entrada: traduce clics y formularios a casos de uso y pinta el resultado.
 * Solo habla con el puerto a través de los casos de uso; nunca hace fetch.
 */
export class App {
    /**
     * @param {import("../ports/GameService.js").GameService} service
     * @param {import("../application/GameSession.js").GameSession} session
     * @param {import("../adapters/TabStorage.js").TabStorage} storage
     */
    constructor(service, session, storage) {
        this.session = session;
        this.storage = storage;
        this.createRoom = new CreateRoom(service, session);
        this.joinRoom = new JoinRoom(service, session);
        this.refreshGame = new RefreshGame(service, session);
        this.makeMove = new MakeMove(service, session);
        this.resign = new Resign(service, session);
        this.selected = null;
        this.pollTimer = null;

        this.$ = id => document.getElementById(id);
        this.board = new BoardView(this.$("board"), square => this.#onSquareClick(square));
    }

    async start() {
        this.$("name").value = this.session.player.name;
        this.$("create").addEventListener("click", () => this.#enterWith(() => this.createRoom.execute(this.#name())));
        this.$("join").addEventListener("click", () => this.#enterWith(() => this.joinRoom.execute(this.#name(), this.#code())));
        this.$("resign").addEventListener("click", () => this.#onResign());
        this.$("leave").addEventListener("click", () => this.#leave());

        const gameId = this.storage.loadGameId();   // al recargar la pestaña, vuelve a su partida
        if (gameId) await this.#enterWith(() => this.refreshGame.execute(gameId));
        else this.#showLobby();
    }

    // --- lobby ---

    #name() {
        const name = this.$("name").value.trim();
        if (!name) throw new Error("Escribe tu nombre");
        return name;
    }

    #code() {
        const code = this.$("code").value.trim();
        if (!code) throw new Error("Escribe el código de la sala");
        return code;
    }

    async #enterWith(action) {
        try {
            await action();
        } catch (error) {
            this.storage.saveGameId(null);
            this.#showLobby();
            return this.#showError(error);
        }
        this.storage.savePlayer(this.session.player);
        this.storage.saveGameId(this.session.game.id);
        this.#showError(null);
        this.#showGame();
    }

    #showLobby() {
        this.#stopPolling();
        this.$("lobby").hidden = false;
        this.$("game").hidden = true;
    }

    // --- partida ---

    #showGame() {
        this.$("lobby").hidden = true;
        this.$("game").hidden = false;
        this.selected = null;
        this.#render();
        this.#startPolling();
    }

    #leave() {
        this.storage.saveGameId(null);
        this.session.game = null;
        this.#showError(null);
        this.#showLobby();
    }

    async #onSquareClick(square) {
        if (!this.session.isMyTurn()) return;
        const piece = this.session.game.pieces[square];
        const mine = piece?.color === this.session.myColor();

        if (!this.selected || mine) {            // seleccionar (o cambiar de pieza)
            this.selected = mine ? square : null;
            return this.#render();
        }
        const from = this.selected;
        this.selected = null;
        if (from === square) return this.#render();

        let promotion;
        if (this.makeMove.needsPromotion(from, square)) {
            promotion = await this.#askPromotion();
            if (!promotion) return this.#render();
        }
        await this.#run(() => this.makeMove.execute(from, square, promotion));
    }

    async #onResign() {
        if (!confirm("¿Seguro que quieres rendirte?")) return;
        await this.#run(() => this.resign.execute());
    }

    #askPromotion() {
        const dialog = this.$("promotion");
        dialog.returnValue = "";
        dialog.showModal();
        return new Promise(resolve =>
            dialog.addEventListener("close", () => resolve(dialog.returnValue || null), { once: true }));
    }

    async #run(action) {
        try {
            await action();
            this.#showError(null);
        } catch (error) {
            this.#showError(error);
        }
        this.#render();
    }

    // --- polling: el rival mueve en otra pestaña/equipo; preguntamos cada POLL_MS ---

    #startPolling() {
        this.#stopPolling();
        const tick = async () => {
            if (this.session.isFinished()) return;
            try {
                const before = JSON.stringify(this.session.game);
                await this.refreshGame.execute();
                if (JSON.stringify(this.session.game) !== before) this.#render();
            } catch (error) {
                this.#showError(error);
            }
            this.pollTimer = setTimeout(tick, POLL_MS);
        };
        this.pollTimer = setTimeout(tick, POLL_MS);
    }

    #stopPolling() {
        clearTimeout(this.pollTimer);
        this.pollTimer = null;
    }

    // --- pintar ---

    #render() {
        const { game } = this.session;
        const me = this.session.myColor();
        this.board.render(game, me, this.selected);
        this.$("room-code").textContent = game.roomCode;
        this.$("white-name").textContent = game.white?.name ?? "—";
        this.$("black-name").textContent = game.black?.name ?? "—";
        this.$("my-color").textContent = me ? "Juegas con " + COLOR_NAMES[me] : "Espectador";
        this.$("status").textContent = this.#statusText();
        this.$("resign").disabled = !this.session.isInProgress() || !me;
        this.#renderMoves(game.moves);
    }

    #statusText() {
        const { game } = this.session;
        switch (game.status) {
            case "Waiting": return "Esperando rival… comparte el código de la sala.";
            case "Checkmate": return "Jaque mate. " + this.#resultText();
            case "Stalemate": return "Tablas por ahogado.";
            case "Resigned": return "Rendición. " + this.#resultText();
        }
        const check = game.status === "Check" ? "¡Jaque! " : "";
        return check + (this.session.isMyTurn() ? "Tu turno." : "Turno del rival.");
    }

    #resultText() {
        const result = this.session.game.result;
        if (result === "Draw") return "Tablas.";
        const winner = result === "WhiteWins" ? "white" : "black";
        const you = this.session.myColor() === winner ? " ¡Has ganado!" : " Has perdido.";
        return "Ganan " + COLOR_NAMES[winner] + "." + you;
    }

    #renderMoves(moves) {
        const list = this.$("moves");
        list.replaceChildren();
        for (let i = 0; i < moves.length; i += 2) {
            const item = document.createElement("li");
            item.textContent = moves[i] + (moves[i + 1] ? "  " + moves[i + 1] : "");
            list.append(item);
        }
    }

    #showError(error) {
        this.$("error").textContent = error ? (ERROR_TEXTS[error.message] ?? error.message) : "";
    }
}
