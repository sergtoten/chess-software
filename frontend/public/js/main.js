// Punto de composición (equivalente a Main.init del backend): elige los adaptadores y los conecta.
// Es el único sitio que decide si se usa el backend real o el doble en memoria.
import { API_URL } from "./config.js";
import { HttpGameService } from "./adapters/HttpGameService.js";
import { FakeGameService } from "./adapters/FakeGameService.js";
import { TabStorage } from "./adapters/TabStorage.js";
import { GameSession } from "./application/GameSession.js";
import { App } from "./ui/App.js";

const useFake = new URLSearchParams(location.search).has("fake");   // index.html?fake
const service = useFake ? new FakeGameService() : new HttpGameService(API_URL);
const storage = new TabStorage();
const session = new GameSession(storage.loadPlayer());

new App(service, session, storage).start();
