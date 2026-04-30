/*
 * This file is part of BukkitUtils, licensed under GNU GPLv3 license.
 * Copyright (C) 2025 impl
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

import java.util.Map;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

class ColorUtilsConstants {

  static final String COLOR_FORMAT = "&(#[0-9a-fA-F]{3,6}|[0-9a-fA-F])(?:-#[0-9a-fA-F]{3,6})?";
  static final String DECORATION_FORMAT = "&[KLMNOklmno]";
  static final String COLOR_DECORATION_FORMAT =
      "&(#[0-9a-fA-F]{3,6}|[0-9a-fA-F])(?:-(#[0-9a-fA-F]{3,6}))?(&[KLMNOklmno])"
          + "?(.*?)(?=&#[0-9a-fA-F]{3,6}|&[0-9a-fA-F]|&[KLMNOklmno]|$)";
  static final String URL_FORMAT = "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}"
      + "\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)";

  static final Map<Character, NamedTextColor> LEGACY_COLOR_MAP = Map.ofEntries(
      Map.entry('0', NamedTextColor.BLACK),
      Map.entry('1', NamedTextColor.DARK_BLUE),
      Map.entry('2', NamedTextColor.DARK_GREEN),
      Map.entry('3', NamedTextColor.DARK_AQUA),
      Map.entry('4', NamedTextColor.DARK_RED),
      Map.entry('5', NamedTextColor.DARK_PURPLE),
      Map.entry('6', NamedTextColor.GOLD),
      Map.entry('7', NamedTextColor.GRAY),
      Map.entry('8', NamedTextColor.DARK_GRAY),
      Map.entry('9', NamedTextColor.BLUE),
      Map.entry('a', NamedTextColor.GREEN),
      Map.entry('b', NamedTextColor.AQUA),
      Map.entry('c', NamedTextColor.RED),
      Map.entry('d', NamedTextColor.LIGHT_PURPLE),
      Map.entry('e', NamedTextColor.YELLOW),
      Map.entry('f', NamedTextColor.WHITE),
      Map.entry('A', NamedTextColor.GREEN),
      Map.entry('B', NamedTextColor.AQUA),
      Map.entry('C', NamedTextColor.RED),
      Map.entry('D', NamedTextColor.LIGHT_PURPLE),
      Map.entry('E', NamedTextColor.YELLOW),
      Map.entry('F', NamedTextColor.WHITE)
  );

  static final Map<String, TextDecoration> DECORATION_MAP = Map.of(
      "&k", TextDecoration.OBFUSCATED,
      "&K", TextDecoration.OBFUSCATED,
      "&l", TextDecoration.BOLD,
      "&L", TextDecoration.BOLD,
      "&m", TextDecoration.STRIKETHROUGH,
      "&M", TextDecoration.STRIKETHROUGH,
      "&n", TextDecoration.UNDERLINED,
      "&N", TextDecoration.UNDERLINED,
      "&o", TextDecoration.ITALIC,
      "&O", TextDecoration.ITALIC
  );

}
