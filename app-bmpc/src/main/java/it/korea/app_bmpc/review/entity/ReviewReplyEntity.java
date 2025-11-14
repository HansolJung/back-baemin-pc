package it.korea.app_bmpc.review.entity;

import org.hibernate.annotations.SQLRestriction;

import it.korea.app_bmpc.common.entity.BaseEntity;
import it.korea.app_bmpc.user.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bmpc_review_reply")
@SQLRestriction("del_yn IN ('N', 'A')")
public class ReviewReplyEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int reviewReplyId;
    private String content;

    @Column(columnDefinition = "CHAR(1)")
    private String delYn;

    // 리뷰 매핑
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false, unique = true)
    private ReviewEntity review;

    // 회원 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
