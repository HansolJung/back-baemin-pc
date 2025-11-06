package it.korea.app_bmpc.store.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import it.korea.app_bmpc.common.entity.BaseEntity;
import it.korea.app_bmpc.menu.entity.MenuCategoryEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bmpc_store")
public class StoreEntity extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int storeId;
    private String storeName;
    private String branchName;
    private String phone;
    private String addr;
    private String addrDetail;

    @Column(precision = 2, scale = 1)
    private BigDecimal ratingAvg;
    
    private int reviewCount;
    private int minPrice;
    private String origin;
    private String notice;

    @Column(columnDefinition = "CHAR(1)")
    private String delYn;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;   // 위도 (y)
    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;  // 경도 (x)

    // 가게-카테고리 매핑
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StoreCategoryEntity> categoryList = new HashSet<>();

    // 파일(이미지) 매핑
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StoreFileEntity> fileList = new HashSet<>();

    // 메뉴 카테고리 매핑
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("displayOrder ASC")
    private List<MenuCategoryEntity> menuCategoryList = new ArrayList<>();

    // 영업시간 매핑
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StoreHourEntity> hourList = new HashSet<>();

    // 가게-카테고리 추가
    public void addCategory(StoreCategoryEntity entity, boolean isUpdate) {
        entity.setStore(this);
        categoryList.add(entity);

        if (isUpdate) { // 만약 카테고리를 업데이트 했다면...
            this.preUpdate();  // store 의 updateDate 갱신
        }
    }

    // 파일(이미지) 추가
    public void addFiles(StoreFileEntity entity, boolean isUpdate) {
        entity.setStore(this);
        fileList.add(entity);

        if (isUpdate) { // 만약 파일을 업데이트 했다면...
            this.preUpdate();  // store 의 updateDate 갱신
        }
    }

    // 메뉴 카테고리 추가
    public void addMenuCategory(MenuCategoryEntity entity, boolean isUpdate) {
        entity.setStore(this);
        menuCategoryList.add(entity);

        if (isUpdate) { // 만약 메뉴 카테고리를 업데이트 했다면...
            this.preUpdate();  // store 의 updateDate 갱신
        }
    }

    // 영업시간 추가
    public void addHour(StoreHourEntity entity, boolean isUpdate) {
        entity.setStore(this);
        hourList.add(entity);

        if (isUpdate) { // 만약 영업 시간을 업데이트 했다면...
            this.preUpdate();  // store 의 updateDate 갱신
        }
    }
}
