package it.korea.app_bmpc.common.utils;

public class GeoUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;   // 지구 반지름

    /**
     * Haversine 공식을 이용해서 거리 계산
     * @param lat1 위도1
     * @param lon1 경도1
     * @param lat2 위도2
     * @param lon2 경도2
     * @return
     */
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double latDiff = Math.toRadians(lat2 - lat1);
        double lonDiff = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(lonDiff / 2) * Math.sin(lonDiff / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}
