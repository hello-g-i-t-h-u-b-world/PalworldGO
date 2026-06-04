package com.game.util;

public class BoostedTypeManager {

    public static String getBoostedType(String weather) {
        if (weather == null) {
            return "";
        }

        switch(weather) {
            case "Rain":
                return "물";

            case "Clear":
                return "불";

            case "Clouds":
                return "풀";

            case "Thunderstorm":
                return "전기";

            default:
                return "";
        }
    }
}