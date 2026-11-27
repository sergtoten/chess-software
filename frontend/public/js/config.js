// Dirección de game-service. Ni la UI ni los casos de uso la conocen: solo HttpGameService.
//  - Con Docker: nginx sirve la web y reenvía /api/... a game-service (mismo origen, sin CORS).
//  - En desarrollo sin Docker (python -m http.server 5500): se llama directo a game-service,
//    arrancado con CORS_ORIGIN=http://localhost:5500.
export const API_URL = location.port === "5500" ? "http://localhost:8080" : "/api";
