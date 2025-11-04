package it.korea.app_bmpc.menu.controller;

import java.util.HashMap;
import java.util.Map;

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
import it.korea.app_bmpc.menu.dto.MenuCategoryDTO;
import it.korea.app_bmpc.menu.dto.MenuDTO;
import it.korea.app_bmpc.menu.dto.MenuOptionDTO;
import it.korea.app_bmpc.menu.dto.MenuOptionGroupDTO;
import it.korea.app_bmpc.menu.service.MenuService;
import it.korea.app_bmpc.user.dto.UserSecureDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "메뉴 API", description = "메뉴 카테고리, 메뉴, 메뉴 옵션 그룹, 메뉴 옵션 CURD 기능 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MenuApiController {

    private final MenuService menuService;

    /**
     * 메뉴 상세정보 가져오기
     * @param menuId 메뉴 아이디
     * @return
     * @throws Exception
     */
    @GetMapping("/menu/{menuId}")
    @Operation(summary = "메뉴 상세정보 가져오기")
    public ResponseEntity<?> getMenu(@PathVariable(name = "menuId") int menuId) throws Exception {

        Map<String, Object> resultMap = new HashMap<>();
        MenuDTO.Detail dto = menuService.getMenu(menuId);

        resultMap.put("vo", dto);

        return ResponseEntity.ok().body(ApiResponse.ok(resultMap));
    }

    /**
     * 메뉴 카테고리 등록하기
     * @param request 메뉴 카테고리 객체
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('OWNER')") // ROLE_OWNER 권한이 있어야 접근 가능
    @PostMapping("/menu/category")
    @Operation(summary = "메뉴 카테고리 등록하기")
    public ResponseEntity<?> createMenuCategory(@Valid @RequestBody MenuCategoryDTO.Request request,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {
        
        menuService.createMenuCategory(request, user.getUserId());

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 메뉴 카테고리 수정하기
     * @param request 메뉴 카테고리 객체
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('OWNER')") // ROLE_OWNER 권한이 있어야 접근 가능
    @PutMapping("/menu/category")
    @Operation(summary = "메뉴 카테고리 수정하기")
    public ResponseEntity<?> updateMenuCategory(@Valid @RequestBody MenuCategoryDTO.Request request,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {
        
        menuService.updateMenuCategory(request, user.getUserId());

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 메뉴 카테고리 삭제하기
     * @param menuCategoryId 메뉴 카테고리 아이디
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('OWNER')") // ROLE_OWNER 권한이 있어야 접근 가능
    @DeleteMapping("/menu/category/{menuCategoryId}")
    @Operation(summary = "메뉴 카테고리 삭제하기")
    public ResponseEntity<?> deleteMenuCategory(@PathVariable(name = "menuCategoryId") int menuCategoryId,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        menuService.deleteMenuCategory(menuCategoryId, user.getUserId());

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 메뉴 등록하기
     * @param request 메뉴 객체
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('OWNER')") // ROLE_OWNER 권한이 있어야 접근 가능
    @PostMapping("/menu")
    @Operation(summary = "메뉴 등록하기")
    public ResponseEntity<?> createMenu(@Valid @ModelAttribute MenuDTO.Request request,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {
        
        menuService.createMenu(request, user.getUserId());

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 메뉴 수정하기
     * @param request 메뉴 객체
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('OWNER')") // ROLE_OWNER 권한이 있어야 접근 가능
    @PutMapping("/menu")
    @Operation(summary = "메뉴 수정하기")
    public ResponseEntity<?> updateMenu(@Valid @ModelAttribute MenuDTO.Request request, 
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {
        
        menuService.updateMenu(request, user.getUserId());

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 메뉴 삭제하기
     * @param menuId 메뉴 아이디
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('OWNER')") // ROLE_OWNER 권한이 있어야 접근 가능
    @DeleteMapping("/menu/{menuId}")
    @Operation(summary = "메뉴 삭제하기")
    public ResponseEntity<?> deleteMenu(@PathVariable(name = "menuId") int menuId,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        menuService.deleteMenu(menuId, user.getUserId());

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 메뉴 옵션 그룹 등록하기
     * @param request 메뉴 옵션 그룹 객체
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('OWNER')") // ROLE_OWNER 권한이 있어야 접근 가능
    @PostMapping("/menu/group")
    @Operation(summary = "메뉴 옵션 그룹 등록하기")
    public ResponseEntity<?> createMenuOptionGroup(@Valid @RequestBody MenuOptionGroupDTO.Request request,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {
        
        menuService.createMenuOptionGroup(request, user.getUserId());

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 메뉴 옵션 그룹 수정하기
     * @param request 메뉴 옵션 그룹 객체
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('OWNER')") // ROLE_OWNER 권한이 있어야 접근 가능
    @PutMapping("/menu/group")
    @Operation(summary = "메뉴 옵션 그룹 수정하기")
    public ResponseEntity<?> updateMenuOptionGroup(@Valid @RequestBody MenuOptionGroupDTO.Request request,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        menuService.updateMenuOptionGroup(request, user.getUserId());

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 메뉴 옵션 그룹 삭제하기
     * @param menuOptGrpId 메뉴 옵션 그룹 아이디
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('OWNER')") // ROLE_OWNER 권한이 있어야 접근 가능
    @DeleteMapping("/menu/group/{menuOptGrpId}")
    @Operation(summary = "메뉴 옵션 그룹 삭제하기")
    public ResponseEntity<?> deleteMenuOptionGroup(@PathVariable(name = "menuOptGrpId") int menuOptGrpId,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        menuService.deleteMenuOptionGroup(menuOptGrpId, user.getUserId());

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 메뉴 옵션 등록하기
     * @param request 메뉴 옵션 객체
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('OWNER')") // ROLE_OWNER 권한이 있어야 접근 가능
    @PostMapping("/menu/option")
    @Operation(summary = "메뉴 옵션 등록하기")
    public ResponseEntity<?> createMenuOption(@Valid @RequestBody MenuOptionDTO.Request request,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        menuService.createMenuOption(request, user.getUserId());

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 메뉴 옵션 수정하기
     * @param request 메뉴 옵션 객체
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('OWNER')") // ROLE_OWNER 권한이 있어야 접근 가능
    @PutMapping("/menu/option")
    @Operation(summary = "메뉴 옵션 수정하기")
    public ResponseEntity<?> updateMenuOption(@Valid @RequestBody MenuOptionDTO.Request request,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        menuService.updateMenuOption(request, user.getUserId());

        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }

    /**
     * 메뉴 옵션 삭제하기
     * @param menuOptId 메뉴 옵션 아이디
     * @param user 로그인한 사용자
     * @return
     * @throws Exception
     */
    @PreAuthorize("hasRole('OWNER')") // ROLE_OWNER 권한이 있어야 접근 가능
    @DeleteMapping("/menu/option/{menuOptId}")
    @Operation(summary = "메뉴 옵션 삭제하기")
    public ResponseEntity<?> deleteMenuOption(@PathVariable(name = "menuOptId") int menuOptId,
            @AuthenticationPrincipal UserSecureDTO user) throws Exception {

        menuService.deleteMenuOption(menuOptId, user.getUserId());
        
        return ResponseEntity.ok().body(ApiResponse.ok("OK"));
    }
}
