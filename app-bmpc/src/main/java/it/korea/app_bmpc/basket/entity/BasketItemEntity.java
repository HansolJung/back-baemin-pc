package it.korea.app_bmpc.basket.entity;

import java.util.LinkedHashSet;
import java.util.Set;

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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bmpc_basket_item")
public class BasketItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int basketItemId;
    private String menuName;
    private int menuPrice;
    private int quantity;
    private int totalPrice;

    // 장바구니 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "basket_id")
    private BasketEntity basket;

    // 메뉴 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    private MenuEntity menu;

    // 장바구니 항목 옵션 매핑
    @OneToMany(mappedBy = "basketItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("basketItemOptId ASC")
    private Set<BasketItemOptionEntity> itemOptionList = new LinkedHashSet<>();

    // 장바구니 항목 옵션 추가
    public void addItemOption(BasketItemOptionEntity entity) {
        entity.setBasketItem(this);
        itemOptionList.add(entity);
    } 
}
