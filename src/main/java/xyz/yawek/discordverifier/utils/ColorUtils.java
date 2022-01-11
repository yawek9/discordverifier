package xyz.yawek.discordverifier.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.util.HSVLike;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {

    public static Component decorate(Component component) {
        if (!PlainTextComponentSerializer.plainText().serialize(component).contains("&")) {
            return makeUrlsClickable(component);
        }

        try {
            String message = PlainTextComponentSerializer.plainText().serialize(component);
            message = message.replaceAll("&#gr&#([A-Fa-f0-9]{6})|&#gr&#rb", "");

            Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6}|rb|gr)");
            Matcher matcher = hexPattern.matcher(message);

            Component output = Component.empty();

            int numberOfParts = message.split("&#([A-Fa-f0-9]{6}|rb|gr)").length;

            String[] array = new String[numberOfParts];
            TextColor[] textArray = new TextColor[numberOfParts + 1];
            int[] rgbArray = new int[numberOfParts + 1];
            int[] gradientArray = new int[numberOfParts + 1];

            int i = 0;
            for (String s : message.split("&#([A-Fa-f0-9]{6}|rb|gr)")) {
                array[i] = s;
                i++;
            }

            i = 0;
            while (matcher.find()) {
                if (matcher.group(1).equalsIgnoreCase("rb")) {
                    rgbArray[i] = 1;
                    textArray[i] = TextColor.fromCSSHexString("#fffff");
                    gradientArray[i] = 0;
                } else if (matcher.group(1).equalsIgnoreCase("gr")) {
                    rgbArray[i] = 0;
                    textArray[i] = TextColor.fromCSSHexString("#fffff");
                    gradientArray[i] = 1;
                } else {
                    rgbArray[i] = 0;
                    textArray[i] = TextColor.fromCSSHexString("#" + matcher.group(1));
                    gradientArray[i] = 0;
                }
                i++;
            }

            i = 0;
            boolean bool = true;
            for (String s : array) {
                if (bool) {
                    bool = false;
                    continue;
                }

                if (containsStyleDecoration(s)) {
                    Pattern decoration = Pattern.compile("&([K-ok-o])");
                    Matcher decorationMatcher = decoration.matcher(s);
                    s = s.replaceAll("&([K-ok-o])", "");
                    TextDecoration textDecoration = null;

                    while (decorationMatcher.find()) {
                        String string = decorationMatcher.group(1);
                        textDecoration = getDecorationStyle("&" + string);
                    }

                    if (rgbArray[i] == 1) {
                        Component color = Component.empty();
                        int j = 0;
                        String noSpaces = s.replace(" ", "");
                        for (char c : s.toCharArray()) {
                            HSVLike hsvLike = HSVLike.of(j * (1F / noSpaces.length()),
                                    1F, 1F);
                            if (j == 0) {
                                color = Component.text(c).color(TextColor.color(hsvLike)).decorate(textDecoration);
                            } else {
                                color = color.append(
                                        Component.text(c).color(TextColor.color(hsvLike)).decorate(textDecoration)
                                );
                            }

                            if (c != ' ') j++;
                        }
                        output = output.append(color);
                    } else if (gradientArray[i] == 1) {
                        Pattern decorationGradient = Pattern.compile("#([A-Fa-f0-9]{6})");
                        Matcher decorationMatcherGradient = decorationGradient.matcher(s);
                        Color from = null;
                        Color to = null;
                        boolean first = true;
                        String notReplaced = s;
                        while (decorationMatcherGradient.find()) {
                            s = s.replace("#" + decorationMatcherGradient.group(1), "");
                            if (first) {
                                from = Color.decode("#" + decorationMatcherGradient.group(1));
                                first = false;
                            } else {
                                to = Color.decode("#" + decorationMatcherGradient.group(1));
                            }
                        }
                        if (from == null || to == null) return makeUrlsClickable(
                                output.append(Component.text(notReplaced))
                        );

                        double[] red = interpolate(from.getRed(), to.getRed(), s.length());
                        double[] green = interpolate(from.getGreen(), to.getGreen(), s.length());
                        double[] blue = interpolate(from.getBlue(), to.getBlue(), s.length());

                        Component color = Component.empty();
                        for (int k = 0; k < s.length(); k++) {
                            Color c = new Color(
                                    (int) Math.round(red[k]),
                                    (int) Math.round(green[k]),
                                    (int) Math.round(blue[k])
                            );
                            if (k == 0) {
                                color = Component.text(s.charAt(k)).color(TextColor.fromHexString("#"
                                        + Integer.toHexString(c.getRGB()).substring(2))).decorate(textDecoration);
                            } else {
                                color = color.append(Component.text(s.charAt(k)).color(TextColor.fromHexString("#"
                                        + Integer.toHexString(c.getRGB()).substring(2))).decorate(textDecoration));
                            }
                        }
                        if (s.length() == 1) {
                            color = Component.text(s).color(TextColor.fromHexString(
                                    "#" + Integer.toHexString(to.getRGB()).substring(2)));
                        }
                        output = output.append(color);
                    } else {
                        output = output.append(Component.text(s).color(textArray[i]).decorate(textDecoration));
                    }
                } else {
                    if (gradientArray[i] == 1) {
                        Pattern decoration = Pattern.compile("#([A-Fa-f0-9]{6})");
                        Matcher decorationMatcher = decoration.matcher(s);
                        Color from = null;
                        Color to = null;
                        boolean first = true;
                        String notReplaced = s;
                        while (decorationMatcher.find()) {
                            s = s.replace("#" + decorationMatcher.group(1), "");
                            if (first) {
                                from = Color.decode("#" + decorationMatcher.group(1));
                                first = false;
                            } else {
                                to = Color.decode("#" + decorationMatcher.group(1));
                            }
                        }
                        if (from == null || to == null) return makeUrlsClickable(
                                output.append(Component.text(notReplaced))
                        );

                        double[] red = interpolate(from.getRed(), to.getRed(), s.length());
                        double[] green = interpolate(from.getGreen(), to.getGreen(), s.length());
                        double[] blue = interpolate(from.getBlue(), to.getBlue(), s.length());

                        Component color = Component.empty();
                        for (int k = 0; k < s.length(); k++) {
                            Color c = new Color(
                                    (int) Math.round(red[k]),
                                    (int) Math.round(green[k]),
                                    (int) Math.round(blue[k])
                            );
                            if (k == 0) {
                                color = Component.text(s.charAt(k)).color(TextColor.fromHexString("#"
                                        + Integer.toHexString(c.getRGB()).substring(2)));
                            } else {
                                color = color.append(Component.text(s.charAt(k)).color(TextColor.fromHexString("#"
                                        + Integer.toHexString(c.getRGB()).substring(2))));
                            }
                        }
                        if (s.length() == 1) {
                            color = Component.text(s).color(TextColor.fromHexString(
                                    "#" + Integer.toHexString(to.getRGB()).substring(2)));
                        }
                        output = output.append(color);
                    } else if (rgbArray[i] == 1) {
                        Component color = Component.empty();
                        int j = 0;
                        String noSpaces = s.replace(" ", "");
                        for (char c : s.toCharArray()) {
                            HSVLike hsvLike = HSVLike.of(j * (1F / noSpaces.length()),
                                    0.9F, 0.9F);
                            if (j == 0) {
                                color = Component.text(c).color(TextColor.color(hsvLike));
                            } else {
                                color = color.append(Component.text(c).color(TextColor.color(hsvLike)));
                            }

                            if (c != ' ') j++;
                        }
                        output = output.append(color);
                    } else {
                        output = output.append(Component.text(s).color(textArray[i]));
                    }
                }
                i++;
            }

            return makeUrlsClickable(output);
        } catch (Exception e) {
            e.printStackTrace();
            return makeUrlsClickable(component);
        }
    }

    public static Component makeUrlsClickable(Component component) {
        Pattern urlPattern = Pattern.compile(
                "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\." +
                        "[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)"
        );

        TextReplacementConfig textReplacementConfig = TextReplacementConfig.builder()
                .replacement((matchResult, builder) -> Component.text(matchResult.group())
                        .clickEvent(ClickEvent.openUrl(matchResult.group()))).match(urlPattern).build();

        return component.replaceText(textReplacementConfig);
    }

    public String stripColor(String string) {
        if (string != null && !string.equals("")) {
            Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
            Matcher matcher = hexPattern.matcher(string);

            while (matcher.find()) {
                String s = matcher.group(1);

                string = string.replace("&#" + s, "");
            }

            Pattern rbPattern = Pattern.compile("&#rb");
            Matcher rbMatcher = rbPattern.matcher(string);

            while (rbMatcher.find()) {
                string = string.replace("&#rb", "");
            }

            Pattern grPattern = Pattern.compile("&#gr#([A-Fa-f0-9]{6})#([A-Fa-f0-9]{6})|&#gr");
            Matcher grMatcher = grPattern.matcher(string);

            while (grMatcher.find()) {
                string = string.replaceAll("&#gr#([A-Fa-f0-9]{6})#([A-Fa-f0-9]{6})|&#gr", "");
            }

            return string;
        } else {
            return null;
        }
    }

    public static String stripTextDecoration(String string) {
        if (containsStyleDecoration(string)) {
            string = string.replaceAll("&([K-ok-o])", "");
        }
        return string;
    }

    private static boolean containsStyleDecoration(String string) {
        return new ArrayList<>(Arrays.asList("&k", "&l", "&m", "&n", "&o"))
                .stream().anyMatch(string::contains);
    }

    private static TextDecoration getDecorationStyle(String string) {
        return switch (string) {
            case "&k" -> TextDecoration.OBFUSCATED;
            case "&l" -> TextDecoration.BOLD;
            case "&m" -> TextDecoration.STRIKETHROUGH;
            case "&n" -> TextDecoration.UNDERLINED;
            case "&o" -> TextDecoration.ITALIC;
            default -> null;
        };
    }

    private static double[] interpolate(double from, double to, int max) {
        final double[] res = new double[max];
        for (int i = 0; i < max; i++) {
            res[i] = from + i * ((to - from) / (max - 1));
        }
        return res;
    }

}
