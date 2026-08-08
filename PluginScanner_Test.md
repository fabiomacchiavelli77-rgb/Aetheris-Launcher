# PluginScanner — Guida al Test

Modulo `PluginScanner` (categoria WORLD) — rileva i plugin di un server Minecraft
**senza usare `/plugins`**. Tre tecniche attive, nessuna esegue comandi dannosi.

## Come testare

1. Compila e avvia: `cd ClientCore && ./gradlew runClient`
2. Apri il menu (Shift o Pausa → Aetheris Menu)
3. Attiva **PluginScanner** (categoria WORLD)
4. Entra in qualsiasi server (meglio uno con plugin: Paper/Spigot con Essentials, LuckPerms, ecc.)
5. Leggi i messaggi in chat col prefisso `[PluginScanner]`

## Le 3 tecniche di rilevamento

### 1. Tab-probe (la principale) — niente `/plugins`
- Il modulo invia pacchetti di tab-completion (`ServerboundCommandSuggestionPacket`)
  con prefissi: `""`, `a`...`z`, `0`...`9`, uno ogni 4 tick.
- Il server risponde con `ClientboundCommandSuggestionsPacket`: la lista di TUTTI
  i comandi registrati, inclusi quelli dei plugin.
- I comandi `namespace:comando` (es. `essentials:fly`) rivelano il plugin dal namespace.
- I comandi root non-vanilla (es. `lp`, `heal`) sono alias di plugin.
- I comandi vanilla (una blacklist interna ~90 comandi) vengono filtrati.
- **Non esegue nessun comando**: il server vede solo richieste di completamento,
  identiche a premere Tab nella chat.

### 2. Sniffing del brand server (`minecraft:brand`)
- Alla connessione il server invia il proprio software: `Paper`, `Purpur`, `Spigot`,
  `Folia`, `Fabric`, `vanilla`...
- `Paper`/`Spigot`/`Purpur`/`Folia` = server a plugin → i plugin sono quasi certi.

### 3. Sniffing dei plugin channel (`minecraft:register`)
- Il server registra i canali dei plugin (es. `bungeecord:main`, canali EssentialsX).
- Intercettato nel mixin `DiscardedPayloadMixin` (il buffer viene letto e ripristinato).
- `bungeecord` = proxy BungeeCord attivo.

## Report in chat

| Messaggio | Significato |
|---|---|
| `[PluginScanner] Server software: Paper` | Software server rilevato |
| `[PluginScanner] Tab-probe avviato...` | Enumerazione comandi in corso |
| `[PluginScanner] Comandi rilevati: lp, heal, ...` | Comandi non-vanilla trovati |
| `[PluginScanner] Plugin permessi: luckperms!` | Plugin permessi identificato |
| `[PluginScanner] Probe: /lp user ...` | Comandi di probing suggeriti |

## Plugin di permessi riconosciuti

PEX (permissionsex), LuckPerms (lp), GroupManager, PermissionsBukkit, Vault,
Essentials/EssentialsX, zPermissions, bPermissions.

Match per nome esatto, contenuto o prefisso breve (`lp` → LuckPerms, `pex` → PEX).

## Fallback automatici

- `/plugins` risponde → parse lista classica
- `/plugins` nascosto (unknown command) → tab-probe automatico
- Nessuna risposta in 5 secondi → tab-probe automatico

## Note anti-cheat

- Nessun comando viene eseguito: solo packet di completamento (come premere Tab).
- Un query ogni 4 tick: niente flood.
- Il rilevamento è passivo (channel/brand) + tab-completion (identico a uso normale).
