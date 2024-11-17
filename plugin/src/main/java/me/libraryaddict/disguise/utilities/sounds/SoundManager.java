package me.libraryaddict.disguise.utilities.sounds;

import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class SoundManager {
    public void load() {
        SoundGroup.getGroups().clear();

        loadSounds();
        loadCustomSounds();
    }

    private void loadCustomSounds() {
        File f = new File(LibsDisguises.getInstance().getDataFolder(), "configs/sounds.yml");

        if (!f.exists()) {
            f.getParentFile().mkdirs();

            File old = new File(LibsDisguises.getInstance().getDataFolder(), "sounds.yml");

            if (old.exists()) {
                old.renameTo(f);
            } else {
                LibsDisguises.getInstance().saveResource("configs/sounds.yml", false);
            }
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(f);

        for (String key : config.getKeys(false)) {
            if (!config.isConfigurationSection(key) || key.equals("GroupName")) {
                continue;
            }

            if (SoundGroup.getGroups().keySet().stream().anyMatch(k -> k.equalsIgnoreCase(key))) {
                LibsDisguises.getInstance().getLogger().warning("The SoundGroup " + key + " has already been registered!");
                continue;
            }

            SoundGroup group = new SoundGroup(key);
            ConfigurationSection section = config.getConfigurationSection(key);

            for (SoundGroup.SoundType type : SoundGroup.SoundType.values()) {
                if (type == SoundGroup.SoundType.CANCEL) {
                    continue;
                }

                List<String> list = section.getStringList(type.name().charAt(0) + type.name().substring(1).toLowerCase(Locale.ENGLISH));

                if (list == null || list.isEmpty()) {
                    continue;
                }

                for (String sound : list) {
                    if (!sound.matches(".+:.+")) {
                        SoundGroup subGroup = SoundGroup.getGroup(sound);

                        if (subGroup == null) {
                            LibsDisguises.getInstance().getLogger()
                                .warning("Invalid sound '" + sound + "'! Must be a minecraft:sound.name or SoundGroup name!");
                            continue;
                        }

                        Object[] sounds = subGroup.getDisguiseSounds().get(type);

                        if (sounds == null) {
                            LibsDisguises.getInstance().getLogger().warning(
                                "Sound group '" + sound + "' does not contain a category for " + type + "! Can't use as default in " + key);
                            continue;
                        }

                        for (Object obj : sounds) {
                            group.addSound(obj, type);
                        }

                        continue;
                    }

                    group.addSound(sound, type);
                }
            }

            LibsDisguises.getInstance().getLogger().info("Loaded sound group '" + key + "'");
        }
    }

    private void loadSounds() {
        try (InputStream stream = LibsDisguises.getInstance().getResource("SOUND_MAPPINGS.txt")) {
            String[] lines = new String(ReflectionManager.readFuzzyFully(stream), StandardCharsets.UTF_8).split("\n");

            for (String line : lines) {
                String[] groups = line.split("/", -1);

                SoundGroup group = new SoundGroup(groups[0]);

                int i = 0;
                for (SoundGroup.SoundType type : SoundGroup.SoundType.values()) {
                    String s = groups[++i];

                    if (s.isEmpty()) {
                        continue;
                    }

                    String[] sounds = s.split(",", -1);

                    for (String sound : sounds) {
                        try {
                            Sound actualSound = ReflectionManager.fromEnum(Sound.class, sound);

                            group.addSound(actualSound, type);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        } catch (IOException | NoClassDefFoundError e) {
            e.printStackTrace();
        }
    }
}
