package it.korea.app_bmpc.basket.entity;

import java.util.LinkedHashSet;
import java.util.Set;

import it.korea.app_bmpc.store.entity.StoreEntity;
import it.korea.app_bmpc.user.entity.UserEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bmpc_basket")
public class BasketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int basketId;
    private int totalPrice;

    // 장바구니 사용자 매핑
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    // 가게 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private StoreEntity store;

    // 장바구니 항목 매핑
    @OneToMany(mappedBy = "basket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("basketItemId ASC")
    private Set<BasketItemEntity> itemList = new LinkedHashSet<>();

    // 장바구니 항목 추가
    public void addItem(BasketItemEntity entity) {
        entity.setBasket(this);
        itemList.add(entity);
    } 
}
