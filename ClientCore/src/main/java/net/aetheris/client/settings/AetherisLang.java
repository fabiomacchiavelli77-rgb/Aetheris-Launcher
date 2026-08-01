package net.aetheris.client.settings;

/**
 * Global language manager for Aetheris client.
 * Supports Italian (IT) and English (EN) with a simple toggle.
 * The choice is persisted in the profile.
 */
public class AetherisLang {

    public enum Language {
        IT("🇮🇹 Italiano"),
        EN("🇬🇧 English");

        private final String displayName;
        Language(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    private static Language current = Language.IT; // default: Italian

    public static Language get() { return current; }
    public static void set(Language lang) { current = lang; }

    public static boolean isIT() { return current == Language.IT; }
    public static boolean isEN() { return current == Language.EN; }

    /** Toggle between IT and EN */
    public static void toggle() {
        current = (current == Language.IT) ? Language.EN : Language.IT;
    }

    /** Returns the flag emoji + short code for the current language */
    public static String getLabel() {
        return current == Language.IT ? "🇮🇹 IT" : "🇬🇧 EN";
    }
}
