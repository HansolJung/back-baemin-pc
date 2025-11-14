package it.korea.app_bmpc.review.controller;

import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.korea.app_bmpc.common.dto.ApiResponse;
import it.korea.app_bmpc.review.dto.ReviewDTO;
import it.korea.app_bmpc.review.dto.ReviewReplyDTO;
import it.korea.app_bmpc.review.service.ReviewService;
import it.korea.app_bmpc.user.dto.UserSecureDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "리뷰 API", description = "리뷰와 리뷰 답변 CRUD API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReviewApiController {

    private final ReviewService reviewService;

    /**
     * 가게의 리뷰 리스트 가져오기
     * @param pageable 페이징 객체
     * @param storeId 가게 아이디
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @GetMapping("/review/store/{storeId}")
    @Operation(summary = "가게의 리뷰 리스트 가져오기")
    public ResponseEntity<?> getStoreReviewList(@PageableDefault(page = 0, size = 10, 
            sort = "createDate", direction = Direction.DESC) Pageable pageable,
            @PathVariable(name = "storeId") int storeId,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {
        
        boolean isAdmin = user != null && user.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        Map<String, Object> resultMap = reviewService.getStoreReviewList(pageable, storeId, isAdmin);

        return ResponseEntity.ok().body(ApiResponse.ok(resultMap));
    }

    /**
     * 나의 가게의 리뷰 리스트 가져오기
     * @param pageable 페이징 객체
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('OWNER')") // ROLE_OWNER 권한이 있어야 접근 가능
    @GetMapping("/review/store/my")
    @Operation(summary = "나의 가게의 리뷰 리스트 가져오기")
    public ResponseEntity<?> getOwnerStoreReviewList(@PageableDefault(page = 0, size = 10, 
            sort = "createDate", direction = Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        Map<String, Object> resultMap = reviewService.getOwnerStoreReviewList(pageable, user.getUserId());

        return ResponseEntity.ok().body(ApiResponse.ok(resultMap));
    }

    /**
     * 내가 작성한 리뷰 리스트 가져오기
     * @param pageable 페이징 객체
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('USER')") // ROLE_USER 권한이 있어야 접근 가능
    @GetMapping("/review")
    @Operation(summary = "내가 작성한 리뷰 리스트 가져오기")
    public ResponseEntity<?> getUserReviewList(@PageableDefault(page = 0, size = 10, 
            sort = "createDate", direction = Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {
        
        Map<String, Object> resultMap = reviewService.getUserReviewList(pageable, user.getUserId());

        return ResponseEntity.ok().body(ApiResponse.ok(resultMap));
    }

    /**
     * 리뷰 등록하기
     * @param request 리뷰 객체
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('USER')") // ROLE_USER 권한이 있어야 접근 가능
    @PostMapping("/review")
    @Operation(summary = "리뷰 등록하기")
    public ResponseEntity<?> createReview(@Valid @ModelAttribute ReviewDTO.Request request,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        request.setUserId(user.getUserId());
        reviewService.createReview(request);

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 리뷰 수정하기
     * @param request 리뷰 객체 
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('USER')") // ROLE_USER 권한이 있어야 접근 가능
    @PutMapping("/review")
    @Operation(summary = "리뷰 수정하기")
    public ResponseEntity<?> updateReview(@Valid @ModelAttribute ReviewDTO.Request request,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        request.setUserId(user.getUserId());
        reviewService.updateReview(request);

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 리뷰 삭제하기
     * @param reviewId 리뷰 아이디
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('USER')") // ROLE_USER 권한이 있어야 접근 가능
    @DeleteMapping("/review/{reviewId}")
    @Operation(summary = "리뷰 삭제하기")
    public ResponseEntity<?> deleteReview(
            @PathVariable(name = "reviewId") int reviewId,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        reviewService.deleteReview(user.getUserId(), reviewId);

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 리뷰 답변 등록하기
     * @param request 리뷰 답변 객체
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('OWNER')") // ROLE_OWNER 권한이 있어야 접근 가능
    @PostMapping("/review/reply")
    @Operation(summary = "리뷰 답변 등록하기")
    public ResponseEntity<?> createReviewReply(@Valid @RequestBody ReviewReplyDTO.Request request,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {
               
        request.setUserId(user.getUserId());
        reviewService.createReviewReply(request);

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 리뷰 답변 수정하기
     * @param request 리뷰 답변 객체
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('OWNER')") // ROLE_OWNER 권한이 있어야 접근 가능
    @PutMapping("/review/reply")
    @Operation(summary = "리뷰 답변 수정하기")
    public ResponseEntity<?> updateReviewReply(@Valid @RequestBody ReviewReplyDTO.Request request,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {
               
        request.setUserId(user.getUserId());
        reviewService.updateReviewReply(request);

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 리뷰 답변 삭제하기
     * @param reviewReplyId 리뷰 답변 아이디
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('OWNER')") // ROLE_OWNER 권한이 있어야 접근 가능
    @DeleteMapping("/review/reply/{reviewReplyId}")
    @Operation(summary = "리뷰 답변 삭제하기")
    public ResponseEntity<?> deleteReviewReply(@PathVariable(name = "reviewReplyId") int reviewReplyId,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {
               
        reviewService.deleteReviewReply(reviewReplyId, user.getUserId());

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }
}
