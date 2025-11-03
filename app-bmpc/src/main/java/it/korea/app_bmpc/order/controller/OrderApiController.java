package it.korea.app_bmpc.order.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.korea.app_bmpc.common.dto.ApiResponse;
import it.korea.app_bmpc.order.dto.OrderDTO;
import it.korea.app_bmpc.order.dto.OrderStatusDTO;
import it.korea.app_bmpc.order.service.OrderService;
import it.korea.app_bmpc.user.dto.UserSecureDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class OrderApiController {

    private final OrderService orderService;

    /**
     * 나의 주문내역 리스트 요청
     * @param pageable 페이징 객체
     * @param userId 사용자 아이디
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('USER')") // ROLE_USER 권한이 있어야 접근 가능
    @GetMapping("/order/user/{userId}")
    public ResponseEntity<?> getMyOrderList(@PageableDefault(page = 0, size = 10, 
            sort = "orderDate", direction = Direction.DESC) Pageable pageable,
            @PathVariable(name = "userId") String userId,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        if (!userId.equals(user.getUserId())) {   // 로그인 된 유저의 아이디와 PathVariable 로 넘어온 아이디가 일치하지 않을 경우...
            throw new RuntimeException("다른 사용자의 주문 내역 리스트는 볼 수 없습니다.");
        }
        
        Map<String, Object> resultMap = orderService.getMyOrderList(pageable, userId);

        return ResponseEntity.ok().body(ApiResponse.ok(resultMap));
    }

    /**
     * 가게의 주문내역 리스트 요청
     * @param pageable 페이징 객체
     * @param storeId 가게 아이디
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('OWNER')") // ROLE_OWNER 권한이 있어야 접근 가능
    @GetMapping("/order/store/{storeId}")
    public ResponseEntity<?> getStoreOrderList(@PageableDefault(page = 0, size = 10, 
            sort = "orderDate", direction = Direction.DESC) Pageable pageable,
            @PathVariable(name = "storeId") int storeId,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        Map<String, Object> resultMap = orderService.getStoreOrderList(pageable, storeId, user.getUserId());

        return ResponseEntity.ok().body(ApiResponse.ok(resultMap));
    }

    /**
     * 내 가게의 주문내역 리스트 요청
     * @param pageable 페이징 객체
     * @param storeId 가게 아이디
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('OWNER')") // ROLE_OWNER 권한이 있어야 접근 가능
    @GetMapping("/order/store/my")
    public ResponseEntity<?> getOwnerStoreOrderList(@PageableDefault(page = 0, size = 10, 
            sort = "orderDate", direction = Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        Map<String, Object> resultMap = orderService.getOwnerStoreOrderList(pageable, user.getUserId());

        return ResponseEntity.ok().body(ApiResponse.ok(resultMap));
    }

    /**
     * 주문내역 상세 보기
     * @param orderId 주문 아이디
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('USER')") // ROLE_USER 권한이 있어야 접근 가능
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getOrderDetail(@PathVariable(name = "orderId") int orderId,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {
        
        String userId = user.getUserId();

        Map<String, Object> resultMap = new HashMap<>();
        OrderDTO.Detail dto = orderService.getOrder(orderId, userId);

        resultMap.put("vo", dto);
        
        return ResponseEntity.ok().body(ApiResponse.ok(resultMap));
    }

    /**
     * 주문 상태 변경하기 (주문취소 or 배달완료)
     * @param request 주문 상태 객체
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('OWNER')") // ROLE_OWNER 권한이 있어야 접근 가능
    @PutMapping("/order/status")
    public ResponseEntity<?> changeStatus(@Valid @RequestBody OrderStatusDTO request,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        orderService.updateStatus(request, user.getUserId());

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 메뉴 주문하기
     * @param request 주문 객체
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PostMapping("/order")
    public ResponseEntity<?> orderMenu(@Valid @RequestBody OrderDTO.Request request,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        request.setUserId(user.getUserId());
        orderService.orderMenu(request);

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 가게 기간별 매출 통계 구하기
     * @param storeId 가게 아이디
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('OWNER')") // ROLE_OWNER 권한이 있어야 접근 가능
    @GetMapping("/order/store/{storeId}/sales")
    public ResponseEntity<?> getStoreSalesStat(
            @PathVariable(name = "storeId") int storeId,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        Map<String, Object> resultMap = orderService.getStoreSalesStat(storeId, user.getUserId());

        return ResponseEntity.ok().body(ApiResponse.ok(resultMap));
    }
}
