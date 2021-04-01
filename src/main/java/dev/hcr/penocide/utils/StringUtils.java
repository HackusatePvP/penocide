package dev.hcr.penocide.utils;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {


    public StringUtils() {
        throw new UnsupportedOperationException("Cannot initiate a util class.");
    }

    public static String format(String line) {
        return ChatColor.translateAlternateColorCodes('&', line);
    }

    public static List<String> format(List<String> lines) {
        List<String> toReturn = new ArrayList<>(lines);
        toReturn.forEach(StringUtils::format);
        return toReturn;
    }
}
