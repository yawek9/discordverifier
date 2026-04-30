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

package xyz.yawek.discordverifier.util;

import static xyz.yawek.discordverifier.util.ColorUtilsConstants.COLOR_DECORATION_FORMAT;
import static xyz.yawek.discordverifier.util.ColorUtilsConstants.COLOR_FORMAT;
import static xyz.yawek.discordverifier.util.ColorUtilsConstants.DECORATION_FORMAT;
import static xyz.yawek.discordverifier.util.ColorUtilsConstants.DECORATION_MAP;
import static xyz.yawek.discordverifier.util.ColorUtilsConstants.LEGACY_COLOR_MAP;
import static xyz.yawek.discordverifier.util.ColorUtilsConstants.URL_FORMAT;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ColorUtils {

    public static Component decorate(@NotNull String plainText) {
        Matcher colorMatcher = Pattern.compile(COLOR_DECORATION_FORMAT, Pattern.DOTALL)
            .matcher(plainText);
        TextComponent.Builder resultBuilder = Component.text();

        int lastEnd = 0;
        while (colorMatcher.find()) {
            // If matcher leaves some leftovers before first occurrence of formatting,
            // we append it as first plain component
            if (colorMatcher.start() > lastEnd) {
                resultBuilder.append(
                    Component.text(plainText.substring(lastEnd, colorMatcher.start())));
            }

            String partText = colorMatcher.group(4);
            String colorString = colorMatcher.group(1);

            // Skip empty matches (when colors are directly adjacent)
            if (partText.isEmpty()) {
                lastEnd = colorMatcher.end();
                continue;
            }

            Component partComponent;
            boolean isGradient = colorMatcher.group(2) != null;
            if (isGradient) {
                String secondColorString = colorMatcher.group(2);
                partComponent = makeGradient(Color.decode(colorString),
                    Color.decode(secondColorString), partText);
            } else {
                TextColor color;
                if (colorString.length() == 1) {
                    color = LEGACY_COLOR_MAP.get(colorString.charAt(0));
                } else {
                    color = TextColor.fromCSSHexString(colorString);
                }
                partComponent = Component.text(partText).color(color);
            }
            TextDecoration decoration = getDecoration(colorMatcher.group(3));
            if (decoration != null) {
                partComponent = partComponent.decorate(decoration);
            }
            resultBuilder.append(partComponent);
            lastEnd = colorMatcher.end();
        }
        // If text has no formatting at all, we append it as plain component
        if (lastEnd < plainText.length()) {
            resultBuilder.append(Component.text(plainText.substring(lastEnd)));
        }
        return makeUrlsClickable(resultBuilder.build());
    }

    public static Component decorate(Component component) {
        return decorate(PlainTextComponentSerializer.plainText().serialize(component));
    }

    public static Component decorate(String input, Object... replacements) {
        if (replacements.length % 2 != 0) {
            throw new IllegalArgumentException("Replacements must be provided in pairs "
                + "(placeHolder, replacement)");
        }

        Component result = ColorUtils.decorate(input);
        String workingInput = input;
        for (int i = 0; i < replacements.length; i += 2) {
            String placeHolder = (String) replacements[i];
            Object replacement = replacements[i + 1];

            if (!(replacement instanceof Component)) {
                workingInput = workingInput.replace("%" + placeHolder + "%",
                    String.valueOf(replacement));
            }
        }

        if (!workingInput.equals(input)) {
            result = ColorUtils.decorate(workingInput);
        }

        for (int i = 0; i < replacements.length; i += 2) {
            String placeHolder = (String) replacements[i];
            Object replacement = replacements[i + 1];

            if (replacement instanceof Component replacementComponent) {
                result = result.replaceText(TextReplacementConfig.builder()
                    .matchLiteral("%" + placeHolder + "%")
                    .replacement(replacementComponent)
                    .build());
            }
        }
        return result;
    }

    private static Component makeGradient(Color firstColor, Color secondColor, String text) {
        int length = text.replaceAll(" ", "").length();
        double[] rColor = interpolateColor(firstColor.getRed(), secondColor.getRed(), length);
        double[] gColor = interpolateColor(firstColor.getGreen(), secondColor.getGreen(), length);
        double[] bColor = interpolateColor(firstColor.getBlue(), secondColor.getBlue(), length);

        TextComponent.Builder resultBuilder = Component.text();
        int i = 0;
        for (char c : text.toCharArray()) {
            if (c == ' ') {
                resultBuilder.append(Component.text(" "));
                continue;
            }
            Color color = new Color((int) Math.round(rColor[i]),
                (int) Math.round(gColor[i]), (int) Math.round(bColor[i]));
            Component toAppend = Component.text(c).color(TextColor.fromCSSHexString(
                "#" + Integer.toHexString(color.getRGB()).substring(2)));
            resultBuilder.append(toAppend);
            i++;
        }
        return resultBuilder.build();
    }

    public static Component makeUrlsClickable(Component component) {
        Pattern urlPattern = Pattern.compile(URL_FORMAT);
        TextReplacementConfig textReplacementConfig = TextReplacementConfig.builder()
            .replacement((matchResult, builder) -> Component.text(matchResult.group())
                .clickEvent(ClickEvent.openUrl(matchResult.group())))
            .match(urlPattern)
            .build();
        return component.replaceText(textReplacementConfig);
    }

    public static String stripColor(String string) {
        if (string == null) {
            return null;
        }
        return string.replaceAll(COLOR_FORMAT, "");
    }

    public static String stripDecoration(String string) {
        if (!containsDecoration(string)) {
            return string;
        }
        return string.replaceAll(DECORATION_FORMAT, "");
    }

    private static boolean containsDecoration(String string) {
        if (string == null) {
            return false;
        }
        return DECORATION_MAP.keySet().stream().anyMatch(string::contains);
    }

    private static @Nullable TextDecoration getDecoration(String string) {
        if (string == null) {
            return null;
        }
        return DECORATION_MAP.getOrDefault(string, null);
    }

    private static double[] interpolateColor(double from, double to, int max) {
        final double[] res = new double[max];
        IntStream.rangeClosed(0, max - 1).forEach(i -> res[i] = from + i * ((to - from) / (max - 1)));
        return res;
    }

}
