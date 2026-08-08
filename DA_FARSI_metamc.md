# To-Do: Scansione plugin/permessi su metamc.it

**Data:** oggi
**Target:** `metamc.it`
**Obiettivo:** enumerare plugin + plugin permessi senza avere permessi sul server.

---

## 1. Moduli Aetheris già pronti (niente da scrivere)

| Modulo | Path | Cosa fa |
|---|---|---|
| `PluginScanner` | `modules/impl/world/PluginScanner.java` | Enumerazione plugin via `/plugins`, rileva PEX / LuckPerms / GroupManager / Essentials / Vault. Auto-probe `/pex`, `/pex user <nome> perm list`, `/lp`, `/lp user <nome> info`, `/manucheckp`. |
| `InstalledPlugins` | `modules/impl/world/InstalledPlugins.java` | Legge il **command dispatcher** del server (completamenti `/comando:`) → trova plugin anche se `/plugins` è disabilitato. |
| `PacketLogger` | `modules/impl/world/PacketLogger.java` | Log in/out dei pacchetti (vedi 3). |
| `AntiDetect` | `modules/impl/player/AntiDetect.java` + `mixins/AntiDetectMixin.java` | Spoofa brand → `vanilla`, nasconde payload `fabric:*` in `Connection.send`. |
| `NoChatReports` | `modules/impl/player/NoChatReports.java` + `mixins/ChatSignatureMixin.java` | Rimuove le firme dai messaggi chat. |

## 2. Run di controllo (in ordine)

1. **Compila** ClientCore: `cd ClientCore && ./gradlew build`
2. Avvia con **AntiDetect ON** + **NoChatReports ON** (maschera base).
3. Connettiti a `metamc.it`.
4. **Attiva `PluginScanner`** (Category WORLD):
   - leggere output in chat → lista plugin trovati;
   - se trova PEX/LuckPerms/GroupManager → nota i comandi di probe che stampa;
   - `autoProbe` ON se vuoi che provi i comandi in autonomia.
5. Se `/plugins` è disabilitato → **attiva `InstalledPlugins`** come fallback (usa i completamenti `/xz:`).
6. **Manualmente prova** solo comandi di *lettura informazioni*, NON modifiche private:
   - `/pex user <tuonome> perm list`
   - `/lp user <tuonome> info`
   - `/manwhois <tuonome>`
7. Se vuoi mappare gli "anti-cheat/server list" del server: **attiva `PacketLogger`**, unisciti , poi scarica il log e cerca `plugins`, `permission`, brand string.
8. **Spegni tutti i moduli** prima di uscire (AntiDetect su brand → il server vede `vanilla`).

## 3. Esiti attesi

- **Lista plugin** via `/plugins` → immediato.
- **Plugin nascosti** → solo via `InstalledPlugins` + PacketLogger.
- **Permessi tuoi attuali** → dipende dai permessi del gruppo guest: `*` o plugin checks.

## 4. Stato anti-detect — HONEST

AntiDetect NON rende il client "invisibile". Rende:

- ✅ brand `vanilla` (non `fabric`);
- ✅ nessun payload `fabric:*`;
- ✅ chat senza firma reportabile.

Non nasconde:
- ⚠️ `ClientBrandRetriever` dal lato PLAYER (alcuni plugin anti-cheat lo leggono);
- ⚠️ comandi sospetti (/pex /lp) — non tolgono i permessi, i comandi fanno solo check di lettura o falliscono pulito;
- ⚠️ movimenti/tempo di reazione di un client hack — altro anti-cheat li vede.

## 5. Prossimi passi (dopo il test)

- [ ] Registrare risultati del test in `Aetheris_Checklist_Test.xlsx` (modulo PluginScanner).
- [ ] Se il server usa anti-cheat descritto → risposta su risegno module per evitare flag.
- [ ] Se `/plugins` è bloccato → verificare che `InstalledPlugins` compili/DEGLI eccezioni (il codice ha `try/catch` ma un metodo da confermare).

## Rischio e regole

- Solo **lettura** di info pubbliche/halve-pubbliche del server. Nessun exploit, nessun cambio permessi.
- Non usare probe che tentano di *escalare* privilegi (es. `/pex user <altri>`).
- Se il server risponde con `Unknown command` per tutto → lascia perdere: probabile plugin di permessi ben configurato, di niente da fare senza privilegi.