package com.game.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

public class WeatherService {

    private static final String API_KEY = "67ebb2c704a13e260265603417f873ed";

    public static String getWeather(double lat, double lng) {
        String selectedWeather = "Clear";

        try {
            String urlStr = "https://api.openweathermap.org/data/2.5/weather?lat="
                    + lat + "&lon=" + lng + "&appid=" + API_KEY;

            // AI 피셜) URI를 먼저 거친 후 URL로 변환하는 방식을 권장합니다.
            URL url = java.net.URI.create(urlStr).toURL();

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line;

            StringBuilder sb = new StringBuilder();

            while((line = br.readLine()) != null) {
                sb.append(line);
            }

            br.close();

            // JSON 응답 데이터를 파싱합니다.
            JSONObject json = new JSONObject(sb.toString());

            // 추출한 날씨 데이터를 요청하신 'selectWeather' 변수에 할당합니다.
            selectedWeather = json.getJSONArray("weather").getJSONObject(0).getString("main");

        } catch(Exception e) {
            e.printStackTrace();
        }

        System.out.println("현재 테스트 날씨: "+ selectedWeather);

        return selectedWeather;
    }
}

// 초기 api 테스트 코드
//import java.util.Random;
//
//public class WeatherService {
//
//    private static final String[] weathers = {
//            "Clear",         // 맑음
//            "Rain",          // 비
//            "Clouds",        // 흐림
//            "Thunderstorm"   // 천둥
//    };
//
//    private static final Random random =
//            new Random();
//
//    public static String getWeather(
//            double lat,
//            double lng
//    ) {
//
//        String selectedWeather =
//                weathers[random.nextInt(weathers.length)];
//
//        System.out.println(
//                "현재 테스트 날씨: "
//                + selectedWeather
//        );
//
//        return selectedWeather;
//    }
//}