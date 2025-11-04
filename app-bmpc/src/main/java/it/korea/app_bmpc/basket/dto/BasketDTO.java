package it.korea.app_bmpc.basket.dto;

import java.util.List;

import it.korea.app_bmpc.basket.entity.BasketEntity;
import it.korea.app_bmpc.store.entity.StoreEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BasketDTO {

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Detail {
        private int basketId;
        private int totalPrice;
        private List<BasketItemDTO> itemList;
        private Integer storeId;

        public static Detail of(BasketEntity entity) {

            List<BasketItemDTO> itemList = entity.getItemList().stream()
                .map(BasketItemDTO::of)
                .toList();

            StoreEntity store = entity.getStore();

            return Detail.builder()
                .basketId(entity.getBasketId())
                .totalPrice(entity.getTotalPrice())
                .itemList(itemList)
                .storeId(store == null ? null : store.getStoreId())
                .build();
        }
    }

    @Data
	public static class Request {

        private String userId;

        @Valid
        private InnerRequest menu;
	}

    @Data
    public static class InnerRequest {
        @NotNull(message = "메뉴 아이디는 필수 항목입니다.")
        private Integer menuId;
        @NotNull(message = "개수는 필수 항목입니다.")
        @Min(value = 1, message = "개수는 1개 이상이어야 합니다.")
        private Integer quantity;

        private List<@Valid InnerOptionRequest> optionList;
    }

    @Data
    public static class InnerOptionRequest {
        @NotNull(message = "메뉴 옵션 아이디는 필수 항목입니다.")
        private Integer menuOptId;
        @NotNull(message = "개수는 필수 항목입니다.")
        @Min(value = 1, message = "개수는 1개 이상이어야 합니다.")
        private Integer quantity;
    }

    @Data
    public static class OrderRequest {
        private String userId;
        @NotBlank(message = "주소는 필수 항목입니다.")
        private String addr;
        private String addrDetail;
    }
}
