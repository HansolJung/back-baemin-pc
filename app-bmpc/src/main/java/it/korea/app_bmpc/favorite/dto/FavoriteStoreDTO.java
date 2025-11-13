package it.korea.app_bmpc.favorite.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import it.korea.app_bmpc.common.utils.GeoUtils;
import it.korea.app_bmpc.common.utils.TimeFormatUtils;
import it.korea.app_bmpc.favorite.entity.FavoriteStoreEntity;
import it.korea.app_bmpc.store.dto.StoreFileDTO;
import it.korea.app_bmpc.store.dto.StoreHourDTO;
import it.korea.app_bmpc.store.entity.StoreEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class FavoriteStoreDTO {

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Response {
        private int favoriteId;
        private String userId;
        private int storeId;
        private String storeName;
        private String addr;
        private String fileName;
        private String storedName;
        private String filePath;
        private String fileThumbName;
        private BigDecimal ratingAvg;
        private boolean isOpen;
        private String hourComment;
        private boolean isAround;

        public static Response of(FavoriteStoreEntity entity, double userLatitude, double userLongitude) {

            StoreEntity store = entity.getStore();

            // 파일 엔티티를 파일 DTO 로 객체 변환
            // 바로 이때 파일 리스트가 SELECT 된다.
            List<StoreFileDTO> fileList = 
                store.getFileList().stream().map(StoreFileDTO::of).filter((file)-> 
                    file.getMainYn().equals("Y")).toList();
            
            StoreFileDTO mainImage = null;

            if (fileList != null && fileList.size() > 0) {
                mainImage = fileList.get(0);
            }

            // 오늘 요일 구하기 (1=월, 2=화, ... 7=일)
            int today = LocalDate.now().getDayOfWeek().getValue();
            // 지금 시간 구하기
            LocalTime now = LocalTime.now();
            
            Optional<StoreHourDTO> optionalDto = 
                store.getHourList().stream().filter((hour) ->
                    hour.getDayOfWeek() == today).map(StoreHourDTO::of).findFirst();

            String hourComment = "";
            boolean isOpen = true;

            // 영업 여부, 영업 코멘트 세팅하기
            if (optionalDto.isPresent()) {
                StoreHourDTO dto = optionalDto.get();

                LocalTime openTime = dto.getOpenTime();
                LocalTime closeTime = dto.getCloseTime();

                if ("Y".equals(dto.getCloseYn())) {  // 만약 오늘이 휴무날이면...
                    hourComment = "오늘 휴무";
                    isOpen = false;
                } else if (now.isBefore(openTime)) { // 만약 지금이 오픈 시간 이전일경우...
                    hourComment = "오픈 전 (오늘 " + TimeFormatUtils.getTime(openTime, "HH:mm") + " 오픈)";
                    isOpen = false;
                } else if (now.isAfter(closeTime)) { // 만약 지금이 마감 시간 이후일경우...
                    hourComment = "영업 종료 (오늘 " + TimeFormatUtils.getTime(closeTime, "HH:mm") + " 마감)";
                    isOpen = false;
                }
            }

            // 지도 API 오류로 인해 위도/경도 계산이 안됐다면 그냥 찜 목록에 보여지게 하기 위해서 true를 기본값으로 설정
            boolean isAround = true;

            // 해당 가게가 반경 4km 안에 있는지 계산 (사용자 위도/경도, 가게 위도/경도 값이 모두 정상적으로 있을 경우에만)
            if (userLatitude > 0 && userLongitude > 0 && 
                store.getLatitude() != null && store.getLongitude() != null) {

                double distanceKm = 
                    GeoUtils.distance(userLatitude, userLongitude, store.getLatitude().doubleValue(), store.getLongitude().doubleValue());
                
                isAround = distanceKm <= 4.0; // 반경 4km 안에 위치한 가게라면 true
            }

            return Response.builder()
                .favoriteId(entity.getFavoriteId())
                .userId(entity.getUser().getUserId())
                .storeId(store.getStoreId())
                .storeName(store.getStoreName())
                .addr(store.getAddr())
                .fileName(mainImage != null ? mainImage.getFileName() : null)
                .storedName(mainImage != null ? mainImage.getStoredName() : null)
                .filePath(mainImage != null ? mainImage.getFilePath() : null)
                .fileThumbName(mainImage != null ? mainImage.getFileThumbName() : null)
                .ratingAvg(store.getRatingAvg())
                .isOpen(isOpen)
                .hourComment(hourComment)
                .isAround(isAround)
                .build();
        }
    }

    @Data
    public static class Search {
        @NotBlank(message = "찜 목록을 가져올 때 사용자 주소는 필수값입니다.")
        private String addr;   // 사용자 주소
    }


    @Data
    public static class Request {

        @NotBlank(message = "사용자 아이디는 필수 항목입니다.")
        private String userId;
        @NotNull(message = "가게 아이디는 필수 항목입니다.")
        private Integer storeId;
    }
}
