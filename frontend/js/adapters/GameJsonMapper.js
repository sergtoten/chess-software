// Traduce el JSON de game-service al modelo del frontend (typedef Game de ports/GameService.js).
// Los símbolos FEN solo se conocen aquí, igual que en el backend viven solo en sus adaptadores.

const FEN_PIECES = {
    k: "king", q: "queen", r: "rook", b: "bishop", n: "knight", p: "pawn",
};
const FILES = "abcdefgh";

/** @returns {import("../ports/GameService.js").Game} */
export function toGame(json) {
    const [placement, turn] = json.position.split(" ");
    return {
        id: json.id,
        roomCode: json.roomCode,
        white: json.white ?? null,       // Gson omite las claves null
        black: json.black ?? null,
        pieces: piecesOf(placement),
        turn: turn === "w" ? "white" : "black",
        moves: json.moves ?? [],
        status: json.status,
        result: json.result ?? null,
    };
}

// "rnbqkbnr/pppppppp/8/..." -> { a8: {color:'black', type:'rook'}, ... }
function piecesOf(placement) {
    const pieces = {};
    placement.split("/").forEach((row, index) => {
        const rank = 8 - index;
        let file = 0;
        for (const symbol of row) {
            if (/\d/.test(symbol)) {
                file += Number(symbol);
                continue;
            }
            pieces[FILES[file] + rank] = {
                color: symbol === symbol.toUpperCase() ? "white" : "black",
                type: FEN_PIECES[symbol.toLowerCase()],
            };
            file++;
        }
    });
    return pieces;
}
