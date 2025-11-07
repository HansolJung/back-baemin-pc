package it.korea.app_bmpc.order.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import it.korea.app_bmpc.order.entity.OrderEntity;
import it.korea.app_bmpc.store.entity.StoreEntity;
import it.korea.app_bmpc.user.dto.UserSecureDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class OrderDTO {

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Response {
        private int orderId;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime orderDate;;
        private int totalPrice;
        private String status;
        private String userName;
        private String userId;
        private String addr;
        private String addrDetail;
        private int storeId;
        private String storeName;
        private List<OrderItemSummaryDTO> itemList;
     
        public static Response of(OrderEntity entity) {
            
            UserSecureDTO user = new UserSecureDTO(entity.getUser());
            List<OrderItemSummaryDTO> itemList = entity.getItemList().stream().map(OrderItemSummaryDTO::of).toList();

            StoreEntity store = entity.getStore();

            return Response.builder()
                .orderId(entity.getOrderId())
                .orderDate(entity.getOrderDate())
                .totalPrice(entity.getTotalPrice())
                .status(entity.getStatus())
                .userName(user.getUserName())
                .userId(user.getUserId())
                .addr(entity.getAddr())
                .addrDetail(entity.getAddrDetail())
                .storeId(store.getStoreId())
                .storeName(store.getStoreName())
                .itemList(itemList)
                .build();
        }
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class DetailResponse {
        private int orderId;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime orderDate;;
        private int totalPrice;
        private String status;
        private String userName;
        private String userId;
        private String addr;
        private String addrDetail;
        private List<OrderItemSummaryDTO> itemList;
     
        public static DetailResponse of(OrderEntity entity) {
            
            UserSecureDTO user = new UserSecureDTO(entity.getUser());
            List<OrderItemSummaryDTO> itemList = entity.getItemList().stream().map(OrderItemSummaryDTO::of).toList();

            return DetailResponse.builder()
                .orderId(entity.getOrderId())
                .orderDate(entity.getOrderDate())
                .totalPrice(entity.getTotalPrice())
                .status(entity.getStatus())
                .userName(user.getUserName())
                .userId(user.getUserId())
                .addr(entity.getAddr())
                .addrDetail(entity.getAddrDetail())
                .itemList(itemList)
                .build();
        }
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Detail {
        private int orderId;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime orderDate;;
        private int totalPrice;
        private String status;
        private String userName;
        private String userId;
        private String addr;
        private String addrDetail;
        private int storeId;
        private String storeName;
        private List<OrderItemDTO> itemList;
     
        public static Detail of(OrderEntity entity) {
            
            UserSecureDTO user = new UserSecureDTO(entity.getUser());
            List<OrderItemDTO> itemList = entity.getItemList()
                .stream().map(OrderItemDTO::of).toList();

            StoreEntity store = entity.getStore();

            return Detail.builder()
                .orderId(entity.getOrderId())
                .orderDate(entity.getOrderDate())
                .totalPrice(entity.getTotalPrice())
                .status(entity.getStatus())
                .userName(user.getUserName())
                .userId(user.getUserId())
                .addr(entity.getAddr())
                .addrDetail(entity.getAddrDetail())
                .storeId(store.getStoreId())
                .storeName(store.getStoreName())
                .itemList(itemList)
                .build();
        }
    }

    @Data
	public static class Request {

        private String userId;
        private String addr;
        private String addrDetail;

        @Valid
        private List<InnerRequest> menuList;
	}

    @Data
    public static class InnerRequest {
        private int menuId;
        private int quantity;
        private List<InnerOptionRequest> optionList;
    }

    @Data
    public static class InnerOptionRequest {
        private int menuOptId;
        private int quantity;
    }
}
