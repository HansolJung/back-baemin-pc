package it.korea.app_bmpc.menu.dto;

import java.util.List;

import it.korea.app_bmpc.menu.entity.MenuOptionGroupEntity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MenuOptionGroupDTO {

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Response {
        
        private int menuOptGrpId;
        private String menuOptGrpName;
        private String requiredYn;
        private String delYn;
        private int minSelect;
        private int maxSelect;
        private int displayOrder;
        private List<MenuOptionDTO.Response> menuOptionList;

        public static Response of(MenuOptionGroupEntity entity) {

            List<MenuOptionDTO.Response> menuOptionList =
                entity.getMenuOptionList().stream().filter(mo -> "N".equals(mo.getDelYn()))
                    .map(MenuOptionDTO.Response::of).toList();

            return Response.builder()
                .menuOptGrpId(entity.getMenuOptGrpId())
                .menuOptGrpName(entity.getMenuOptGrpName())
                .requiredYn(entity.getRequiredYn())
                .delYn(entity.getDelYn())
                .minSelect(entity.getMinSelect())
                .maxSelect(entity.getMaxSelect())
                .displayOrder(entity.getDisplayOrder())
                .menuOptionList(menuOptionList)
                .build();
        }
    }

    @Data
	public static class Request {

		private int menuOptGrpId;
        @NotNull(message = "메뉴 아이디는 필수 항목입니다.")
        private Integer menuId;
        @NotBlank(message = "메뉴 옵션 그룹명은 필수 항목입니다.")
        private String menuOptGrpName;
        @NotBlank(message = "필수 선택 여부는 필수 항목입니다.")
        @Pattern(regexp = "^[YN]$", message = "필수 선택 여부는 'Y' 또는 'N'만 가능합니다.")
        private String requiredYn;
        @NotNull(message = "최소 선택 개수는 필수 항목입니다.")
        private Integer minSelect;
        @NotNull(message = "최대 선택 개수는 필수 항목입니다.")
        private Integer maxSelect;
        @NotNull(message = "정렬 순서는 필수 항목입니다.")
        @Min(value = 1, message = "정렬 순서는 1 이상이어야 합니다.")
        private Integer displayOrder;
	}
}
