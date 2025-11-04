package it.korea.app_bmpc.menu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bmpc_menu_option")
public class MenuOptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int menuOptId;
    private String menuOptName;
    private int price;
    
    @Column(columnDefinition = "CHAR(1)")
    private String availableYn;
    @Column(columnDefinition = "CHAR(1)")
    private String delYn;

    private int maxSelect;
    private int displayOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_opt_grp_id", nullable = false)
    private MenuOptionGroupEntity menuOptionGroup;
}
