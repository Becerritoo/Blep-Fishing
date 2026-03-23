package com.kunfury.blepfishing.config;

import com.kunfury.blepfishing.BlepFishing;
import com.kunfury.blepfishing.helpers.Formatting;
import com.kunfury.blepfishing.helpers.Utilities;
import com.kunfury.blepfishing.ui.MenuHandler;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ConfigHandler {

    public static ConfigHandler instance;

    public BaseConfig baseConfig;
    public FishConfig fishConfig;
    public TournamentConfig tourneyConfig;
    public RarityConfig rarityConfig;
    public AreaConfig areaConfig;
    public TreasureConfig treasureConfig;
    public GuiConfig guiConfig;

    public HashMap<String, YamlConfiguration> Translations;

    private static final String DEFAULT_LANGUAGE = "English";
    private static final String LANG_DIR = "lang";
    private static final String LEGACY_LANG_DIR = "translations";

    private String activeLanguage = DEFAULT_LANGUAGE;
    private final Set<String> missingLocalizationWarnings = new HashSet<>();

    public void Initialize(){
        instance = this;

        baseConfig = new BaseConfig();
        initializeLanguageSystem();

        fishConfig = new FishConfig();
        tourneyConfig = new TournamentConfig();
        rarityConfig = new RarityConfig();
        areaConfig = new AreaConfig();
        treasureConfig = new TreasureConfig();

        guiConfig = new GuiConfig();
    }

    public List<String> ErrorMessages = new ArrayList<>();

    public void ReportIssue(String issue){
        ErrorMessages.add(issue);
        //TODO: Show error to those with bf admin
        //TODO: Button in message to overwrite file with fixed version
    }

    private void initializeLanguageSystem() {
        missingLocalizationWarnings.clear();
        ensureCoreDataFilesExist();
        exportBundledLanguageFiles();
        LoadTranslations();

        if (Translations == null || Translations.isEmpty()) {
            Utilities.Severe("No language files were found. Falling back to built-in defaults.");
        }

        String configuredLanguage = baseConfig.getLanguage();
        if (configuredLanguage == null || configuredLanguage.trim().isEmpty()) {
            configuredLanguage = DEFAULT_LANGUAGE;
        }

        String resolvedLanguage = resolveLanguageName(configuredLanguage);
        if (resolvedLanguage == null) {
            Bukkit.getLogger().warning("[BlepFishing] Language '" + configuredLanguage + "' not found. Using '" + DEFAULT_LANGUAGE + "'.");
            resolvedLanguage = DEFAULT_LANGUAGE;
        }

        activeLanguage = resolvedLanguage;
        seedDynamicLanguageEntries(activeLanguage);
        LoadTranslations();

        if (!loadLanguageInternal(activeLanguage, false)) {
            loadLanguageInternal(DEFAULT_LANGUAGE, false);
        }
    }

    private void ensureCoreDataFilesExist() {
        saveResourceIfMissing("fish.yml");
        saveResourceIfMissing("treasure.yml");
        saveResourceIfMissing("tournaments.yml");
        saveResourceIfMissing("rarities.yml");
        saveResourceIfMissing("areas.yml");
        saveResourceIfMissing("gui.yml");
    }

    private void saveResourceIfMissing(String resource) {
        File target = new File(BlepFishing.instance.getDataFolder(), resource);
        if (target.exists()) {
            return;
        }

        if (BlepFishing.instance.getResource(resource) == null) {
            return;
        }

        BlepFishing.instance.saveResource(resource, false);
    }

    private void exportBundledLanguageFiles() {
        File languageDir = getLanguageDirectory();
        if (!languageDir.exists() && !languageDir.mkdirs()) {
            Utilities.Severe("Could not create language directory: " + languageDir.getAbsolutePath());
            return;
        }

        List<String> bundledPaths = listBundledLanguagePaths(LANG_DIR + "/");
        if (bundledPaths.isEmpty()) {
            bundledPaths = listBundledLanguagePaths(LEGACY_LANG_DIR + "/");
        }

        for (String resourcePath : bundledPaths) {
            String fileName = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
            File target = new File(languageDir, fileName);
            if (target.exists()) {
                continue;
            }

            try (InputStream resourceStream = BlepFishing.instance.getResource(resourcePath)) {
                if (resourceStream == null) {
                    continue;
                }
                FileUtils.copyInputStreamToFile(resourceStream, target);
            } catch (IOException e) {
                Utilities.Severe("Failed to export language file: " + resourcePath + " -> " + e.getMessage());
            }
        }

        File[] existingLangFiles = languageDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if ((existingLangFiles == null || existingLangFiles.length == 0) && getMessagesFile().exists()) {
            YamlConfiguration legacyMessages = YamlConfiguration.loadConfiguration(getMessagesFile());
            legacyMessages.set("Language", DEFAULT_LANGUAGE);
            try {
                legacyMessages.save(new File(languageDir, DEFAULT_LANGUAGE + ".yml"));
            } catch (IOException e) {
                Utilities.Severe("Failed to convert legacy messages.yml into lang file: " + e.getMessage());
            }
        }
    }

    private List<String> listBundledLanguagePaths(String prefix) {
        List<String> result = new ArrayList<>();

        String jarPath = URLDecoder.decode(
                getClass().getProtectionDomain().getCodeSource().getLocation().getPath(),
                StandardCharsets.UTF_8
        );
        File jarFile = new File(jarPath);

        if (!jarFile.isFile()) {
            return result;
        }

        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                String path = entries.nextElement().getName();
                if (path.startsWith(prefix) && path.toLowerCase().endsWith(".yml")) {
                    result.add(path);
                }
            }
        } catch (IOException e) {
            Utilities.Severe("Failed to scan bundled language files: " + e.getMessage());
        }

        result.sort(Comparator.naturalOrder());
        return result;
    }

    private File getLanguageDirectory() {
        return new File(BlepFishing.instance.getDataFolder(), LANG_DIR);
    }

    private File getMessagesFile() {
        return new File(BlepFishing.instance.getDataFolder(), "messages.yml");
    }

    private File getLanguageFile(String language) {
        return new File(getLanguageDirectory(), language + ".yml");
    }

    private String resolveLanguageName(String requestedName) {
        if (requestedName == null || requestedName.trim().isEmpty()) {
            return null;
        }

        if (Translations != null && Translations.containsKey(requestedName)) {
            return requestedName;
        }

        if (Translations != null) {
            for (String lang : Translations.keySet()) {
                if (lang.equalsIgnoreCase(requestedName)) {
                    return lang;
                }
            }
        }

        return null;
    }

    private void mergeYaml(YamlConfiguration from, YamlConfiguration into) {
        if (from == null || into == null) {
            return;
        }

        for (String path : from.getKeys(true)) {
            into.set(path, from.get(path));
        }
    }

    private void writeMessagesFile(YamlConfiguration languageYaml) {
        try {
            languageYaml.save(getMessagesFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean loadLanguageInternal(String language, boolean persistInConfig) {
        String resolved = resolveLanguageName(language);
        if (resolved == null) {
            return false;
        }

        YamlConfiguration selectedYaml = Translations.get(resolved);
        YamlConfiguration englishYaml = Translations.get(DEFAULT_LANGUAGE);
        if (englishYaml == null) {
            englishYaml = selectedYaml;
        }

        if (selectedYaml == null) {
            return false;
        }

        YamlConfiguration mergedYaml = new YamlConfiguration();
        mergeYaml(englishYaml, mergedYaml);
        mergeYaml(selectedYaml, mergedYaml);

        mergedYaml.set("Language", resolved);

        Formatting.languageYaml = mergedYaml;
        activeLanguage = resolved;
        writeMessagesFile(mergedYaml);

        if (persistInConfig && baseConfig != null) {
            baseConfig.setLanguage(resolved);
            baseConfig.Save();
        }

        return true;
    }

    public void LoadLanguage(String language){
        if (loadLanguageInternal(language, true)) {
            return;
        }

        Utilities.Severe("Tried to load invalid Language: " + language);
        if (!DEFAULT_LANGUAGE.equalsIgnoreCase(language)) {
            loadLanguageInternal(DEFAULT_LANGUAGE, true);
        }
    }

    private boolean setMissing(YamlConfiguration yaml, String path, Object value) {
        if (yaml == null || value == null) {
            return false;
        }
        if (yaml.contains(path)) {
            return false;
        }
        yaml.set(path, value);
        return true;
    }

    private void seedDynamicLanguageEntries(String language) {
        File englishFile = getLanguageFile(DEFAULT_LANGUAGE);
        File activeFile = getLanguageFile(language);

        if (!englishFile.exists()) {
            return;
        }

        if (!activeFile.exists() && !DEFAULT_LANGUAGE.equals(language)) {
            try {
                FileUtils.copyFile(englishFile, activeFile);
            } catch (IOException e) {
                Utilities.Severe("Failed to create language seed file for " + language + ": " + e.getMessage());
                return;
            }
        }

        YamlConfiguration englishYaml = YamlConfiguration.loadConfiguration(englishFile);
        YamlConfiguration activeYaml = DEFAULT_LANGUAGE.equals(language)
                ? englishYaml
                : YamlConfiguration.loadConfiguration(activeFile);

        boolean englishChanged = false;
        boolean activeChanged = false;

        YamlConfiguration fishYaml = loadConfigYaml("fish.yml");
        if (fishYaml != null) {
            for (String fishId : fishYaml.getKeys(false)) {
                if (!fishYaml.isConfigurationSection(fishId)) {
                    continue;
                }
                englishChanged |= setMissing(englishYaml, "Text.Fish." + fishId + ".Name", fishYaml.getString(fishId + ".Name"));
                englishChanged |= setMissing(englishYaml, "Text.Fish." + fishId + ".Lore", fishYaml.getString(fishId + ".Lore"));
                englishChanged |= setMissing(englishYaml, "Text.Fish." + fishId + ".Description", fishYaml.getString(fishId + ".Description"));

                if (activeYaml != englishYaml) {
                    activeChanged |= setMissing(activeYaml, "Text.Fish." + fishId + ".Name", fishYaml.getString(fishId + ".Name"));
                    activeChanged |= setMissing(activeYaml, "Text.Fish." + fishId + ".Lore", fishYaml.getString(fishId + ".Lore"));
                    activeChanged |= setMissing(activeYaml, "Text.Fish." + fishId + ".Description", fishYaml.getString(fishId + ".Description"));
                }
            }
        }

        YamlConfiguration rarityYaml = loadConfigYaml("rarities.yml");
        if (rarityYaml != null) {
            for (String rarityId : rarityYaml.getKeys(false)) {
                if (!rarityYaml.isConfigurationSection(rarityId)) {
                    continue;
                }
                englishChanged |= setMissing(englishYaml, "Text.Rarities." + rarityId + ".Name", rarityYaml.getString(rarityId + ".Name"));
                if (activeYaml != englishYaml) {
                    activeChanged |= setMissing(activeYaml, "Text.Rarities." + rarityId + ".Name", rarityYaml.getString(rarityId + ".Name"));
                }
            }
        }

        YamlConfiguration areaYaml = loadConfigYaml("areas.yml");
        if (areaYaml != null) {
            for (String areaId : areaYaml.getKeys(false)) {
                if (!areaYaml.isConfigurationSection(areaId)) {
                    continue;
                }
                englishChanged |= setMissing(englishYaml, "Text.Areas." + areaId + ".Name", areaYaml.getString(areaId + ".Name"));
                englishChanged |= setMissing(englishYaml, "Text.Areas." + areaId + ".CompassHint", areaYaml.getString(areaId + ".Compass Hint"));

                if (activeYaml != englishYaml) {
                    activeChanged |= setMissing(activeYaml, "Text.Areas." + areaId + ".Name", areaYaml.getString(areaId + ".Name"));
                    activeChanged |= setMissing(activeYaml, "Text.Areas." + areaId + ".CompassHint", areaYaml.getString(areaId + ".Compass Hint"));
                }
            }
        }

        YamlConfiguration treasureYaml = loadConfigYaml("treasure.yml");
        if (treasureYaml != null) {
            ConfigurationSection caskets = treasureYaml.getConfigurationSection("Caskets");
            if (caskets != null) {
                for (String casketId : caskets.getKeys(false)) {
                    String path = "Text.Treasure.Caskets." + casketId + ".Name";
                    String value = caskets.getString(casketId + ".Name");
                    englishChanged |= setMissing(englishYaml, path, value);
                    if (activeYaml != englishYaml) {
                        activeChanged |= setMissing(activeYaml, path, value);
                    }
                }
            }
        }

        YamlConfiguration tournamentYaml = loadConfigYaml("tournaments.yml");
        if (tournamentYaml != null) {
            for (String tournamentId : tournamentYaml.getKeys(false)) {
                if ("Settings".equalsIgnoreCase(tournamentId) || !tournamentYaml.isConfigurationSection(tournamentId)) {
                    continue;
                }
                String path = "Text.Tournaments." + tournamentId + ".Name";
                String value = tournamentYaml.getString(tournamentId + ".Name");
                englishChanged |= setMissing(englishYaml, path, value);
                if (activeYaml != englishYaml) {
                    activeChanged |= setMissing(activeYaml, path, value);
                }
            }
        }

        YamlConfiguration guiYaml = loadConfigYaml("gui.yml");
        if (guiYaml != null) {
            String title = guiYaml.getString("PlayerPanel.Title");
            englishChanged |= setMissing(englishYaml, "Text.Gui.PlayerPanel.Title", title);
            if (activeYaml != englishYaml) {
                activeChanged |= setMissing(activeYaml, "Text.Gui.PlayerPanel.Title", title);
            }

            ConfigurationSection buttons = guiYaml.getConfigurationSection("PlayerPanel.Buttons");
            if (buttons != null) {
                for (String buttonKey : buttons.getKeys(false)) {
                    ConfigurationSection item = buttons.getConfigurationSection(buttonKey + ".Item");
                    if (item == null) {
                        continue;
                    }

                    String namePath = "Text.Gui.PlayerPanel.Buttons." + buttonKey + ".Name";
                    String name = item.getString("Name");
                    englishChanged |= setMissing(englishYaml, namePath, name);
                    if (activeYaml != englishYaml) {
                        activeChanged |= setMissing(activeYaml, namePath, name);
                    }

                    if (item.contains("Lore")) {
                        List<String> lore = item.getStringList("Lore");
                        String lorePath = "Text.Gui.PlayerPanel.Buttons." + buttonKey + ".Lore";
                        englishChanged |= setMissing(englishYaml, lorePath, lore);
                        if (activeYaml != englishYaml) {
                            activeChanged |= setMissing(activeYaml, lorePath, lore);
                        }
                    }
                }
            }
        }

        try {
            if (englishChanged) {
                englishYaml.save(englishFile);
            }
            if (activeYaml != englishYaml && activeChanged) {
                activeYaml.save(activeFile);
            }
        } catch (IOException e) {
            Utilities.Severe("Failed to save seeded language files: " + e.getMessage());
        }
    }

    private YamlConfiguration loadConfigYaml(String fileName) {
        File file = new File(BlepFishing.instance.getDataFolder(), fileName);
        if (!file.exists()) {
            return null;
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private void warnMissingLocalizationKey(String key) {
        if (key == null || key.isEmpty()) {
            return;
        }

        if (missingLocalizationWarnings.add(key)) {
            Bukkit.getLogger().warning("[BlepFishing] Missing language key '" + key + "'. Falling back to config value.");
        }
    }

    public String getLocalizedValue(String key, String fallback) {
        if (key == null || key.isEmpty()) {
            return fallback;
        }

        String value = Formatting.languageYaml.getString(key);
        if (value != null) {
            return value;
        }

        YamlConfiguration english = Translations != null ? Translations.get(DEFAULT_LANGUAGE) : null;
        if (english != null) {
            String englishValue = english.getString(key);
            if (englishValue != null) {
                return englishValue;
            }
        }

        if (fallback == null || fallback.trim().isEmpty()) {
            return fallback;
        }

        warnMissingLocalizationKey(key);
        return fallback;
    }

    public List<String> getLocalizedList(String key, List<String> fallback) {
        if (key == null || key.isEmpty()) {
            return fallback != null ? fallback : new ArrayList<>();
        }

        if (Formatting.languageYaml.contains(key)) {
            return Formatting.languageYaml.getStringList(key);
        }

        YamlConfiguration english = Translations != null ? Translations.get(DEFAULT_LANGUAGE) : null;
        if (english != null && english.contains(key)) {
            return english.getStringList(key);
        }

        if (fallback == null || fallback.isEmpty()) {
            return fallback != null ? fallback : new ArrayList<>();
        }

        warnMissingLocalizationKey(key);
        return fallback != null ? fallback : new ArrayList<>();
    }

    private void LoadTranslations(){
        Translations = new HashMap<>();

        File languageDir = getLanguageDirectory();
        if (!languageDir.exists()) {
            return;
        }

        File[] files = languageDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (files == null || files.length == 0) {
            return;
        }

        List<File> sortedFiles = new ArrayList<>();
        for (File file : files) {
            sortedFiles.add(file);
        }
        sortedFiles.sort(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));

        for (File file : sortedFiles) {
            String fileName = file.getName();
            int dotPos = fileName.lastIndexOf('.');
            String languageName = dotPos == -1 ? fileName : fileName.substring(0, dotPos);

            YamlConfiguration languageYaml = YamlConfiguration.loadConfiguration(file);
            Translations.put(languageName, languageYaml);
        }
    }

    public void Reload() {
        baseConfig = new BaseConfig();
        initializeLanguageSystem();

        fishConfig = new FishConfig();
        tourneyConfig = new TournamentConfig();
        rarityConfig = new RarityConfig();
        areaConfig = new AreaConfig();
        treasureConfig = new TreasureConfig();
        guiConfig = new GuiConfig();

        MenuHandler.reload();
    }
}
