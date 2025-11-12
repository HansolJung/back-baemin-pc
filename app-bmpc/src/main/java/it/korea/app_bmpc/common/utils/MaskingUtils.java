package it.korea.app_bmpc.common.utils;

public class MaskingUtils {

    /**
     * 사용자 아이디 마스킹 처리하기
     * 예) "user1234" -> "use****"
     * @param userId 사용자 아이디
     * @return
     */
    public static String maskingUserId(String userId) {
        if (userId == null || userId.length() < 3) {
            return "****";
        }

        int length = Math.min(3, userId.length());
        String visibleStr = userId.substring(0, length);
        String maskedStr = "*".repeat(userId.length() - length);

        System.out.println("테스트!");
        System.out.println(visibleStr + maskedStr);

        return visibleStr + maskedStr;
    }
}
