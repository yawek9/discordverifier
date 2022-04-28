/*
 * This file is part of DiscordVerifier, licensed under GNU GPLv3 license.
 * Copyright (C) 2022 yawek9
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.yawek.discordverifier.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import xyz.yawek.discordverifier.DiscordVerifier;
import xyz.yawek.discordverifier.utils.LogUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigProvider {

    private final DiscordVerifier verifier;
    private HashMap<String, Object> config;

    public ConfigProvider(DiscordVerifier verifier) {
        this.verifier = verifier;
    }

    public void loadConfig() {
        Path directory = verifier.getDataDirectory();
        if (!directory.toFile().exists()) directory.toFile().mkdirs();

        Yaml yaml = new Yaml();
        File configFile = new File(directory.toString(), "config.yml");
        if (!configFile.exists()) {
            InputStream inputStream = verifier.getClass()
                    .getClassLoader()
                    .getResourceAsStream("config.yml");
            try (OutputStream outputStream = new FileOutputStream(configFile, false)) {
                int read;
                byte[] bytes = new byte[8192];
                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            } catch (IOException e) {
                LogUtils.error("Unable to create config file.");
                e.printStackTrace();
            }
        } else {
            try {
                File file = new File(directory.toString(), "config.yml");
                DumperOptions options = new DumperOptions();
                options.setAllowUnicode(true);
                options.setIndent(2);
                options.setPrettyFlow(true);
                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                Yaml yamlToWrite = new Yaml(options);
                InputStream inputStream = new FileInputStream(file);
                Map<String, Object> map = yamlToWrite.load(inputStream);
                InputStream targetInputStream = verifier.getClass()
                        .getClassLoader()
                        .getResourceAsStream("config.yml");
                Map<String, Object> targetMap = yamlToWrite.load(targetInputStream);
                boolean update = false;
                for (String s : targetMap.keySet()) {
                    if (!map.containsKey(s)) {
                        map.put(s, targetMap.get(s));
                        update = true;
                    }
                }
                if (update) {
                    OutputStreamWriter writer =
                            new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);

                    yamlToWrite.dump(map, writer);
                }
            } catch (IOException exception) {
                LogUtils.error("Unable to update config file.");
                exception.printStackTrace();
            }
        }

        try {
            config = new HashMap<>(yaml.load(new FileInputStream(configFile)));
        } catch (FileNotFoundException e) {
            LogUtils.error("Unable to load config file.");
            e.printStackTrace();
        }
    }

    public String getString(String key) {
        return String.valueOf(config.get(key));
    }

    public int getInt(String key) {
        return (int) config.get(key);
    }

    public boolean getBoolean(String key) {
        return (boolean) config.get(key);
    }

    @SuppressWarnings("unchecked")
    public LinkedHashMap<String, ?> getMap(String key) {
        return (LinkedHashMap<String, ?>) config.get(key);
    }

}
