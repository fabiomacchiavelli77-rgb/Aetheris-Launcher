package net.aetheris.client.account;

import net.minecraft.client.Minecraft;
import net.minecraft.client.User;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();
    private final List<String> alts = new ArrayList<>();
    private String currentAlt = "";

    private SessionManager() {
        alts.add("AetherisUser");
    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public List<String> getAlts() {
        return alts;
    }

    public void addAlt(String username) {
        if (username != null && !username.trim().isEmpty() && !alts.contains(username.trim())) {
            alts.add(username.trim());
        }
    }

    public void removeAlt(String username) {
        alts.remove(username);
    }

    public void setSession(String username) {
        if (username == null || username.trim().isEmpty()) return;
        try {
            String cleanName = username.trim();
            UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + cleanName).getBytes(StandardCharsets.UTF_8));
            User newUser = new User(cleanName, uuid, "", Optional.empty(), Optional.empty(), User.Type.LEGACY);
            
            Minecraft mc = Minecraft.getInstance();
            mc.user = newUser;
            this.currentAlt = cleanName;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getCurrentAlt() {
        if (Minecraft.getInstance().getUser() != null) {
            return Minecraft.getInstance().getUser().getName();
        }
        return currentAlt;
    }
}
