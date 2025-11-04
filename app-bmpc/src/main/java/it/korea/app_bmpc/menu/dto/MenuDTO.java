package it.korea.app_bmpc.menu.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonFormat;

import it.korea.app_bmpc.menu.entity.MenuEntity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MenuDTO {

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Response {
        
        private int menuId;
        private String menuName;
        private String description;
        private int price;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createDate;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updateDate;
        private String fileName;
        private String storedName;
        private String filePath;
        private String fileThumbName;
        private String soldoutYn;
        private String delYn;

        public static Response of(MenuEntity entity) {
            // 파일 엔티티를 파일 DTO 로 객체 변환
            // 바로 이때 파일 리스트가 SELECT 된다.
            MenuFileDTO image = null;

            if (entity.getFile() != null) {
                image = MenuFileDTO.of(entity.getFile());
            }
            
            return Response.builder()
                .menuId(entity.getMenuId())
                .menuName(entity.getMenuName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .createDate(entity.getCreateDate())
                .updateDate(entity.getUpdateDate())
                .fileName(image != null ? image.getFileName() : null)
                .storedName(image != null ? image.getStoredName() : null)
                .filePath(image != null ? image.getFilePath() : null)
                .fileThumbName(image != null ? image.getFileThumbName() : null)
                .soldoutYn(entity.getSoldoutYn())
                .delYn(entity.getDelYn())
                .build();
        }
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Detail {

        private int menuId;
        private String menuName;
        private String description;
        private int price;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createDate;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updateDate;
        private String fileName;
        private String storedName;
        private String filePath;
        private String fileThumbName;
        private String soldoutYn;
        private String delYn;
        private List<MenuOptionGroupDTO.Response> menuOptionGroupList;

        public static Detail of(MenuEntity entity) {
            // 파일 엔티티를 파일 DTO 로 객체 변환
            // 바로 이때 파일 리스트가 SELECT 된다.
            MenuFileDTO image = null;

            if (entity.getFile() != null) {
                image = MenuFileDTO.of(entity.getFile());
            }

            List<MenuOptionGroupDTO.Response> menuOptionGroupList =
                entity.getMenuOptionGroupList().stream().filter(mog -> "N".equals(mog.getDelYn()))
                    .map(MenuOptionGroupDTO.Response::of).toList();

            return Detail.builder()
                .menuId(entity.getMenuId())
                .menuName(entity.getMenuName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .createDate(entity.getCreateDate())
                .updateDate(entity.getUpdateDate())
                .fileName(image != null ? image.getFileName() : null)
                .storedName(image != null ? image.getStoredName() : null)
                .filePath(image != null ? image.getFilePath() : null)
                .fileThumbName(image != null ? image.getFileThumbName() : null)
                .soldoutYn(entity.getSoldoutYn())
                .delYn(entity.getDelYn())
                .menuOptionGroupList(menuOptionGroupList)
                .build();
        }
    }

    @Data
	public static class Request {

		private int menuId;
        @NotNull(message = "메뉴 카테고리 아이디는 필수 항목입니다.")
        private Integer menuCategoryId;
        @NotBlank(message = "메뉴명은 필수 항목입니다.")
        private String menuName;
        private String description;
        @NotNull(message = "가격은 필수 항목입니다.")
        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        private Integer price;
        @NotBlank(message = "품절여부는 필수 항목입니다.")
        @Pattern(regexp = "^[YN]$", message = "품절여부는 'Y' 또는 'N'만 가능합니다.")
        private String soldoutYn;
        
		// 메인 이미지
		private MultipartFile mainImage;
	}
}
