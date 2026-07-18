# Piano Implementazione — Seed Cracker + Anti-Detection

## Contesto

L'installer Aristois (questo progetto) è un tool desktop che scarica e installa il mod Aristois per Minecraft. Non ha accesso al codice sorgente del mod — scarica JAR pre-compilati da `maven.aristois.net` e `gitlab.com/EMC-Framework/maven`.

**Richiesta:** Aggiungere all'installer:
1. **Seed Cracker**: installare SeedCrackerX (da https://github.com/19MisterX98/SeedcrackerX) insieme ad Aristois
2. **Anti-Detection**: rendere il mod più difficile da rilevare per plugin anti-cheat (Grim, Spartan)

---

## Riepilogo Modifiche

| File | Tipo | Descrizione |
|------|------|-------------|
| `model/Configuration.java` | Modifica | Aggiunge campi `seedCracker` e `antiDetection` |
| `view/components/CheckBox.java` | Modifica | Aggiunge callback `onChange` |
| `view/scenes/SetupScene.java` | Modifica | Due nuove checkbox: SeedCrackerX, Anti-detection |
| `view/scenes/WelcomeScene.java` | Modifica | Fetch releases SeedCrackerX all'avvio |
| `view/scenes/InstallingScene.java` | Modifica | Chiamate install SeedCrackerX + patching anti-detection |
| `model/provider/Provider.java` | Modifica | Nuovo metodo `installSeedCracker()` |
| `model/SeedCrackerManager.java` | **Nuovo** | Gestisce download SeedCrackerX da GitHub API |
| `model/JarPatcher.java` | **Nuovo** | Patcha metadata JAR (fabric.mod.json / mcmod.info) |

**Nessuna nuova dipendenza.** Solo `java.util.zip`, `com.google.gson` (già presenti).

---

## Fase 1: Fondamenta

### 1.1 Configuration.java
Aggiungere campi e builder fluent:
```java
private boolean seedCracker;
private boolean antiDetection;

public Configuration withSeedCracker(boolean state) { ... }
public Configuration withAntiDetection(boolean state) { ... }
public boolean isSeedCracker() { ... }
public boolean isAntiDetection() { ... }
```

### 1.2 CheckBox.java — callback onChange
Aggiungere:
```java
private Runnable onChange;

public CheckBox withOnChange(Runnable r) {
    this.onChange = r;
    return this;
}
```
Chiamare `onChange.run()` in `mouseClick()` dopo aver togglato `checked`.

### 1.3 JarPatcher.java (nuovo file)
Utility stateless: `JarPatcher.patch(Path jarPath) → boolean`
- Apre JAR come zip
- Cerca `fabric.mod.json` e `mcmod.info`
- Appende suffisso random (6 char UUID) a `id`/`modid`
- Rinomina `name` aggiungendo " (Optimization)"
- Scrive JAR patchato via file temporaneo + move atomica

### 1.4 SeedCrackerManager.java (nuovo file)
Singleton, pattern come `Config.getInstance()`:
- `fetchReleases()` — chiama GitHub API `https://api.github.com/repos/19MisterX98/SeedcrackerX/releases`
- `findDownloadUrl(String mcVersion) → Optional<String>` — matcha versione Minecraft con release name
- `downloadJar(String url) → InputStream`
- Supporta match esatto, range ("1.21.2–1.21.3"), prefix match
- Se nessun match trovato: `Optional.empty()` → installer salta SeedCrackerX con warning

---

## Fase 2: Provider Interface

### 2.1 Provider.java
Aggiungere default method `installSeedCracker()`:
```java
default void installSeedCracker(Path root, Launcher launcher,
        String profile, String mcVersion, Consumer<String> logger) throws IOException
```
- Chiama `SeedCrackerManager.findDownloadUrl(mcVersion)`
- Se trovato, scarica JAR in `launcher.getModsDirectory(root, profile)`
- Per protocollo ≤573 (pre-1.16), usa sottodirectory `mods/{version}/`

---

## Fase 3: UI e Wiring

### 3.1 SetupScene.java
Aggiungere due checkbox sotto "Clean install" / "Forge":

```
Y=330: [Clean install] [Forge]
Y=380: [Install SeedCrackerX] [Anti-detection patch]
```

**Logica interdipendenza Forge ↔ SeedCrackerX:**
```java
forgeInstall.withOnChange(() -> {
    if (forgeInstall.isChecked()) {
        seedCracker.setChecked(false);
        seedCracker.setVisible(false);
    } else {
        seedCracker.setVisible(true);
    }
});
```

**Passaggio a Configuration:**
```java
Configuration configuration = new Configuration()
    .withClean(cleanInstall.isChecked())
    .withForge(forgeInstall.isChecked())
    .withSeedCracker(seedCracker.isChecked())
    .withAntiDetection(antiDetection.isChecked());
```

Nota: Le nuove checkbox vanno aggiunte PRIMA del bottone "Next" e PRIMA di `Collections.reverse(components)`.

### 3.2 WelcomeScene.java
In `fetch()`, dopo `PistonMeta.init()`, aggiungere:
```java
try {
    progressBar.setLabel("Fetching SeedCrackerX versions");
    SeedCrackerManager.getInstance().fetchReleases();
} catch (Exception ex) {
    logger.warn("Unable to fetch SeedCrackerX versions", ex);
}
```
Non-critico — se fallisce, l'installazione continua senza SeedCrackerX.

### 3.3 InstallingScene.java
In `run()`, dopo il blocco `if (configuration.isForge())` / `else` e prima di "Done":

**Seed Cracker:**
```java
if (configuration.isSeedCracker() && !configuration.isForge()) {
    logText.accept("Installing SeedCrackerX");
    try {
        provider.installSeedCracker(root, launcher, profile, provider.getVersion(), logText);
    } catch (Exception ex) {
        log.append("SeedCrackerX install failed: " + ex.getMessage());
    }
} else if (configuration.isSeedCracker()) {
    log.append("Note: SeedCrackerX is Fabric-only, skipped (Forge install)");
}
```

**Anti-Detection:**
```java
if (configuration.isAntiDetection()) {
    logText.accept("Applying anti-detection patches");
    Path installDir = configuration.isForge()
        ? launcher.getEMCDirectory(root, profile).resolve(provider.getVersion())
        : launcher.getInstallationDirectory(root);
    try (Stream<Path> walk = Files.walk(installDir)) {
        walk.filter(p -> p.toString().endsWith(".jar")).forEach(jar -> {
            try {
                if (JarPatcher.patch(jar)) {
                    logText.accept("Patched " + jar.getFileName());
                }
            } catch (Exception ex) {
                log.append("Patch failed: " + jar.getFileName());
            }
        });
    }
}
```

---

## Verifica

### Seed Cracker
1. Avviare installer → SetupScene mostra checkbox SeedCrackerX
2. Selezionare Forge → SeedCrackerX si nasconde
3. Deselezionare Forge, check SeedCrackerX, scegliere versione MC nota (es. "1.21.4")
4. Installare → nella cartella mods deve esistere `SeedCrackerX.jar`
5. Log deve mostrare "Downloading SeedCrackerX"
6. Con versione MC non supportata → warning nel log, installazione continua
7. Con rete disconnessa → errore grazioso, installazione continua

### Anti-Detection
1. Check "Anti-detection patch" in SetupScene
2. Installare (non-Forge)
3. Aprire JAR Aristois con tool zip → `fabric.mod.json` ha `id` con suffisso random
4. Avviare Minecraft → Aristois deve ancora caricarsi
5. Ripetere con Forge → EMC.jar e Aristois.jar entrambi patchati

---

## Rischi e Mitigazioni

| Rischio | Mitigazione |
|--------|-------------|
| GitHub API rate limit (60 req/h) | Solo 1 richiesta per sessione, fallimento non blocca installazione |
| Release naming di SeedCrackerX cambia | Parser euristico con fallback generosi, skip graceful su mismatch |
| Patch mod ID rompe caricamento Forge/Fabric | La mod ID è per identificazione, non per loading. Il suffisso random preserva compatibilità |
| Corruzione ZIP durante patch | File temporaneo + move atomica. In caso di crash, il JAR originale è intatto |
| Compatibilità Java 8 | Usa solo `java.util.zip`, `StandardCharsets` (Java 7+), lambda (Java 8+). Nessun `var`, text block, record |
