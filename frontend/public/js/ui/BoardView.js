// Pinta el tablero y avisa de los clics. No sabe nada de HTTP ni de casos de uso:
// recibe la partida ya traducida y una función a la que llamar con la casilla pulsada.

const GLYPHS = { king: "♚", queen: "♛", rook: "♜", bishop: "♝", knight: "♞", pawn: "♟" };
const TEXT_STYLE = "︎"; // evita que algunos sistemas pinten ♟ como emoji

export class BoardView {
    /** @param {HTMLElement} element @param {(square: string) => void} onSquareClick */
    constructor(element, onSquareClick) {
        this.element = element;
        element.addEventListener("click", event => {
            const square = event.target.closest("[data-square]");
            if (square) onSquareClick(square.dataset.square);
        });
    }

    /**
     * @param {import("../ports/GameService.js").Game} game
     * @param {'white'|'black'|null} orientation  color que queda abajo
     * @param {string|null} selected  casilla seleccionada
     */
    render(game, orientation, selected) {
        const files = orientation === "black" ? "hgfedcba" : "abcdefgh";
        const ranks = orientation === "black" ? "12345678" : "87654321";
        const last = game.moves.at(-1);
        const highlighted = last ? [last.slice(0, 2), last.slice(2, 4)] : [];

        this.element.replaceChildren();
        for (const rank of ranks) {
            for (const file of files) {
                const name = file + rank;
                const cell = document.createElement("div");
                cell.dataset.square = name;
                cell.className = "square " + (isLight(file, rank) ? "light" : "dark");
                if (highlighted.includes(name)) cell.classList.add("last-move");
                if (name === selected) cell.classList.add("selected");

                const piece = game.pieces[name];
                if (piece) {
                    const glyph = document.createElement("span");
                    glyph.className = "piece " + piece.color;
                    glyph.textContent = GLYPHS[piece.type] + TEXT_STYLE;
                    cell.append(glyph);
                }
                if (file === files[0]) cell.append(label("rank", rank));
                if (rank === ranks[7]) cell.append(label("file", file));
                this.element.append(cell);
            }
        }
    }
}

function isLight(file, rank) {
    return ("abcdefgh".indexOf(file) + Number(rank)) % 2 === 0; // a1 oscura, h1 clara
}

function label(kind, text) {
    const span = document.createElement("span");
    span.className = "coord " + kind;
    span.textContent = text;
    return span;
}
