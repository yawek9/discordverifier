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
import xyz.yawek.discordverifier.util.LogUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

public class ConfigProvider {

    private final DiscordVerifier verifier;
    private HashMap<String, Object> config;

    public ConfigProvider(DiscordVerifier barricade) {
        this.verifier = barricade;
    }

    public void loadConfig() {
        Path dataDir = verifier.getDataDirectory();
        if (!dataDir.toFile().exists()) dataDir.toFile().mkdirs();

        Yaml yaml = new Yaml();
        File configFile = new File(dataDir.toString(), "config.yml");
        if (!configFile.exists()) {
            InputStream inputStream = verifier
                    .getClass()
                    .getClassLoader()
                    .getResourceAsStream("config.yml");
            try (OutputStream outputStream = new FileOutputStream(configFile, false)) {
                int read;
                byte[] bytes = new byte[8192];
                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            } catch (IOException e) {
                LogUtils.error("Config file could not be created.");
                e.printStackTrace();
            }
        } else {
            try {
                File file = new File(dataDir.toString(), "config.yml");
                DumperOptions options = new DumperOptions();
                options.setAllowUnicode(true);
                options.setIndent(2);
                options.setPrettyFlow(true);
                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                Yaml yamlToWrite = new Yaml(options);
                InputStream inputStream = new FileInputStream(file);
                Map<String, Object> map = yamlToWrite.load(inputStream);
                inputStream.close();
                InputStream targetInputStream = verifier
                        .getClass()
                        .getClassLoader()
                        .getResourceAsStream("config.yml");
                Map<String, Object> targetMap = yamlToWrite.load(targetInputStream);

                Optional<Map<String, Object>> updatedOptional = updateMap(map, targetMap);
                if (updatedOptional.isPresent()) {
                    OutputStreamWriter writer =
                            new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
                    yamlToWrite.dump(updatedOptional.get(), writer);
                }
            } catch (IOException exception) {
                LogUtils.error("Config file could not be updated.");
                exception.printStackTrace();
            }
        }

        try {
            FileInputStream fileInputStream = new FileInputStream(configFile);
            config = new HashMap<>(yaml.load(fileInputStream));
            fileInputStream.close();
        } catch (IOException e) {
            LogUtils.error("Config file could not be loaded.");
            e.printStackTrace();
        }
    }

    protected String getString(String key) {
        Object value = getValue(key);
        return value != null ? (String) value : null;
    }

    protected int getInt(String key) {
        Object value = getValue(key);
        return value != null ? (int) value : -1;
    }

    protected boolean getBoolean(String key) {
        Object value = getValue(key);
        return value != null && (boolean) value;
    }

    @SuppressWarnings("unchecked")
    protected List<String> getStringList(String key) {
        Object value = getValue(key);
        return value != null ? (List<String>) value : null;
    }

    @SuppressWarnings("unchecked")
    protected LinkedHashMap<String, ?> getMap(String key) {
        Object value = getValue(key);
        return value != null ? (LinkedHashMap<String, ?>) value : null;
    }

    private Object getValue(String key) {
        if (key.contains(".")) {
            return getNestedValue(key);
        } else {
            return config.get(key);
        }
    }

    @SuppressWarnings("unchecked")
    private Object getNestedValue(String key) {
        String[] abstractKey = key.split("\\.");
        if (!config.containsKey(abstractKey[0])) return null;
        Map<String, ?> map = getMap(abstractKey[0]);
        int i = 2;
        for (String s : Arrays.stream(abstractKey).skip(1).toList()) {
            if (map.containsKey(s) && (!(map.get(s) instanceof Map)
                    || i == abstractKey.length)) {
                return map.get(s);
            } else if (map.containsKey(s)) {
                map = (Map<String, ?>) map.get(s);
            }
            i++;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Optional<Map<String, Object>> updateMap(
            Map<String, Object> map, Map<String, Object> targetMap) {
        Map<String, Object> temporaryMap = new HashMap<>(map);
        for (String targetKey : targetMap.keySet()) {
            if (targetMap.get(targetKey) instanceof Map nestedMap) {
                if (!map.containsKey(targetKey) || !(map.get(targetKey) instanceof Map)) {
                    temporaryMap.put(targetKey, targetMap.get(targetKey));
                    continue;
                }
                Optional<Map<String, Object>> nestedMapOptional =
                        updateMap((Map<String, Object>) map.get(targetKey),
                                (Map<String, Object>) nestedMap);
                nestedMapOptional.ifPresent(stringObjectMap ->
                        temporaryMap.put(targetKey, stringObjectMap));
            } else if (!map.containsKey(targetKey)) {
                temporaryMap.put(targetKey, targetMap.get(targetKey));
            }
        }
        if (!map.equals(temporaryMap)) return Optional.of(temporaryMap);
        return Optional.empty();
    }

}
