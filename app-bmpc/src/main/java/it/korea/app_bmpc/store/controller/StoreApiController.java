package it.korea.app_bmpc.store.controller;

import java.util.HashMap;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.korea.app_bmpc.common.dto.ApiResponse;
import it.korea.app_bmpc.store.dto.CategoryDTO;
import it.korea.app_bmpc.store.dto.StoreDTO;
import it.korea.app_bmpc.store.dto.StoreSearchDTO;
import it.korea.app_bmpc.store.service.StoreService;
import it.korea.app_bmpc.user.dto.OwnerResponseDTO;
import it.korea.app_bmpc.user.dto.UserSecureDTO;
import it.korea.app_bmpc.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "가게 API", description = "회원 관리, 리뷰 관리 등 어드민 기능 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StoreApiController {

    private final StoreService storeService;
    private final UserService userService;
    
    /**
     * 가게 카테고리 리스트 가져오기
     * @return
     * @throws Exception
     */
    @GetMapping("/store/category")
    @Operation(summary = "가게 카테고리 리스트 가져오기")
    public ResponseEntity<?> getCategoryList() throws Exception {

        List<CategoryDTO> categoryList = storeService.getCategoryList();

        return ResponseEntity.ok().body(ApiResponse.ok(categoryList));
    }

    /**
     * 가게 리스트 가져오기
     * @param pageable 페이징 객체
     * @param searchDTO 검색 내용
     * @return
     * @throws Exception
     */
    @GetMapping("/store")
    @Operation(summary = "가게 리스트 가져오기")
    public ResponseEntity<?> getStoreList(@PageableDefault(page = 0, size = 10, 
            sort = "updateDate", direction = Direction.DESC) Pageable pageable,
            @Valid StoreSearchDTO searchDTO) throws Exception {

        Map<String, Object> resultMap = storeService.getStoreList(pageable, searchDTO);

        return ResponseEntity.ok().body(ApiResponse.ok(resultMap));
    }

    /**
     * 가게 상세정보 가져오기
     * @param storeId 가게 아이디
     * @return
     * @throws Exception
     */
    @GetMapping("/store/{storeId}")
    @Operation(summary = "가게 상세정보 가져오기")
    public ResponseEntity<?> getStore(@PathVariable(name = "storeId") int storeId) throws Exception {

        Map<String, Object> resultMap = new HashMap<>();
        StoreDTO.Detail dto = storeService.getStore(storeId);
        OwnerResponseDTO ownerDto = userService.getOwner(storeId);   // 점주 정보 가져오기

        resultMap.put("vo", dto);
        resultMap.put("ownerInfo", ownerDto);    // 점주 정보

        return ResponseEntity.ok().body(ApiResponse.ok(resultMap));
    }

    /**
     * 나의 가게 상세정보 가져오기
     * @param userId 사용자 아이디
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('OWNER')") // ROLE_OWNER 권한이 있어야 접근 가능
    @GetMapping("/store/my")
    @Operation(summary = "나의 가게 상세정보 가져오기")
    public ResponseEntity<?> getOwnerStore(@AuthenticationPrincipal UserSecureDTO user) throws Exception {
        
        Map<String, Object> resultMap = new HashMap<>();
        StoreDTO.OwnerDetail dto = storeService.getOwnerStore(user.getUserId());

        resultMap.put("vo", dto);

        return ResponseEntity.ok().body(ApiResponse.ok(resultMap));
    }

    /**
     * 가게 등록하기
     * @param request 가게 객체
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('OWNER')") // ROLE_OWNER 권한이 있어야 접근 가능
    @PostMapping("/store")
    @Operation(summary = "가게 등록하기")
    public ResponseEntity<?> createStore(@Valid @ModelAttribute StoreDTO.Request request, 
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {
        
        storeService.createStore(request, user.getUserId());

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 가게 수정하기
     * @param request 가게 객체
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('OWNER')") // ROLE_OWNER 권한이 있어야 접근 가능
    @PutMapping("/store")
    @Operation(summary = "가게 수정하기")
    public ResponseEntity<?> updateStore(@Valid @ModelAttribute StoreDTO.Request request,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {
        
        storeService.updateStore(request, user.getUserId());

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 가게 삭제하기
     * @param storeId 가게 아이디
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('OWNER')") // ROLE_OWNER 권한이 있어야 접근 가능
    @DeleteMapping("/store/{storeId}")
    @Operation(summary = "가게 삭제하기")
    public ResponseEntity<?> deleteStore(@PathVariable(name = "storeId") int storeId,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {
 
        storeService.deleteStore(storeId, user.getUserId());

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 최근 한달간 인기 가게 top 10 리스트 가져오기 (with 캐시 저장)
     * @return
     * @throws Exception
     */
    @GetMapping("/store/popular")
    public ResponseEntity<?> getPopularStoreList() throws Exception {

        Map<String, Object> resultMap = new HashMap<>();
        List<StoreDTO.Popular> dtoList = storeService.getPopularStoreList();

        resultMap.put("vo", dtoList);

        return ResponseEntity.ok().body(ApiResponse.ok(resultMap));
    }
}
