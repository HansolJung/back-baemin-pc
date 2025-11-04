package it.korea.app_bmpc.menu.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import it.korea.app_bmpc.menu.entity.MenuCategoryEntity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MenuCategoryDTO {

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Response {
        
        private int menuCaId;
        private String menuCaName;
        private int displayOrder;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createDate;
        private String delYn;
        private List<MenuDTO.Response> menuList;

        public static Response of(MenuCategoryEntity entity) {
            List<MenuDTO.Response> menuList = 
                entity.getMenuList().stream().filter(menu -> "N".equals(menu.getDelYn()))
                    .map(MenuDTO.Response::of).toList();

            return Response.builder()
                .menuCaId(entity.getMenuCaId())
                .menuCaName(entity.getMenuCaName())
                .displayOrder(entity.getDisplayOrder())
                .createDate(entity.getCreateDate())
                .delYn(entity.getDelYn())
                .menuList(menuList)
                .build();
        }
    }

    @Data
	public static class Request {

        private int menuCaId;
        private Integer storeId;
        @NotBlank(message = "메뉴 카테고리명은 필수 항목입니다.")
        private String menuCaName;
        @NotNull(message = "정렬순서는 필수 항목입니다.")
        @Min(value = 1, message = "정렬순서는 1 이상이어야 합니다.")
        private Integer displayOrder;
        private String delYn;
	}
}
