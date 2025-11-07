package it.korea.app_bmpc.order.entity;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.BatchSize;

import it.korea.app_bmpc.menu.entity.MenuEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bmpc_order_item")
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int itemId;
    private int quantity;
    private int totalPrice;
    private String menuName;
    private int menuPrice;

    // 주문 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    // 메뉴 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private MenuEntity menu;

    // 주문 아이템 옵션 매핑
    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true) // 기본적으로 fetch = FetchType.LAZY
    @BatchSize(size = 100)
    private Set<OrderItemOptionEntity> itemOptionList = new HashSet<>();

    // 주문 아이템 옵션 추가
    public void addItemOption(OrderItemOptionEntity entity) {
        entity.setOrderItem(this);
        itemOptionList.add(entity);
    }
}
