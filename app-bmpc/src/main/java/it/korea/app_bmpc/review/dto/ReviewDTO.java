package it.korea.app_bmpc.review.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonFormat;

import it.korea.app_bmpc.common.utils.MaskingUtils;
import it.korea.app_bmpc.order.dto.OrderDTO;
import it.korea.app_bmpc.order.dto.OrderSummaryDTO;
import it.korea.app_bmpc.review.entity.ReviewEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ReviewDTO {

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Response {
        private int reviewId;
        private int rating;
        private String content;
        private String delYn;
        private String writer; 
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createDate;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updateDate;
        private OrderSummaryDTO order;
        private List<ReviewFileDTO> fileList;
        private ReviewReplyDTO.Response reply;
        
        public static Response of(ReviewEntity entity, boolean isMasked) {

            // 파일 엔티티를 파일 DTO 로 객체 변환
            // 바로 이때 파일 리스트가 SELECT 된다.
            List<ReviewFileDTO> fileList = 
                entity.getFileList().stream().map(ReviewFileDTO::of).toList();

            // 답변 엔티티를 답변 DTO 로 객체 변환
            ReviewReplyDTO.Response reply = null;
            if (entity.getReply() != null) {
                reply = ReviewReplyDTO.Response.of(entity.getReply());
            }

            // 주문 엔티티를 주문 정보 요약 DTO 로 객체 변환
            OrderSummaryDTO orderSummary = OrderSummaryDTO.of(entity.getOrder());

            // 만약 마스킹 처리를 원하면 마스킹 처리를 해서 사용자 아이디 리턴, 아니라면 사용자 아이디 그대로 리턴
            String userId = entity.getUser().getUserId();
            String writer = isMasked ? MaskingUtils.maskingUserId(userId) : userId;

            return Response.builder()
                .reviewId(entity.getReviewId())
                .rating(entity.getRating())
                .content(entity.getContent())
                .delYn(entity.getDelYn())
                .createDate(entity.getCreateDate())
                .updateDate(entity.getUpdateDate())
                .writer(writer)
                .order(orderSummary)
                .fileList(fileList)
                .reply(reply)
                .build();
        }
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class DetailResponse {
        private int reviewId;
        private int rating;
        private String content;
        private String delYn;
        private String writer; 
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createDate;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updateDate;
        private OrderDTO.Response order;
        private List<ReviewFileDTO> fileList;
        private ReviewReplyDTO.Response reply;
        
        public static DetailResponse of(ReviewEntity entity) {

            // 파일 엔티티를 파일 DTO 로 객체 변환
            // 바로 이때 파일 리스트가 SELECT 된다.
            List<ReviewFileDTO> fileList = 
                entity.getFileList().stream().map(ReviewFileDTO::of).toList();

            // 답변 엔티티를 답변 DTO 로 객체 변환
            ReviewReplyDTO.Response reply = null;
            if (entity.getReply() != null) {
                reply = ReviewReplyDTO.Response.of(entity.getReply());
            }

            // 주문 엔티티를 주문 DTO 로 객체 변환
            OrderDTO.Response order = OrderDTO.Response.of(entity.getOrder());

            String userId = entity.getUser().getUserId();
            
            return DetailResponse.builder()
                .reviewId(entity.getReviewId())
                .rating(entity.getRating())
                .content(entity.getContent())
                .delYn(entity.getDelYn())
                .createDate(entity.getCreateDate())
                .updateDate(entity.getUpdateDate())
                .writer(MaskingUtils.maskingUserId(userId))
                .order(order)
                .fileList(fileList)
                .reply(reply)
                .build();
        }
    }

    @Data
    public static class Request {
        private int reviewId;

        @NotNull(message = "주문 아이디는 필수 항목입니다.")
        private Integer orderId;
        private String userId;
        @NotNull(message = "별점은 필수 항목입니다.")
        @Min(value = 1, message = "별점은 최소 1점이어야 합니다.")
        @Max(value = 5, message = "별점은 최대 5점까지 가능합니다.")
        private Integer rating;
        @NotBlank(message = "내용은 필수 항목입니다.")
        private String content;

        // 이미지
        private List<@Valid InnerRequest> imageList;
    }

    @Data
    public static class InnerRequest {
        private MultipartFile image;
        @NotNull(message = "정렬 순서는 필수 항목입니다.")
        @Min(value = 1, message = "정렬 순서는 1 이상이어야 합니다.")
        private Integer displayOrder;
    }
}
