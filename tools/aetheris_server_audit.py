#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Aetheris Server Audit — strumento difensivo per amministratori di server Minecraft.

Analizza UNA CARTELLA DI UN SERVER CHE POSSIEDI/GESTISCI e segnala:
  - server.properties  (online-mode, RCON, whitelist, query...)
  - ops.json           (operatori e livelli)
  - LuckPerms          (permessi pericolosi su gruppi, wildcard, default group)
  - PermissionsEx      (permissions.yml: wildcard, default group)
  - plugins/*.jar      (inventario plugin, plugin di permessi rilevati)
  - bukkit/spigot/paper config (esposizione plugin via query)

NON si connette a server di terzi, NON invia pacchetti, NON modifica nulla:
è un analizzatore locale di file, pensato per verificare la propria configurazione.

Uso (sulla macchina del server, con accesso ai file):
    python aetheris_server_audit.py /path/to/server
    python aetheris_server_audit.py . --json report.json --md report.md

Exit code: 0 = ok/solo note, 1 = trovate vulnerabilità CRITICHE/ALTE.
"""

import argparse
import json
import re
import sys
from pathlib import Path

# --------------------------------------------------------------------------
# Severity helpers
# --------------------------------------------------------------------------

SEVERITY_ORDER = {"CRITICAL": 0, "HIGH": 1, "MEDIUM": 2, "LOW": 3, "INFO": 4}


class Audit:
    def __init__(self):
        self.findings = []

    def add(self, severity, category, file, detail, recommendation):
        self.findings.append({
            "severity": severity,
            "category": category,
            "file": file,
            "detail": detail,
            "recommendation": recommendation,
        })

    @property
    def worst(self):
        if not self.findings:
            return None
        return min(self.findings, key=lambda f: SEVERITY_ORDER[f["severity"]])["severity"]

    def sorted(self):
        return sorted(self.findings, key=lambda f: (SEVERITY_ORDER[f["severity"]], f["category"], f["file"]))


# --------------------------------------------------------------------------
# server.properties
# --------------------------------------------------------------------------

def parse_properties(path: Path):
    props = {}
    for line in path.read_text(encoding="utf-8", errors="replace").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        k, v = line.split("=", 1)
        props[k.strip()] = v.strip()
    return props


def audit_server_properties(root: Path, audit: Audit):
    p = root / "server.properties"
    if not p.is_file():
        audit.add("INFO", "core", "server.properties", "File non trovato", "Assicurati di puntare alla cartella del server.")
        return
    props = parse_properties(p)

    if props.get("online-mode", "true").lower() == "false":
        audit.add("CRITICAL", "core", "server.properties",
                  "online-mode=false: chiunque può entrare con il nome di un altro giocatore (cracked).",
                  "Imposta online-mode=true se non hai un proxy di autenticazione (velocity/bungee con forwarding).")

    rcon_enabled = props.get("enable-rcon", "false").lower() == "true"
    rcon_pass = props.get("rcon.password", "")
    if rcon_enabled and (not rcon_pass or rcon_pass in ("password", "changeme", "123456", "admin")):
        audit.add("CRITICAL", "core", "server.properties",
                  f"RCON attivo con password assente/debole ('{rcon_pass}').",
                  "Password lunga e casuale, oppure disattiva enable-rcon; non esporre la porta.")
    elif rcon_enabled:
        audit.add("MEDIUM", "core", "server.properties",
                  "RCON attivo (console remota).",
                  "Verifica che la porta rcon-port non sia raggiungibile da internet.")

    if props.get("white-list", "false").lower() != "true":
        audit.add("MEDIUM", "core", "server.properties",
                  "Whitelist disattivata: chiunque può entrare.",
                  "Per server privati imposta white-list=true e popola whitelist.json.")

    if props.get("enable-query", "false").lower() == "true":
        audit.add("LOW", "core", "server.properties",
                  "query attiva (GS4/UDP): espone info del server.",
                  "Disattiva enable-query se non ti serve; su Bukkit controlla anche query.plugins.")

    if props.get("spawn-protection", "16").isdigit() and int(props.get("spawn-protection", "16")) == 0:
        audit.add("LOW", "core", "server.properties",
                  "spawn-protection=0.",
                  "Valuta un valore >0 per proteggere l'area spawn da grief.")

    spl = props.get("op-permission-level", "4")
    if spl.isdigit() and int(spl) >= 4:
        audit.add("INFO", "core", "server.properties",
                  "op-permission-level=4: gli op hanno accesso completo ai comandi (inclusi /op e /stop).",
                  "Ok per server piccoli; riduci a 2-3 se dai op a moderatori.")

    fpl = props.get("function-permission-level", "2")
    if fpl.isdigit() and int(fpl) >= 3:
        audit.add("MEDIUM", "core", "server.properties",
                  f"function-permission-level={fpl}: le datapack function girano con permessi alti.",
                  "Tieni il livello a 2 a meno che tu non ti fidi di ogni datapack installato.")


# --------------------------------------------------------------------------
# ops.json / whitelist.json
# --------------------------------------------------------------------------

def audit_ops(root: Path, audit: Audit):
    p = root / "ops.json"
    if not p.is_file():
        return
    try:
        ops = json.loads(p.read_text(encoding="utf-8"))
    except Exception as e:
        audit.add("LOW", "core", "ops.json", f"JSON non valido: {e}", "Correggi il file o rigeneralo.")
        return
    if not isinstance(ops, list):
        return
    if len(ops) == 0:
        audit.add("INFO", "core", "ops.json", "Nessun operatore.", "Ok.")
        return
    for op in ops:
        name = op.get("name", op.get("uuid", "?"))
        level = op.get("level", 0)
        if level >= 4:
            audit.add("MEDIUM", "core", "ops.json",
                      f"OP livello 4: {name} (bypass player-limit: {op.get('bypassesPlayerLimit', False)}).",
                      "Verifica che sia una persona fidata: livello 4 = controllo totale del server.")
    if len(ops) > 5:
        audit.add("LOW", "core", "ops.json", f"{len(ops)} operatori registrati.",
                  "Riduci gli op al minimo indispensabile.")


def audit_whitelist(root: Path, audit: Audit):
    sp = root / "server.properties"
    p = root / "whitelist.json"
    if not sp.is_file():
        return
    props = parse_properties(sp)
    if props.get("white-list", "false").lower() == "true" and p.is_file():
        try:
            wl = json.loads(p.read_text(encoding="utf-8"))
            if isinstance(wl, list) and len(wl) == 0:
                audit.add("MEDIUM", "core", "whitelist.json",
                          "Whitelist attiva ma vuota: nessuno può entrare.",
                          "Aggiungi i giocatori con /whitelist add <nome>.")
        except Exception:
            pass


# --------------------------------------------------------------------------
# Permissions: pericolose e wildcard
# --------------------------------------------------------------------------

# Permesso -> (severity su gruppo default, severity su gruppo non-default)
DANGEROUS_PERMISSIONS = {
    "*": ("CRITICAL", "HIGH"),
    "minecraft.*": ("CRITICAL", "HIGH"),
    "minecraft.command.op": ("CRITICAL", "HIGH"),
    "minecraft.command.deop": ("CRITICAL", "HIGH"),
    "minecraft.command.stop": ("HIGH", "MEDIUM"),
    "bukkit.*": ("CRITICAL", "HIGH"),
    "bukkit.command.op": ("CRITICAL", "HIGH"),
    "bukkit.command.reload": ("MEDIUM", "LOW"),
    "luckperms.*": ("CRITICAL", "HIGH"),
    "luckperms.editor": ("HIGH", "MEDIUM"),
    "luckperms.editor.*": ("HIGH", "MEDIUM"),
    "essentials.*": ("HIGH", "MEDIUM"),
    "essentials.gamemode.others": ("MEDIUM", "LOW"),
    "worldedit.*": ("MEDIUM", "LOW"),
    "worldguard.*": ("MEDIUM", "LOW"),
    "permissions.*": ("CRITICAL", "HIGH"),
    "pex.*": ("CRITICAL", "HIGH"),
}


def classify_permission(perm):
    """Match esatto o wildcard-suffisso (es. minecraft.command.*)."""
    if perm in DANGEROUS_PERMISSIONS:
        return DANGEROUS_PERMISSIONS[perm]
    for key in DANGEROUS_PERMISSIONS:
        if key.endswith(".*") and perm.startswith(key[:-1]):
            return DANGEROUS_PERMISSIONS[key]
    return None


def audit_group_permission(audit, plugin, file, group, perm):
    entry = classify_permission(perm)
    if not entry:
        return
    sev_default, sev_other = entry
    is_default = group.lower() in ("default", "member", "player", "everyone")
    sev = sev_default if is_default else sev_other
    audit.add(sev, plugin, file,
              f"Gruppo '{group}' ha il permesso pericoloso '{perm}'."
              + (" Questo gruppo è assegnato a chi entra senza rank." if is_default else ""),
              f"Rimuovi '{perm}' dal gruppo '{group}' e assegnalo solo a un gruppo admin ristretto.")


# --------------------------------------------------------------------------
# LuckPerms (yaml-storage / json-storage)
# --------------------------------------------------------------------------

def mini_yaml(text):
    """Parser minimale per i file gruppo di LuckPerms (key: value + liste '- item').
    Se PyYAML è installato viene preferito dal chiamante."""
    data = {}
    current_list_key = None
    for raw in text.splitlines():
        if not raw.strip() or raw.lstrip().startswith("#"):
            continue
        stripped = raw.strip()
        # LuckPerms scrive le liste con dash a indentazione 0 o 2: accettiamo entrambe.
        if stripped.startswith("- ") and current_list_key is not None:
            item = stripped[2:].strip().strip("'\"")
            data.setdefault(current_list_key, []).append(item)
        elif ":" in stripped:
            key, _, value = stripped.partition(":")
            key = key.strip().strip("'\"")
            value = value.strip()
            if value == "":
                current_list_key = key
                data.setdefault(key, [])
            else:
                current_list_key = None
                if value.startswith("[") and value.endswith("]"):
                    inner = value[1:-1].strip()
                    data[key] = [] if not inner else [v.strip().strip("'\"") for v in inner.split(",")]
                elif value in ("{}", "~", "null"):
                    data[key] = {}
                else:
                    data[key] = value.strip("'\"")
    return data


def check_lp_group(audit, file, group, permissions):
    for perm in permissions or []:
        perm = str(perm).strip()
        if not perm:
            continue
        base = perm.split(" ")[0]
        audit_group_permission(audit, "luckperms", file, group, base)


def audit_luckperms(root: Path, audit: Audit):
    lp = root / "plugins" / "LuckPerms"
    if not lp.is_dir():
        return

    yaml_groups = lp / "yaml-storage" / "groups"
    json_groups = lp / "json-storage" / "groups"
    found_any = False
    group_data = {}  # nome -> {"permissions": [...], "parents": [...]}

    if yaml_groups.is_dir():
        for f in sorted(yaml_groups.glob("*.yml")):
            found_any = True
            group = f.stem
            text = f.read_text(encoding="utf-8-sig", errors="replace")
            try:
                import yaml  # type: ignore
                data = yaml.safe_load(text) or {}
            except ImportError:
                data = mini_yaml(text)
            perms = data.get("permissions", []) or []
            if isinstance(perms, str):
                perms = [perms]
            parents = data.get("parents", []) or []
            if isinstance(parents, str):
                parents = [parents]
            group_data[group.lower()] = {"permissions": perms, "parents": parents}
            check_lp_group(audit, f.relative_to(root).as_posix(), group, perms)

    if json_groups.is_dir():
        for f in sorted(json_groups.glob("*.json")):
            found_any = True
            try:
                data = json.loads(f.read_text(encoding="utf-8"))
            except Exception:
                continue
            name = data.get("name", f.stem)
            parents = data.get("parents", []) or []
            if isinstance(parents, list):
                # Le voci possono essere stringhe o {"key": "nome"} a seconda della versione
                parents = [p.get("key", p.get("group", "")) if isinstance(p, dict) else p for p in parents]
                group_data[str(name).lower()] = {"permissions": data.get("permissions", []) or [], "parents": parents}
            check_lp_group(audit, f.relative_to(root).as_posix(), name, data.get("permissions", []))

    # Passata 2: ereditarietà — se un parent contiene permessi pericolosi,
    # anche i gruppi figli li ereditano di fatto.
    if group_data:
        for group, info in group_data.items():
            for parent in info.get("parents", []):
                parent = str(parent).strip().lower()
                if not parent:
                    continue
                pdata = group_data.get(parent)
                if not pdata:
                    continue
                for perm in pdata.get("permissions", []):
                    base = str(perm).split(" ")[0]
                    if classify_permission(base):
                        audit.add("MEDIUM", "luckperms", f"plugins/LuckPerms (gruppo '{group}')",
                                  f"Il gruppo '{group}' eredita da '{parent}' il permesso pericoloso '{base}'.",
                                  f"Controlla l'ereditarietà: /lp group {group} parent remove {parent}, o restringi i permessi del parent.")
                        break  # un rilievo per gruppo-parent basta

    if not found_any:
        config = lp / "config.yml"
        storage = "?"
        if config.is_file():
            m = re.search(r"^\s*storage-method:\s*(\S+)", config.read_text(encoding="utf-8", errors="replace"), re.M)
            if m:
                storage = m.group(1)
        if storage in ("h2", "mysql", "mariadb", "postgresql", "mongodb"):
            audit.add("INFO", "luckperms", "plugins/LuckPerms/config.yml",
                      f"Storage LuckPerms = {storage}: i permessi non sono leggibili da file.",
                      "Esporta i gruppi con '/lp export' o usa '/lp verbose' per verificare i permessi pericolosi.")


# --------------------------------------------------------------------------
# PermissionsEx
# --------------------------------------------------------------------------

def audit_pex(root: Path, audit: Audit):
    candidates = [
        root / "plugins" / "PermissionsEx" / "permissions.yml",
        root / "plugins" / "permissions.yml",
    ]
    pex_file = next((c for c in candidates if c.is_file()), None)
    if pex_file is None:
        return

    rel = pex_file.relative_to(root).as_posix()
    try:
        import yaml  # type: ignore
        data = yaml.safe_load(pex_file.read_text(encoding="utf-8-sig", errors="replace")) or {}
    except ImportError:
        data = None

    if isinstance(data, dict):
        groups = data.get("groups", {}) or {}
        for group, cfg in groups.items():
            if not isinstance(cfg, dict):
                continue
            perms = cfg.get("permissions", []) or []
            if isinstance(perms, str):
                perms = [perms]
            is_default = str(cfg.get("default", "")).lower() == "true"
            for perm in perms:
                base = str(perm).split(" ")[0]
                audit_group_permission(audit, "permissionsex", rel,
                                       "default" if is_default else str(group), base)
        return

    # Fallback euristico senza PyYAML: individua blocchi gruppo e permessi wildcard.
    text = pex_file.read_text(encoding="utf-8-sig", errors="replace")
    current_group, group_is_default = None, False
    for line in text.splitlines():
        m_group = re.match(r"^  ([A-Za-z0-9_\-\.]+):\s*$", line)
        if m_group:
            current_group = m_group.group(1)
            group_is_default = False
            continue
        if current_group and re.match(r"^\s+default:\s*true", line):
            group_is_default = True
        m_perm = re.match(r"^\s+-\s*['\"]?([^#'\"\s]+)", line)
        if current_group and m_perm:
            base = m_perm.group(1)
            if base.endswith(":") or base in ("options", "inheritance", "schema", "users"):
                continue
            audit_group_permission(audit, "permissionsex", rel,
                                   "default" if group_is_default else current_group, base)


# --------------------------------------------------------------------------
# Plugin jar inventory
# --------------------------------------------------------------------------

def audit_plugins(root: Path, audit: Audit):
    plugins_dir = root / "plugins"
    if not plugins_dir.is_dir():
        return
    jars = sorted(plugins_dir.glob("*.jar"))
    if not jars:
        return

    perm_managers = {
        "luckperms": "LuckPerms",
        "permissionsex": "PermissionsEx",
        "groupmanager": "GroupManager",
        "vault": "Vault",
    }
    for jar in jars:
        name = jar.name.lower()
        for key, pretty in perm_managers.items():
            if key in name:
                audit.add("INFO", "plugins", jar.relative_to(root).as_posix(),
                          f"Permission manager rilevato: {jar.name}.",
                          "Tienilo aggiornato: le versioni vecchie di plugin di permessi hanno CVE noti.")
                break
        if re.search(r"-\d+\.\d+", jar.name) is None:
            audit.add("LOW", "plugins", jar.relative_to(root).as_posix(),
                      f"Jar senza versione nel nome: {jar.name}.",
                      "Rinomina o aggiorna per tracciare le versioni; i plugin datati sono vettori d'attacco noti.")


# --------------------------------------------------------------------------
# Bukkit / Spigot / Paper config
# --------------------------------------------------------------------------

def audit_bukkit_configs(root: Path, audit: Audit):
    bukkit = root / "bukkit.yml"
    if bukkit.is_file():
        props = parse_properties(bukkit)
        if props.get("query.plugins", "").lower() == "true":
            audit.add("MEDIUM", "bukkit", "bukkit.yml",
                      "query.plugins=true: la lista plugin è esposta via GS4 query.",
                      "Imposta query.plugins=false.")

    paper = root / "config" / "paper-global.yml"
    if paper.is_file():
        text = paper.read_text(encoding="utf-8", errors="replace")
        m = re.search(r"^\s+rcon:\s*\n(?:[ \t]+.*\n)*?[ \t]+enabled:\s*(\S+)", text, re.M)
        if m and m.group(1).lower() == "true":
            audit.add("MEDIUM", "paper", "config/paper-global.yml",
                      "RCON abilitato anche nella config Paper.",
                      "Verifica password e esposizione della porta come per server.properties.")


# --------------------------------------------------------------------------
# Report
# --------------------------------------------------------------------------

SEV_COLOR = {"CRITICAL": "\033[91m", "HIGH": "\033[93m", "MEDIUM": "\033[96m",
             "LOW": "\033[94m", "INFO": "\033[92m", "END": "\033[0m"}


def print_report(audit: Audit, color: bool):
    def c(sev):
        return f"{SEV_COLOR[sev]}{sev}{SEV_COLOR['END']}" if color else sev

    counts = {}
    for f in audit.findings:
        counts[f["severity"]] = counts.get(f["severity"], 0) + 1

    print("=" * 78)
    print("AETHERIS SERVER AUDIT — report configurazione (solo server di tua proprietà)")
    print("=" * 78)
    if not audit.findings:
        print("Nessun problema rilevato. Controlla comunque gli aggiornamenti dei plugin.")
    for f in audit.sorted():
        print(f"[{c(f['severity'])}] {f['category']} — {f['file']}")
        print(f"    {f['detail']}")
        print(f"    → {f['recommendation']}")
    print("-" * 78)
    summary = ", ".join(f"{s}: {counts[s]}" for s in ("CRITICAL", "HIGH", "MEDIUM", "LOW", "INFO") if s in counts)
    print(f"Totale: {len(audit.findings)} rilievi ({summary or 'nessuno'})")
    print("Nota: strumento euristico — una verifica manuale resta consigliata.")


def md_report(audit: Audit) -> str:
    lines = ["# Aetheris Server Audit", "",
             "Report generato sui file del server analizzato (strumento difensivo per amministratori).", ""]
    if not audit.findings:
        lines.append("Nessun problema rilevato.")
    for f in audit.sorted():
        lines.append(f"## [{f['severity']}] {f['category']} — `{f['file']}`")
        lines.append("")
        lines.append(f"- **Problema:** {f['detail']}")
        lines.append(f"- **Raccomandazione:** {f['recommendation']}")
        lines.append("")
    return "\n".join(lines)


def main(argv=None):
    # Console Windows: forza UTF-8 con fallback per i caratteri accentati/frecce.
    for stream in (sys.stdout, sys.stderr):
        try:
            stream.reconfigure(encoding="utf-8", errors="replace")
        except Exception:
            pass

    parser = argparse.ArgumentParser(
        description="Audit difensivo della configurazione di un server Minecraft DI TUA PROPRIETÀ.")
    parser.add_argument("path", nargs="?", default=".", help="Cartella del server (default: cartella corrente)")
    parser.add_argument("--json", dest="json_out", metavar="FILE", help="Salva il report in JSON")
    parser.add_argument("--md", dest="md_out", metavar="FILE", help="Salva il report in Markdown")
    parser.add_argument("--no-color", action="store_true", help="Disattiva i colori terminale")
    args = parser.parse_args(argv)

    root = Path(args.path).resolve()
    if not root.is_dir():
        print(f"Errore: {root} non è una cartella valida.", file=sys.stderr)
        return 2

    audit = Audit()
    audit_server_properties(root, audit)
    audit_ops(root, audit)
    audit_whitelist(root, audit)
    audit_luckperms(root, audit)
    audit_pex(root, audit)
    audit_plugins(root, audit)
    audit_bukkit_configs(root, audit)

    print_report(audit, color=not args.no_color and sys.stdout.isatty())

    if args.json_out:
        Path(args.json_out).write_text(
            json.dumps({"server": str(root), "findings": audit.sorted()}, indent=2, ensure_ascii=False),
            encoding="utf-8")
        print(f"Report JSON salvato: {args.json_out}")
    if args.md_out:
        Path(args.md_out).write_text(md_report(audit), encoding="utf-8")
        print(f"Report Markdown salvato: {args.md_out}")

    return 1 if audit.worst in ("CRITICAL", "HIGH") else 0


if __name__ == "__main__":
    sys.exit(main())
