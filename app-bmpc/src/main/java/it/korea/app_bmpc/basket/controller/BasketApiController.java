package it.korea.app_bmpc.basket.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.korea.app_bmpc.basket.dto.BasketDTO;
import it.korea.app_bmpc.basket.service.BasketService;
import it.korea.app_bmpc.common.dto.ApiResponse;
import it.korea.app_bmpc.user.dto.UserSecureDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BasketApiController {

    private final BasketService basketService;

    /**
     * 나의 장바구니 가져오기
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('USER')") // ROLE_USER 권한이 있어야 접근 가능
    @GetMapping("/basket")
    public ResponseEntity<?> getBasket(@AuthenticationPrincipal UserSecureDTO user) throws Exception {

        Map<String, Object> resultMap = new HashMap<>();
        BasketDTO.Detail dto = basketService.getBasket(user.getUserId());
    
        resultMap.put("vo", dto);

        return ResponseEntity.ok().body(ApiResponse.ok(resultMap));
    }

    /**
     * 장바구니 전부 주문하기
     * @param request 장바구니 객체
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('USER')") // ROLE_USER 권한이 있어야 접근 가능
    @PostMapping("/basket/order")
    public ResponseEntity<?> orderAllMenu(@Valid @RequestBody BasketDTO.OrderRequest request,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        request.setUserId(user.getUserId());

        basketService.orderAllMenu(request);

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 장바구니에 메뉴 추가하기
     * @param request 장바구니 객체
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('USER')") // ROLE_USER 권한이 있어야 접근 가능
    @PostMapping("/basket")
    public ResponseEntity<?> addMenu(@Valid @RequestBody BasketDTO.Request request,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        if (!user.getUserId().equals(request.getUserId())) {
            throw new RuntimeException("다른 사용자의 장바구니엔 메뉴를 추가할 수 없습니다.");
        }
    
        request.setUserId(user.getUserId());
        basketService.addMenu(request);

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 장바구니 메뉴 삭제하기
     * @param basketItemId 장바구니 항목 아이디
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('USER')") // ROLE_USER 권한이 있어야 접근 가능
    @DeleteMapping("/basket/item/{basketItemId}")
    public ResponseEntity<?> deleteMenu(@PathVariable(name = "basketItemId") int basketItemId,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        basketService.deleteMenu(user.getUserId(), basketItemId);

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }
    
    /**
     * 장바구니 메뉴 전부 삭제하기
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('USER')") // ROLE_USER 권한이 있어야 접근 가능
    @DeleteMapping("/basket")
    public ResponseEntity<?> deleteAllMenu(@AuthenticationPrincipal UserSecureDTO user) throws Exception {

        basketService.deleteAllMenu(user.getUserId());

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 장바구니 항목 수량 증가시키기 (+ 버튼 클릭)
     * @param basketItemId 장바구니 항목 아이디
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/basket/item/{basketItemId}/increase")
    public ResponseEntity<?> increaseMenuQuantity(
            @PathVariable(name = "basketItemId") int basketItemId,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        basketService.increaseMenuQuantity(user.getUserId(), basketItemId);

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 장바구니 항목 수량 감소시키기 (- 버튼 클릭)
     * @param basketItemId 장바구니 항목 아이디
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/basket/item/{basketItemId}/decrease")
    public ResponseEntity<?> decreaseMenuQuantity(
            @PathVariable(name = "basketItemId") int basketItemId,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        basketService.decreaseMenuQuantity(user.getUserId(), basketItemId);

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }
}
