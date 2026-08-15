# Aetheris Tools

## `aetheris_server_audit.py` — audit difensivo del TUO server

Strumento per **amministratori di server Minecraft**: analizza localmente i file di
configurazione di un server di tua proprietà e segnala errori comuni e permessi pericolosi.

**Non è uno scanner remoto**: non si connette a server di terzi, non invia pacchetti,
non modifica nulla. Legge solo i file della cartella che gli indichi.

### Uso

```bash
# Sulla macchina del server (richiede solo Python 3.8+):
python tools/aetheris_server_audit.py /path/alla/cartella/server

# Con report salvati:
python tools/aetheris_server_audit.py . --json report.json --md report.md
```

Exit code `1` = trovate vulnerabilità CRITICHE/ALTE (utile in script/CI).

### Test rapido

```bash
python tools/aetheris_server_audit.py tools/test_server_fixture --no-color
# Atteso: 15 rilievi (CRITICAL: 4, HIGH: 2, MEDIUM: 4, ...) e exit code 1
```

### Cosa controlla

| Area | Rilievi |
|------|---------|
| `server.properties` | `online-mode=false` (cracked), RCON con password debole, whitelist off, query, spawn-protection, function-permission-level |
| `ops.json` | OP livello 4 (controllo totale), troppi operatori |
| `whitelist.json` | whitelist attiva ma vuota |
| LuckPerms | permessi wildcard/pericolosi (`*`, `luckperms.*`, `minecraft.command.op`, ...) su gruppi — severity maggiorata se sul gruppo `default`; **ereditarietà**: gruppi figli di un parent con permessi pericolosi; storage H2/MySQL segnalato come non ispezionabile da file |
| PermissionsEx | wildcard e permessi pericolosi in `permissions.yml`, attenzione particolare al gruppo `default: true` |
| `plugins/*.jar` | presenza permission manager, jar senza versione nel nome |
| `bukkit.yml` / Paper | `query.plugins=true`, RCON in paper-global |

### Permessi considerati pericolosi

Wildcard totali (`*`, `minecraft.*`, `bukkit.*`, `luckperms.*`), comandi di gestione
OP (`minecraft.command.op`, `bukkit.command.op`), `luckperms.editor`, wildcard plugin
amministrativi (`essentials.*`, `worldedit.*`). La lista è in `DANGEROUS_PERMISSIONS`
ed è estendibile.

### Limiti

- Euristico: non sostituisce un audit manuale completo.
- LuckPerms con storage H2/MySQL non è leggibile da file (usa `/lp export`).
- I gruppi con ereditarietà: controlla anche i parent con `/lp group <g> info`.
