import express from 'express';
import cors from 'cors';
import fs from 'fs';
import path from 'path';
import { exec } from 'child_process';
import util from 'util';

const execPromise = util.promisify(exec);
const app = express();
const port = 3000;

app.use(cors());
app.use(express.json());
app.use(express.static('dist'));

app.post('/install', async (req, res) => {
    try {
        const mcDir = path.join(process.env.APPDATA, '.minecraft');
        const modsDir = path.join(mcDir, 'mods');

        // Clean Install Logic: Backup old mods folder
        if (req.body.clean && fs.existsSync(modsDir)) {
            const backupDir = path.join(mcDir, `mods_backup_${Date.now()}`);
            fs.renameSync(modsDir, backupDir);
            console.log(`Mods backed up to ${backupDir}`);
        }

        // 1. Install Fabric 0.19.3 silently
        await execPromise(`java -jar fabric-installer.jar client -dir "${mcDir}" -mcversion 1.21.4 -loader 0.19.3`);

        // 1.5 Rename profile to Aetheris
        const oldVersionDir = path.join(mcDir, 'versions', 'fabric-loader-0.19.3-1.21.4');
        const newVersionDir = path.join(mcDir, 'versions', 'Aetheris-1.21.4');
        if (fs.existsSync(oldVersionDir)) {
            if (fs.existsSync(newVersionDir)) {
                fs.rmSync(newVersionDir, { recursive: true, force: true });
            }
            fs.renameSync(oldVersionDir, newVersionDir);
            const oldJson = path.join(newVersionDir, 'fabric-loader-0.19.3-1.21.4.json');
            const newJson = path.join(newVersionDir, 'Aetheris-1.21.4.json');
            if (fs.existsSync(oldJson)) fs.renameSync(oldJson, newJson);
            
            if (fs.existsSync(newJson)) {
                let jsonContent = JSON.parse(fs.readFileSync(newJson, 'utf-8'));
                jsonContent.id = 'Aetheris-1.21.4';
                fs.writeFileSync(newJson, JSON.stringify(jsonContent, null, 2));
            }
            
            const profilesPath = path.join(mcDir, 'launcher_profiles.json');
            if (fs.existsSync(profilesPath)) {
                const profilesData = JSON.parse(fs.readFileSync(profilesPath, 'utf-8'));
                profilesData.profiles["Aetheris"] = {
                    name: "Aetheris",
                    type: "custom",
                    lastVersionId: "Aetheris-1.21.4"
                };
                fs.writeFileSync(profilesPath, JSON.stringify(profilesData, null, 2));
            }
        }

        // 2. Ensure mods directory exists
        if (!fs.existsSync(modsDir)) {
            fs.mkdirSync(modsDir, { recursive: true });
        }

        // 3. Copy Aetheris Core
        const sourceJar = path.resolve(__dirname, '../ClientCore/build/libs/aetheris-core-1.0.0.jar');
        if (fs.existsSync(sourceJar)) {
            fs.copyFileSync(sourceJar, path.join(modsDir, 'aetheris-core-1.0.0.jar'));
        } else {
            throw new Error("Aetheris Core JAR not found! Please build it first.");
        }

        res.json({ success: true, message: 'Installazione completata con successo!' });
    } catch (error) {
        console.error('Install error:', error);
        res.status(500).json({ success: false, message: error.message });
    }
});

app.post('/launch', async (req, res) => {
    try {
        const launcherPaths = [
            'C:\\Program Files (x86)\\Minecraft Launcher\\MinecraftLauncher.exe',
            'C:\\Program Files\\Minecraft Launcher\\MinecraftLauncher.exe',
            path.join(process.env.LOCALAPPDATA || '', 'Programs\\Minecraft Launcher\\MinecraftLauncher.exe')
        ];

        let launched = false;
        for (const lPath of launcherPaths) {
            if (fs.existsSync(lPath)) {
                // Use start to launch it independently and not block the server
                await execPromise(`start "" "${lPath}"`);
                launched = true;
                break;
            }
        }

        if (!launched) {
            res.json({ success: true, message: "Non riesco a trovare il tuo Launcher in automatico. Nessun problema, l'installazione di Aetheris è completata: apri pure Minecraft manualmente!" });
            return;
        }

        res.json({ success: true, message: "" });
    } catch (error) {
        console.error('Launch error:', error);
        res.status(500).json({ success: false });
    }
});

app.listen(port, () => {
    console.log(`Aetheris installer backend in ascolto su http://localhost:${port}`);
});
