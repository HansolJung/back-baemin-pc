package it.korea.app_bmpc.menu.entity;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
@Table(name = "bmpc_menu_option_group")
public class MenuOptionGroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int menuOptGrpId;
    private String menuOptGrpName;

    @Column(columnDefinition = "CHAR(1)")
    private String requiredYn;
    @Column(columnDefinition = "CHAR(1)")
    private String delYn;

    private int minSelect;
    private int maxSelect;
    private int displayOrder;

    // 메뉴 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private MenuEntity menu;

    // 메뉴 옵션 매핑
    @OneToMany(mappedBy = "menuOptionGroup", cascade = CascadeType.ALL, orphanRemoval = true) // 기본적으로 fetch = FetchType.LAZY
    @OrderBy("price ASC")
    private Set<MenuOptionEntity> menuOptionList = new LinkedHashSet<>();

    // 메뉴 추가
    public void addMenuOption(MenuOptionEntity entity, boolean isUpdate) {
        entity.setMenuOptionGroup(this);
        menuOptionList.add(entity);
    }
}
