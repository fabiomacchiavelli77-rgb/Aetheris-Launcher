# Moduli Aetheris Client — Lista Moduli da Testare / Implementare

Questo documento contiene l'elenco completo dei moduli di **Aetheris Client** inclusi nel file `Aetheris_Checklist_Test.xlsx` che risultano **non ancora testati** o contrassegnati per estensioni future.

---

## 1. Moduli Presenti (Da Testare)

### Render (4)
- **NoHurtCam**: Rimuove l'effetto visivo di scossa/tremore della telecamera quando si subisce danno.
- **NameTags**: Mostra i nomi, la distanza e la barra della salute sopra i giocatori attraverso i blocchi.
- **Tracers**: Disegna linee 3D sullo schermo dirette verso le entità/giocatori nelle vicinanze.
- **FreeCam**: Permette di staccare la telecamera dal corpo del giocatore per esplorare l'area circostante.

### World (5)
- **FastBreak**: Aumenta la velocità di distruzione dei blocchi.
- **Scaffold**: Piazza automaticamente blocchi sotto i piedi del giocatore durante il movimento.
- **Timer**: Modifica la velocità dei tick locali di gioco.
- **AutoTool**: Seleziona automaticamente dalla hotbar lo strumento adatto al blocco in fase di scavo.
- **InstalledPlugins**: Identifica i plugin del server tramite l'autocompletamento dei comandi.

### Player (6)
- **AutoRespawn**: Esegue automaticamente il respawn alla morte del personaggio.
- **FastPlace**: Elimina il ritardo nel piazzamento dei blocchi.
- **NoHunger**: Riduce la fame simulando stati di camminata.
- **ChestStealer**: Preleva automaticamente gli oggetti dalle ceste aperte.
- **AutoFish**: Automatizza il lancio e il recupero della canna da pesca.
- **InventoryCleaner**: Rimuove gli oggetti considerati spazzatura dall'inventario.

---

## 2. ✅ Implementati (ex proposti — Batch 2025-08)

### Combat (4) ✅
- **CrystalAura**: Attacca automaticamente gli End Crystal vicini per esploderli. `modules/impl/combat/CrystalAura.java` — rotation + attack, cooldown sync opzionale, CPS configurabile. (Solo explode, no placement).
- **BedAura**: Click destro automatico sui letti vicini (Nether/End) per esploderli. `modules/impl/combat/BedAura.java` — usa mano vuota per evitare placement accidentali, scan area ±range.
- **AimAssist**: Rotazione graduale del mirino verso le entità nel FOV. `modules/impl/combat/AimAssist.java` — settings range/speed/fov/targetPlayers.
- **SelfTrap**: Piazzamento rapido di blocchi attorno e sopra la testa. `modules/impl/combat/SelfTrap.java` — 6 offset (4 lati y+1, testa y+1, sopra y+2), auto-disable a trappola completa.

### Render (2) ✅
- **StorageESP**: Box colorati sulle storage (chest=oro, shulker=viola, ender chest=rosso scuro) attraverso i muri. `modules/impl/render/StorageESP.java` + `mixins/StorageESPMixin.java` (hook su `LevelRenderer.renderLevel` TAIL, disegno con `RenderType.lines()`).
- **ItemESP**: Glow sugli oggetti a terra (sistema glow vanilla via `MinecraftClientMixin.shouldEntityAppearGlowing`). `modules/impl/render/ItemESP.java` — range configurabile.

### World (2) ✅
- **LiquidInteract**: Piazzamento blocchi sopra acqua/lava. `modules/impl/world/LiquidInteract.java` + `mixins/BlockItemMixin.java` (override `BlockItem.canPlace`).
- **AutoSign**: Compila e conferma automaticamente i cartelli piazzati. `modules/impl/world/AutoSign.java` + `mixins/AutoSignMixin.java` (Accessor su `AbstractSignEditScreen` — scrive `messages[]` + `text`, chiama `onDone()`). **Righe hardcoded in `AutoSign.LINES`** (default "Aetheris Client").

### Player (2) ✅
- **AutoEat**: Consuma automaticamente cibo dalla hotbar quando la fame scende sotto soglia. `modules/impl/player/AutoEat.java` — detect cibo via `DataComponents.FOOD`, settings threshold/eatInWater.
- **AntiAFK**: Rotazione periodica + swing per evitare il kick AFK. `modules/impl/player/AntiAFK.java` — invia `ServerboundMovePlayerPacket.Rot` per sincronizzare col server.

### Note tecniche Batch
- Registrati in `ModuleManager.init()`: Combat 12, Render 9, World 8, Movement 10, Player 8 (totale 47).
- Moduli più recenti (Batch 2026-08): `BunnyJump`, `Jetpack`, `Sneak`, `AutoFarm` (con auto-replant).
- Build verificata: `cd ClientCore && ./gradlew build` ✅

---

## 3. Nuovi Moduli Proposti (Futuri)

### Combat
- ~~CrystalAura~~ ✅ → estensione possibile: **auto-placement** dei crystal (calcolo posizione sopra blocchi di ossidiana).
- ~~BedAura~~ ✅ → estensione possibile: **BedTrap** (piazza letto + esplode in loop).

### Render
- ~~StorageESP~~ ✅ / ~~ItemESP~~ ✅ → estensione possibile: **ChestStealer ESP** (evidenzia le chest già svuotate).
- **Trajectories**: Predice la traiettoria degli oggetti lanciati (richiede calcolo fisica — draw code già possibile con il pattern StorageESP).

### World
- ~~LiquidInteract~~ ✅ / ~~AutoSign~~ ✅
- **AirPlace**: Piazzamento blocchi in aria senza blocco adiacente (richiede packet spoof posizione — pattern Criticals PACKET).

### Player
- ~~AutoEat~~ ✅ / ~~AntiAFK~~ ✅
- **InventorySort**: Ordina automaticamente l'inventario (pattern ChestStealer `handleInventoryMouseClick`).

### Altri (ispirati Meteor/Wurst)
- **AmbientSound** / **CameraClip**: Rimozione clipping camera nei blocchi.
- **TimerRange** / **SpeedHack**: Raffinamenti Speed/Timer esistenti.
- **ClickGUI**: Tema premium già presente — possibile aggiunta **HUD Editor**.
