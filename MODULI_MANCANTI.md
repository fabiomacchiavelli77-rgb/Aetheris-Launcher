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

## 2. Nuovi Moduli Proposti (Ispirati a Meteor / Wurst)

### Combat (4)
- **CrystalAura**: Gestione automatica del piazzamento ed esplosione degli End Crystal per PvP.
- **BedAura**: Posizionamento ed esplosione automatica dei letti nelle dimensioni del Nether/End.
- **AimAssist**: Assistenza nel puntamento del mirino sulle entità vicine.
- **SelfTrap**: Posizionamento rapido di blocchi protettivi attorno e sopra la testa del giocatore.

### Render (2)
- **StorageESP**: Evidenzia ceste, shulker box e bauli con box colorati attraverso le pareti.
- **ItemESP**: Evidenzia gli oggetti rilasciati a terra visibili a distanza.

### World (2)
- **LiquidInteract**: Consente di posizionare blocchi direttamente sopra superfici di acqua o lava.
- **AutoSign**: Compila automaticamente il testo sui cartelli piazzati con una frase preimpostata.

### Player (2)
- **AutoEat**: Consuma automaticamente cibo dall'inventario quando il livello di fame scende.
- **AntiAFK**: Esegue movimenti periodici per prevenire la disconnessione per inattività (kick AFK).
