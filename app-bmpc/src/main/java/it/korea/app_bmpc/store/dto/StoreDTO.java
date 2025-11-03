package it.korea.app_bmpc.store.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonFormat;

import it.korea.app_bmpc.common.utils.TimeFormatUtils;
import it.korea.app_bmpc.menu.dto.MenuCategoryDTO;
import it.korea.app_bmpc.store.entity.StoreEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class StoreDTO {

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Response {
        private int storeId;
        private String storeName;
        private String branchName;
        private String phone;
        private String addr;
        private String addrDetail;
        private BigDecimal ratingAvg;
        private int reviewCount;
        private String delYn;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createDate;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updateDate;
        private String fileName;
        private String storedName;
        private String filePath;
        private String fileThumbName;
        private boolean isOpen;
        private String hourComment;
        
        public static Response of(StoreEntity entity) {

            // 파일 엔티티를 파일 DTO 로 객체 변환
            // 바로 이때 파일 리스트가 SELECT 된다.
            List<StoreFileDTO> fileList = 
                entity.getFileList().stream().map(StoreFileDTO::of).filter((file)-> 
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
                entity.getHourList().stream().filter((hour) ->
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

            return Response.builder()
                .storeId(entity.getStoreId())
                .storeName(entity.getStoreName())
                .branchName(entity.getBranchName())
                .phone(entity.getPhone())
                .addr(entity.getAddr())
                .addrDetail(entity.getAddrDetail())
                .ratingAvg(entity.getRatingAvg())
                .reviewCount(entity.getReviewCount())
                .delYn(entity.getDelYn())
                .createDate(entity.getCreateDate())
                .updateDate(entity.getUpdateDate())
                .fileName(mainImage != null ? mainImage.getFileName() : null)
                .storedName(mainImage != null ? mainImage.getStoredName() : null)
                .filePath(mainImage != null ? mainImage.getFilePath() : null)
                .fileThumbName(mainImage != null ? mainImage.getFileThumbName() : null)
                .isOpen(isOpen)
                .hourComment(hourComment)
                .build();
        }
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Detail {
        private int storeId;
        private String storeName;
        private String branchName;
        private String phone;
        private String addr;
        private String addrDetail;
        private BigDecimal ratingAvg;
        private int reviewCount;
        private int minPrice;
        private String origin;
        private String notice;
        private String delYn;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createDate;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updateDate;
        private List<StoreFileDTO> fileList;
        private List<StoreCategoryDTO> categoryList;
        private List<MenuCategoryDTO.Response> menuCategoryList;
        private boolean isOpen;
        private String hourComment;
        private String businessHour;

        public static Detail of(StoreEntity entity) {

            // 파일 엔티티를 파일 DTO 로 객체 변환
            // 바로 이때 파일 리스트가 SELECT 된다.
            List<StoreFileDTO> fileList = 
                entity.getFileList().stream().map(StoreFileDTO::of)
                .sorted((o1, o2)-> o2.getMainYn().equals("Y") ? 1 : -1).toList();

            List<StoreCategoryDTO> categoryList = 
                entity.getCategoryList().stream().map(StoreCategoryDTO::of).toList();

            List<MenuCategoryDTO.Response> menuCategoryList =
                entity.getMenuCategoryList().stream()
                //.filter(menuCategory -> "N".equals(menuCategory.getDelYn()))
                .map(MenuCategoryDTO.Response::of).toList();


            // 오늘 요일 구하기 (1=월, 2=화, ... 7=일)
            int today = LocalDate.now().getDayOfWeek().getValue();
            // 지금 시간 구하기
            LocalTime now = LocalTime.now();
            
            Optional<StoreHourDTO> optionalDto = 
                entity.getHourList().stream().filter((hour) ->
                    hour.getDayOfWeek() == today).map(StoreHourDTO::of).findFirst();

            String hourComment = "";
            String businessHour = "";
            boolean isOpen = true;

            // 영업 여부, 영업 코멘트, 영업 시간 세팅하기
            if (optionalDto.isPresent()) {
                StoreHourDTO dto = optionalDto.get();

                LocalTime openTime = dto.getOpenTime();
                LocalTime closeTime = dto.getCloseTime();

                if ("Y".equals(dto.getCloseYn())) {  // 만약 오늘이 휴무날이면...
                    hourComment = "오늘 휴무";
                    businessHour = "오늘 휴무";
                    isOpen = false;
                } else if (now.isBefore(openTime)) { // 만약 지금이 오픈 시간 이전일경우...
                    hourComment = "오픈 전 (오늘 " + TimeFormatUtils.getTime(openTime, "HH:mm") + " 오픈)";
                    isOpen = false;
                } else if (now.isAfter(closeTime)) { // 만약 지금이 마감 시간 이후일경우...
                    hourComment = "영업 종료 (오늘 " + TimeFormatUtils.getTime(closeTime, "HH:mm") + " 마감)";
                    isOpen = false;
                }

                if ("N".equals(dto.getCloseYn())) { // 만약 오늘이 영업날이면...
                    businessHour = TimeFormatUtils.getTime(openTime, "HH:mm") + " ~ " + TimeFormatUtils.getTime(closeTime, "HH:mm");
                }
            }

            return Detail.builder()
                .storeId(entity.getStoreId())
                .storeName(entity.getStoreName())
                .branchName(entity.getBranchName())
                .phone(entity.getPhone())
                .addr(entity.getAddr())
                .addrDetail(entity.getAddrDetail())
                .ratingAvg(entity.getRatingAvg())
                .reviewCount(entity.getReviewCount())
                .minPrice(entity.getMinPrice())
                .origin(entity.getOrigin())
                .notice(entity.getNotice())
                .delYn(entity.getDelYn())
                .createDate(entity.getCreateDate())
                .updateDate(entity.getUpdateDate())
                .fileList(fileList)
                .categoryList(categoryList)
                .menuCategoryList(menuCategoryList)
                .isOpen(isOpen)
                .hourComment(hourComment)
                .businessHour(businessHour)
                .build();
        }
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class OwnerDetail {
        private int storeId;
        private String storeName;
        private String branchName;
        private String phone;
        private String addr;
        private String addrDetail;
        private BigDecimal ratingAvg;
        private int reviewCount;
        private int minPrice;
        private String origin;
        private String notice;
        private String delYn;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createDate;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updateDate;
        private List<StoreFileDTO> fileList;
        private List<StoreCategoryDTO> categoryList;
        private List<MenuCategoryDTO.Response> menuCategoryList;
        private List<StoreHourDTO> hourList;

        public static OwnerDetail of(StoreEntity entity) {

            // 파일 엔티티를 파일 DTO 로 객체 변환
            // 바로 이때 파일 리스트가 SELECT 된다.
            List<StoreFileDTO> fileList = 
                entity.getFileList().stream().map(StoreFileDTO::of)
                .sorted((o1, o2)-> o2.getMainYn().equals("Y") ? 1 : -1).toList();

            List<StoreCategoryDTO> categoryList = 
                entity.getCategoryList().stream().map(StoreCategoryDTO::of).toList();

            List<MenuCategoryDTO.Response> menuCategoryList =
                entity.getMenuCategoryList().stream()
                //.filter(menuCategory -> "N".equals(menuCategory.getDelYn()))
                .map(MenuCategoryDTO.Response::of).toList();

            List<StoreHourDTO> hourList =
                entity.getHourList().stream().map(StoreHourDTO::of)
                    .sorted(Comparator.comparingInt(StoreHourDTO::getDayOfWeek)).toList();

            return OwnerDetail.builder()
                .storeId(entity.getStoreId())
                .storeName(entity.getStoreName())
                .branchName(entity.getBranchName())
                .phone(entity.getPhone())
                .addr(entity.getAddr())
                .addrDetail(entity.getAddrDetail())
                .ratingAvg(entity.getRatingAvg())
                .reviewCount(entity.getReviewCount())
                .minPrice(entity.getMinPrice())
                .origin(entity.getOrigin())
                .notice(entity.getNotice())
                .delYn(entity.getDelYn())
                .createDate(entity.getCreateDate())
                .updateDate(entity.getUpdateDate())
                .fileList(fileList)
                .categoryList(categoryList)
                .menuCategoryList(menuCategoryList)
                .hourList(hourList)
                .build();
        }
    }

    @Data
    public static class Request {
        private int storeId;
        @NotBlank(message = "가게명은 필수 항목입니다.")
        private String storeName;
        private String branchName;
        @NotBlank(message = "전화번호는 필수 항목입니다.")
        private String phone;
        @NotBlank(message = "주소는 필수 항목입니다.")
        private String addr;
        private String addrDetail;
        @NotNull(message = "최소 주문 금액은 필수 항목입니다.")
        @Min(value = 0, message = "최소 주문 금액은 0원 이상이어야 합니다.")
        private Integer minPrice;
        @NotBlank(message = "원산지표시는 필수 항목입니다.")
        private String origin;
        private String notice;
        private String delYn;

        // 메인 이미지
		private MultipartFile mainImage;

        // 기타 이미지
        private List<MultipartFile> image;

        // 연관 정보
        @NotEmpty(message = "카테고리는 최소 1개 이상 선택해야 합니다.")
        private List<@NotNull(message = "카테고리 아이디는 비어 있을 수 없습니다.") Integer> categoryIds; // 선택한 카테고리 ID 리스트

        @NotEmpty(message = "요일별 영업시간 정보는 필수입니다.")
        @Size(min = 7, max = 7, message = "요일별 영업시간 정보는 7일(월 ~ 일) 모두 포함해야 합니다.")
        @Valid   // HourDTO의 필드 검증 활성화 
        private List<HourDTO> hourList;    // 요일별 영업 시간 정보
    }

    @Data
    public static class HourDTO {
        @Min(value = 1, message = "요일은 1(월)부터 7(일) 사이여야 합니다.")
        @Max(value = 7, message = "요일은 1(월)부터 7(일) 사이여야 합니다.")
        private int dayOfWeek;       // 1=월, 2=화, ... , 7=일
        @NotNull(message = "오픈 시간은 필수 항목입니다.")
        private LocalTime openTime;
        @NotNull(message = "마감 시간은 필수 항목입니다.")
        private LocalTime closeTime;
        @NotBlank(message = "휴우 여부는 필수 항목입니다.")
        @Pattern(regexp = "^[YN]$", message = "휴무 여부는 'Y' 또는 'N'이어야 합니다.")
        private String closeYn;      
    }
}
