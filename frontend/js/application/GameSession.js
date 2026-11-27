/**
 * Estado de la pestaña: quién soy y la última versión de la partida que conozco.
 * Lo comparten los casos de uso; la UI solo lo lee para pintar.
 */
export class GameSession {
    /** @param {import("../ports/GameService.js").Player} player */
    constructor(player) {
        this.player = player;
        /** @type {import("../ports/GameService.js").Game | null} */
        this.game = null;
    }

    /** @returns {'white'|'black'|null} mi color en la partida actual */
    myColor() {
        if (!this.game) return null;
        if (this.game.white?.id === this.player.id) return "white";
        if (this.game.black?.id === this.player.id) return "black";
        return null;
    }

    isInProgress() {
        return this.game?.status === "Ongoing" || this.game?.status === "Check";
    }

    isFinished() {
        return ["Checkmate", "Stalemate", "Resigned"].includes(this.game?.status);
    }

    isMyTurn() {
        return this.isInProgress() && this.game.turn === this.myColor();
    }
}
