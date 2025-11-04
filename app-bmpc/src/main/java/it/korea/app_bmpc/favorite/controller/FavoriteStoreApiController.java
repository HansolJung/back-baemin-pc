package it.korea.app_bmpc.favorite.controller;

import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.korea.app_bmpc.common.dto.ApiResponse;
import it.korea.app_bmpc.favorite.dto.FavoriteStoreDTO;
import it.korea.app_bmpc.favorite.service.FavoriteStoreService;
import it.korea.app_bmpc.user.dto.UserSecureDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "찜 API", description = "찜 등록, 찜 삭제 등 찜 기능 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class FavoriteStoreApiController {

    private final FavoriteStoreService favoriteStoreService;

    /**
     * 찜 리스트 가져오기
     * @param pageable 페이징 객체
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @GetMapping("/favorite")
    @Operation(summary = "찜 리스트 가져오기")
    public ResponseEntity<?> getFavoriteStoreList(@PageableDefault(page = 0, size = 10, 
            sort = "createDate", direction = Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        Map<String, Object> resultMap = favoriteStoreService.getFavoriteStoreList(pageable, user.getUserId());

        return ResponseEntity.ok().body(ApiResponse.ok(resultMap));
    }

    /**
     * 찜 목록 존재 여부 확인하기
     * @param storeId 가게 아이디
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @GetMapping("/favorite/store/{storeId}")
    @Operation(summary = "찜 목록 존재 여부 확인하기")
    public ResponseEntity<?> isFavoriteStore(@PathVariable(name = "storeId") int storeId,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        boolean isFavoriteStore = favoriteStoreService.isFavoriteStore(user.getUserId(), storeId);

        return ResponseEntity.ok().body(ApiResponse.ok(isFavoriteStore));
    }

    /**
     * 찜 등록하기
     * @param request 찜 객체
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PostMapping("/favorite")
    @Operation(summary = "찜 등록하기")
    public ResponseEntity<?> addFavoriteStore(@Valid @RequestBody FavoriteStoreDTO.Request request,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        if (!request.getUserId().equals(user.getUserId())) {   // 로그인 된 유저의 아이디와 request 로 넘어온 아이디가 일치하지 않을 경우...
            throw new RuntimeException("다른 사용자의 찜 목록엔 등록할 수 없습니다.");
        }
    
        favoriteStoreService.addFavoriteStore(request);

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 찜 목록에서 삭제하기
     * @param storeId 가게 아이디
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @DeleteMapping("/favorite/store/{storeId}")
    @Operation(summary = "찜 목록에서 삭제하기")
    public ResponseEntity<?> deleteFavoriteStore(@PathVariable(name = "storeId") int storeId,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        favoriteStoreService.deleteFavoriteStore(user.getUserId(), storeId);

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }
}
