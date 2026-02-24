package com.stockmaster.i18n;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * Manages application language settings.
 * Uses Java Preferences to persist the selected language across sessions.
 */
public class LanguageManager {
    private static LanguageManager instance;
    private ResourceBundle bundle;
    private Locale currentLocale;
    private static final String PREF_KEY = "language";
    private final Preferences prefs;

    private LanguageManager() {
        prefs = Preferences.userNodeForPackage(LanguageManager.class);
        String savedLang = prefs.get(PREF_KEY, "en");
        setLocale(new Locale(savedLang));
    }

    public static synchronized LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }

    public void setLocale(Locale locale) {
        this.currentLocale = locale;
        this.bundle = ResourceBundle.getBundle("i18n.messages", locale);
        prefs.put(PREF_KEY, locale.getLanguage());
    }

    public String get(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return "!" + key + "!";
        }
    }

    public String get(String key, Object... args) {
        try {
            return java.text.MessageFormat.format(bundle.getString(key), args);
        } catch (Exception e) {
            return "!" + key + "!";
        }
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public boolean isEnglish() {
        return "en".equals(currentLocale.getLanguage());
    }

    public boolean isSpanish() {
        return "es".equals(currentLocale.getLanguage());
    }
}
