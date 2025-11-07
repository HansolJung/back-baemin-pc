package it.korea.app_bmpc.order.dto;

import java.time.LocalDateTime;
import java.util.List;

import it.korea.app_bmpc.order.entity.OrderEntity;
import it.korea.app_bmpc.order.entity.OrderItemEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OrderSummaryDTO {

    private int orderId;
    private LocalDateTime orderDate;
    private int totalPrice;

    private List<OrderItemDTO> itemList;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItemDTO {
        private int itemId;
        private int quantity;
        private int totalPrice;
        private String menuName;
        private int menuPrice;

        public static OrderItemDTO of(OrderItemEntity entity) {
            return OrderItemDTO.builder()
                .itemId(entity.getItemId())
                .quantity(entity.getQuantity())
                .totalPrice(entity.getTotalPrice())
                .menuName(entity.getMenuName())
                .menuPrice(entity.getMenuPrice())
                .build();
        }
    }

    public static OrderSummaryDTO of(OrderEntity orderEntity) {

        List<OrderItemDTO> itemList = orderEntity.getItemList().stream()
            .map(OrderItemDTO::of)
            .toList();

        return OrderSummaryDTO.builder()
            .orderId(orderEntity.getOrderId())
            .orderDate(orderEntity.getOrderDate())
            .totalPrice(orderEntity.getTotalPrice())
            .itemList(itemList)
            .build();
    }
}
