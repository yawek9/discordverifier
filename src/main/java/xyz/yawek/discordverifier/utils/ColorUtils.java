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

package xyz.yawek.discordverifier.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {

    public static Component decorate(Component input) {
        TextComponent.Builder outputBuilder = Component.text();
        String stringInput  = PlainTextComponentSerializer.plainText().serialize(input);
        Pattern colorOrGradientPattern = Pattern
                .compile("(&#gr#[A-Fa-f0-9]{6}#[A-Fa-f0-9]{6})|(&#[A-Fa-f0-9]{6})");
        Matcher colorOrGradientMatcher = colorOrGradientPattern.matcher(stringInput);
        Pattern nextTextPattern = Pattern.compile(".+?(?=&#)", Pattern.DOTALL);
        while (colorOrGradientMatcher.find()) {
            String group = colorOrGradientMatcher.group();
            boolean isGradient = group.length() > 10;
            if (isGradient) {
                Matcher gradientMatcher = Pattern.compile("&#gr#[A-Fa-f0-9]{6}#[A-Fa-f0-9]{6}")
                        .matcher(stringInput);
                if (!gradientMatcher.find()) {
                    continue;
                }
                String gradientGroup = gradientMatcher.group();
                stringInput = stringInput.replaceFirst(gradientGroup, "");
                Matcher nextColorMatcher = nextTextPattern.matcher(stringInput);
                String toGradient;
                if (nextColorMatcher.find()) {
                    toGradient = nextColorMatcher.group();
                } else {
                    toGradient = stringInput;
                }
                if (colorOrGradientPattern.matcher(toGradient).find()) {
                    continue;
                }
                TextDecoration decoration = retrieveDecoration(toGradient);
                stringInput = stringInput.replaceFirst(toGradient, "");
                outputBuilder.append(makeGradient(
                        retrieveFirstColor(gradientGroup),
                        retrieveFirstColor(gradientGroup.replaceFirst("#[A-Fa-f0-9]{6}", "")),
                        toGradient.replaceAll("&[K-ok-o]", ""),
                        decoration));
            } else {
                Matcher singleColorMatcher = Pattern.compile("&#[A-Fa-f0-9]{6}").matcher(stringInput);
                if (!singleColorMatcher.find()) {
                    continue;
                }
                String colorGroup = singleColorMatcher.group();
                stringInput = stringInput.replaceFirst(colorGroup, "");
                Matcher nextColorMatcher = nextTextPattern.matcher(stringInput);
                String toColor;
                if (nextColorMatcher.find()) {
                    toColor = nextColorMatcher.group();
                } else {
                    toColor = stringInput;
                }
                if (colorOrGradientPattern.matcher(toColor).find()) {
                    continue;
                }
                TextDecoration decoration = retrieveDecoration(toColor);
                Component toAppend = Component.text(toColor.replaceAll("&[K-ok-o]", ""))
                        .color(TextColor.fromHexString(colorGroup.replace("&", "")));
                if (decoration != null) toAppend = toAppend.decorate(decoration);
                outputBuilder.append(toAppend);
                stringInput = stringInput.replaceFirst(toColor, "");
            }
        }
        return makeUrlsClickable(outputBuilder.build());
    }

    private static Component makeGradient(Color firstColor, Color secondColor,
                                          String text, @Nullable TextDecoration textDecoration) {
        int length = text.replaceAll(" ", "").length();
        double[] rColor = interpolateColor(firstColor.getRed(), secondColor.getRed(), length);
        double[] gColor = interpolateColor(firstColor.getGreen(), secondColor.getGreen(), length);
        double[] bColor = interpolateColor(firstColor.getBlue(), secondColor.getBlue(), length);

        TextComponent.Builder outputBuilder = Component.text();

        int i = 0;
        for (char c : text.toCharArray()) {
            if (c == ' ') {outputBuilder.append(Component.text(" ")); continue;}
            Color color = new Color(
                    (int) Math.round(rColor[i]),
                    (int) Math.round(gColor[i]),
                    (int) Math.round(bColor[i]));
            Component toAppend = Component.text(c).color(TextColor.fromCSSHexString(
                    "#" + Integer.toHexString(color.getRGB()).substring(2)));
            if (textDecoration != null) toAppend = toAppend.decorate(textDecoration);
            outputBuilder.append(toAppend);
            i++;
        }
        return outputBuilder.build();
    }

    public static Component makeUrlsClickable(Component component) {
        Pattern urlPattern = Pattern.compile(
                "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\." +
                        "[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)");
        TextReplacementConfig textReplacementConfig = TextReplacementConfig.builder()
                .replacement((matchResult, builder) -> Component.text(matchResult.group())
                        .clickEvent(ClickEvent.openUrl(matchResult.group())))
                .match(urlPattern).build();
        return component.replaceText(textReplacementConfig);
    }

    public String stripColor(String string) {
        return string.replaceAll("(&#gr#[A-Fa-f0-9]{6}#[A-Fa-f0-9]{6})" +
                "|(&#[A-Fa-f0-9]{6})", "");
    }

    public static String stripDecoration(String string) {
        if (containsDecoration(string)) {
            string = string.replaceAll("&[K-ok-o]", "");
        }
        return string;
    }

    private static Color retrieveFirstColor(String string) {
        Matcher colorMatcher = Pattern.compile("#[A-Fa-f0-9]{6}").matcher(string);
        colorMatcher.find();
        return Color.decode(colorMatcher.group());
    }

    private static TextDecoration retrieveDecoration(String string) {
        Matcher decorationMatcher = Pattern.compile("&[K-ok-o]").matcher(string);
        if (decorationMatcher.find()) {
            return getDecoration(decorationMatcher.group());
        }
        return null;
    }

    private static boolean containsDecoration(String string) {
        return new ArrayList<>(Arrays.asList("&k", "&l", "&m", "&n", "&o"))
                .stream().anyMatch(string::contains);
    }

    private static TextDecoration getDecoration(String string) {
        return switch (string) {
            case "&k" -> TextDecoration.OBFUSCATED;
            case "&l" -> TextDecoration.BOLD;
            case "&m" -> TextDecoration.STRIKETHROUGH;
            case "&n" -> TextDecoration.UNDERLINED;
            case "&o" -> TextDecoration.ITALIC;
            default -> null;
        };
    }

    private static double[] interpolateColor(double from, double to, int max) {
        final double[] res = new double[max];
        for (int i = 0; i < max; i++) {
            res[i] = from + i * ((to - from) / (max - 1));
        }
        return res;
    }

}
