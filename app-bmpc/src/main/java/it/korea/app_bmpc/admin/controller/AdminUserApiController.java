package it.korea.app_bmpc.admin.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.korea.app_bmpc.admin.dto.AdminReviewSearchDTO;
import it.korea.app_bmpc.admin.dto.AdminUserDTO;
import it.korea.app_bmpc.admin.dto.AdminUserRequestDTO;
import it.korea.app_bmpc.admin.dto.AdminUserSearchDTO;
import it.korea.app_bmpc.admin.dto.AdminUserUpdateRequestDTO;
import it.korea.app_bmpc.admin.service.AdminUserService;
import it.korea.app_bmpc.common.dto.ApiResponse;
import it.korea.app_bmpc.order.dto.OrderSearchDTO;
import it.korea.app_bmpc.order.dto.OrderStatusDTO;
import it.korea.app_bmpc.order.service.OrderService;
import it.korea.app_bmpc.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AdminUserApiController {

    private final AdminUserService userService;
    private final OrderService orderService;
    private final ReviewService reviewService;

    /**
     * 회원 리스트 가져오기
     * @param pageable 페이징 객체
     * @param searchDTO 검색 내용
     * @return
     * @throws Exception
     */
    @GetMapping("/admin/user")
    public ResponseEntity<?> getUserList(@PageableDefault(page = 0, size = 10, 
            sort = "createDate", direction = Direction.DESC) Pageable pageable,
            AdminUserSearchDTO searchDTO) throws Exception {
        
        Map<String, Object> resultMap = userService.getUserList(pageable, searchDTO);
        
        return ResponseEntity.ok().body(ApiResponse.ok(resultMap));
    }

    /**
     * 회원 상세 정보 가져오기
     * @param userId 사용자 아이디
     * @return
     * @throws Exception
     */
    @GetMapping("/admin/user/{userId}")
    public ResponseEntity<?> getUser(@PathVariable(name = "userId") String userId) throws Exception {
        
        Map<String, Object> resultMap = new HashMap<>();
        AdminUserDTO userDTO = userService.getUser(userId);

        resultMap.put("vo", userDTO);

        return ResponseEntity.ok().body(ApiResponse.ok(resultMap));
    }

    /**
     * 회원 등록하기
     * @param userRequestDTO 회원 등록 내용 DTO
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/admin/user")  
    public ResponseEntity<?> createUser(@Valid @RequestBody // JSON 타입으로 받기 때문에 @RequestBody 사용
            AdminUserRequestDTO userRequestDTO) throws Exception {
 
        userService.createUser(userRequestDTO);
       
        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 회원 정보 수정하기
     * @param userRequestDTO 회워 정보 수정 내용 DTO
     * @return
     * @throws Exception
     */
    @PutMapping(value = "/admin/user")
    public ResponseEntity<?> updateUser(@Valid @RequestBody // JSON 타입으로 받기 때문에 @RequestBody 사용
            AdminUserUpdateRequestDTO userRequestDTO) throws Exception {

        userService.updateUser(userRequestDTO);

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 회원 삭제하기
     * @param userId 회원 아이디
     * @return
     * @throws Exception
     */
    @DeleteMapping("/admin/user/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable(name = "userId") String userId) throws Exception {
  
        userService.deleteUser(userId);

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 주문 내역 리스트 가져오기
     * @param pageable 페이징 객체
     * @param searchDTO 검색 내용 DTO
     * @return
     * @throws Exception
     */
    @GetMapping("/admin/order")
    public ResponseEntity<?> getOrderList(@PageableDefault(page = 0, size = 10, 
            sort = "orderDate", direction = Direction.DESC) Pageable pageable,
            OrderSearchDTO searchDTO) throws Exception {
        
        Map<String, Object> resultMap = orderService.getOrderList(pageable, searchDTO);

        return ResponseEntity.ok().body(ApiResponse.ok(resultMap));
    }

    /**
     * 주문 상태 변경하기
     * @param orderStatusDTO 주문 상태 변경 내용 DTO
     * @return
     * @throws Exception
     */
    @PutMapping(value = "/admin/order")
    public ResponseEntity<?> updateStatus(@Valid @RequestBody // JSON 타입으로 받기 때문에 @RequestBody 사용
            OrderStatusDTO statusDTO) throws Exception {

        //orderService.updateStatus(statusDTO);

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 리뷰 리스트 가져오기
     * @param pageable 페이징 객체
     * @param searchDTO 검색 내용
     * @return
     * @throws Exception
     */
    @GetMapping("/admin/review")
    public ResponseEntity<?> getReviewList(@PageableDefault(page = 0, size = 10, 
            sort = "updateDate", direction = Direction.DESC) Pageable pageable,
            AdminReviewSearchDTO searchDTO) throws Exception {
        
        Map<String, Object> resultMap = reviewService.getReviewList(pageable, searchDTO);

        return ResponseEntity.ok().body(ApiResponse.ok(resultMap));
    }

    /**
     * 리뷰 삭제하기
     * @param reviewId 리뷰 아이디
     * @return
     * @throws Exception
     */
    @DeleteMapping("/admin/review/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable(name = "reviewId") int reviewId) throws Exception {

        reviewService.deleteReviewByAdmin(reviewId);

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 리뷰 답변 삭제하기
     * @param reviewReplyId 리뷰 답변 아이디
     * @return
     * @throws Exception
     */
    @DeleteMapping("/admin/review/reply/{reviewReplyId}")
    public ResponseEntity<?> deleteReviewReplyByAdmin(@PathVariable(name = "reviewReplyId") int reviewReplyId) throws Exception {
               
        reviewService.deleteReviewReplyByAdmin(reviewReplyId);

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }
}
