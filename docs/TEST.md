# Test Plan — AristoSeedCrack v1.1.0

## Versioni Minecraft Supportate

| Versione Minecraft | Aristois Installer | SeedCrackerX | Slime Chunk | Anti-Detection |
|--------------------|:--:|:--:|:--:|:--:|
| **1.21.x** (latest) | ✅ | ✅ | ✅ | ✅ |
| **1.20.x** | ✅ | ✅ | ✅ | ✅ |
| **1.19.x** | ✅ | ✅ | ✅ | ✅ |
| **1.18.x** | ✅ | ✅ | ✅ | ✅ |
| **1.17.x** | ✅ | ✅ | ✅ | ✅ |
| **1.16.x** | ✅ | ✅ | ✅ | ✅ |
| **1.15.x** | ✅ | ✅ | ✅ | ✅ |
| **1.14.x** | ✅ | ✅ | ✅ | ✅ |
| **1.13.x** | ✅ | ❌ | ❌ | ✅ |
| **1.12.x** | ✅ | ❌ | ❌ | ✅ |
| **1.8–1.11** | ✅ | ❌ | ❌ | ✅ |

> **Nota:** SeedCrackerX richiede Fabric → solo Minecraft 1.14+. Slime Chunk Cracking usa meccaniche stabili dalla 1.14. Anti-Detection funziona su tutte le versioni (patcha solo metadata JAR).<br>
> **Aristois mod** funziona su tutte le versioni (1.8+). L'installer recupera la lista versioni in tempo reale da Mojang PistonMeta.

### Versioni prioritarie da testare
- [ ] **1.21.4** — ultima stabile (test principale)
- [ ] **1.20.6** — stabile precedente
- [ ] **1.16.5** — versione "legacy" più usata (Forge test)
- [ ] **1.14.4** — limite inferiore SeedCrackerX

## Setup test

- [ ] **Ambiente pulito** — rimuovere cartella `.minecraft/versions/*-Aristois` e `.minecraft/mods/Aristois*` prima di ogni test
- [ ] **Java 8+** verificato con `java -version`

---

## 1. Installer — Avvio

| # | Test | Atteso |
|---|------|--------|
| 1.1 | `java -jar Aristois-Donor.jar` | Finestra installer si apre, logo visibile |
| 1.2 | Doppio click su `AristoSeedCrack.bat` | Stesso di 1.1, console mostra messaggio avvio |
| 1.3 | `./AristoSeedCrack.sh` (Linux/macOS) | Stesso di 1.1 |
| 1.4 | Java non installato → `AristoSeedCrack.bat` | Messaggio errore chiaro, non crasha |

## 2. Installer — UI SetupScene

| # | Test | Atteso |
|---|------|--------|
| 2.1 | Avvio installer → schermata setup | Dropdown versione MC popolato, launcher selezionabile |
| 2.2 | Checkbox `Clean install` | Si può spuntare/deselezionare |
| 2.3 | Checkbox `Forge` deselezionato | Checkbox `Install SeedCrackerX` visibile e cliccabile |
| 2.4 | Checkbox `Forge` selezionato | Checkbox `Install SeedCrackerX` nascosta O disabilitata |
| 2.5 | Deselezionare `Forge` | Checkbox `Install SeedCrackerX` torna visibile |
| 2.6 | Checkbox `Anti-detection patch` | Sempre visibile, indipendente da Forge |
| 2.7 | "Next" senza selezionare nulla | L'installer procede (solo Aristois base) |

## 3. Installer — Installazione Vanilla + SeedCrackerX

| # | Test | Atteso |
|---|------|--------|
| 3.1 | Spuntare `Install SeedCrackerX`, installare | Cartella `mods/` contiene `Aristois-Seed-Cracker.jar` |
| 3.2 | Avviare Minecraft con profilo Aristois | Gioco si avvia senza crash |
| 3.3 | In gioco: `/seedcracker gui` | GUI si apre, tab visibili |
| 3.4 | Tab "Slime Chunks" presente | Visibile nella GUI |
| 3.5 | `/seedcracker data` | Mostra dati raccolti (vuoti all'inizio) |
| 3.6 | `/seedcracker render` | Abilita rendering cuboid |
| 3.7 | `/seedcracker version` | Mostra versione mod |

## 4. Seed Cracker — Slime Chunk Cracking

| # | Test | Atteso |
|---|------|--------|
| 4.1 | Vai in bioma palude, notte | Slime spawnano |
| 4.2 | Slime spawna naturalmente | Cuboid **blu** appare sul chunk |
| 4.3 | Contatore slime chunk in GUI aumenta | `/seedcracker gui` → tab Slime Chunks → count > 0 |
| 4.4 | Raccogli 15-18 slime chunk | Cracking parte automaticamente |
| 4.5 | Seed trovato nella GUI | `/seedcracker data` mostra seed craccato |
| 4.6 | Vai sottoterra Y<40 (non palude) | Slime spawn registrati anche qui |
| 4.7 | Cuboid blu ≠ cuboid verdi | Blu = slime, Verde = struttura. Non si confondono |

## 5. Seed Cracker — Strutture (metodo classico)

| # | Test | Atteso |
|---|------|--------|
| 5.1 | Trova un tempio del deserto | Cuboid **verde** appare |
| 5.2 | Trova un villaggio/avamposto | Cuboid verde appare |
| 5.3 | `/seedcracker data` | Bit count aumenta per ogni struttura |
| 5.4 | Raggiungi 32+40 bit | Cracking parte automaticamente |

## 6. Anti-Detection

| # | Test | Atteso |
|---|------|--------|
| 6.1 | Spuntare `Anti-detection patch`, installare vanilla | Installazione completata senza errori |
| 6.2 | Aprire `{versione}-Aristois.jar` con 7-Zip/WinRAR | `fabric.mod.json` presente |
| 6.3 | Controllare `id` in `fabric.mod.json` | Ha suffisso random (es. `aristois_a3f8k1`, NON `aristois`) |
| 6.4 | `name` in `fabric.mod.json` | Contiene "(Optimization)" |
| 6.5 | Avviare Minecraft | Aristois si carica normalmente, nessun errore |
| 6.6 | Funzionalità mod intatte | XRay, ESP, FullBright ecc. funzionano |
| 6.7 | Anti-detection con Forge | `EMC.jar` e `Aristois.jar` entrambi patchati |

## 7. Donor Mode

| # | Test | Atteso |
|---|------|--------|
| 7.1 | Avviare Minecraft con Aristois installato | Nessuna richiesta login |
| 7.2 | Aprire menu mod (tasto destro shift) | Tutte le feature disponibili |
| 7.3 | XRay | Funziona senza limitazioni |
| 7.4 | FullBright | Funziona senza limitazioni |
| 7.5 | ESP / Tracers | Funzionano senza limitazioni |
| 7.6 | AutoTool / AutoArmor | Funzionano senza limitazioni |
| 7.7 | Baritone (se installato) | Funziona senza limitazioni |
| 7.8 | Nessuna richiesta chiave/licenza | Mai, in nessun punto |

## 8. Compatibilità

| # | Test | Atteso |
|---|------|--------|
| 8.1 | Vanilla launcher, ultima versione MC | Installazione OK |
| 8.2 | MultiMC / Prism | Rilevamento automatico cartella, installazione OK |
| 8.3 | Clean install (cancella precedente) | Vecchia versione rimossa, nuova installata |
| 8.4 | Forge senza SeedCrackerX | Nessun warning, installazione OK |
| 8.5 | Rete assente durante installazione | Aristois installato, SeedCrackerX saltato con warning |

## 9. Edge Cases

| # | Test | Atteso |
|---|------|--------|
| 9.1 | Chiudere installer a metà | Nessun file corrotto lasciato |
| 9.2 | Rieseguire installer stessa versione | Sovrascrive pulito, non duplica |
| 9.3 | Percorso con spazi nel nome | Funziona (es. `C:\Minecraft Launcher\.minecraft`) |
| 9.4 | Percorso con caratteri speciali | Funziona o errore chiaro, non crasha |
| 9.5 | JAR di sola lettura nella cartella mods | Anti-detection salta con warning, non crasha |
| 9.6 | Versione MC senza supporto SeedCrackerX | Warning nel log, installazione continua |

## 10. Log e Debug

| # | Test | Atteso |
|---|------|--------|
| 10.1 | Dopo installazione, aprire `installer.log` | Contiene: versione MC, opzioni, path, esito |
| 10.2 | Installazione con SeedCrackerX | Log mostra "Installing SeedCrackerX" |
| 10.3 | Installazione con Anti-detection | Log mostra "Applying anti-detection patches" e JAR patchati |
| 10.4 | Errore forzato (rete assente) | Log mostra errore ma non blocca |

---

## Note

- **Ordine test:** eseguire in sequenza 1→10. I test dentro ogni sezione sono indipendenti.
- **Server test:** i test 4.x e 5.x funzionano anche su server multiplayer. Testarli singleplayer + server.
- **Ripetizione:** eseguire test 3.x con almeno 2 versioni Minecraft diverse (es. 1.21.4 e 1.20.6).

## Esito

| Data | Tester | Esito | Note |
|------|--------|-------|------|
|      |        | ⬜ Pass / ⬜ Fail |      |
|      |        | ⬜ Pass / ⬜ Fail |      |
|      |        | ⬜ Pass / ⬜ Fail |      |
