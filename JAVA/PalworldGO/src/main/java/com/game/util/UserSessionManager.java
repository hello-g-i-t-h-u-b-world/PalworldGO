package com.game.util;

import com.game.model.User;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserSessionManager {
    // 여러 사용자가 동시에 로그인/조회해도 메모리 충돌이 없는 Thread-safe한 Map 구조 사용
    private static final Map<Integer, User> activeSessions = new ConcurrentHashMap<>();


    // 로그인 성공 시 유저 정보를 메모리 세션에 등록
    public static void registerSession(User user) {
        if (user != null) {
            activeSessions.put(user.id, user);
            System.out.println("[세션 등록] 유저 ID: " + user.id + " (" + user.username + ") 로그인 정보 캐싱 완료");
        }
    }

    // 데이터베이스 접근 없이 메모리에서 유저 정보 즉시 획득 (미구현)
    public static User getUserSession(int userId) {
        return activeSessions.get(userId);
    }

    // 로그아웃 또는 계정 탈퇴 시 세션 무효화 (미구현)
    public static void removeSession(int userId) {
        activeSessions.remove(userId);
        System.out.println("[세션 만료] 유저 ID: " + userId + " 세션 제거 완료");
    }
}