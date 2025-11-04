package it.korea.app_bmpc.menu.service;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import it.korea.app_bmpc.common.utils.FileUtils;
import it.korea.app_bmpc.config.WebConfig;
import it.korea.app_bmpc.menu.dto.MenuCategoryDTO;
import it.korea.app_bmpc.menu.dto.MenuDTO;
import it.korea.app_bmpc.menu.dto.MenuOptionDTO;
import it.korea.app_bmpc.menu.dto.MenuOptionGroupDTO;
import it.korea.app_bmpc.menu.entity.MenuCategoryEntity;
import it.korea.app_bmpc.menu.entity.MenuEntity;
import it.korea.app_bmpc.menu.entity.MenuFileEntity;
import it.korea.app_bmpc.menu.entity.MenuOptionEntity;
import it.korea.app_bmpc.menu.entity.MenuOptionGroupEntity;
import it.korea.app_bmpc.menu.repository.MenuCategoryRepository;
import it.korea.app_bmpc.menu.repository.MenuOptionGroupRepository;
import it.korea.app_bmpc.menu.repository.MenuOptionRepository;
import it.korea.app_bmpc.menu.repository.MenuRepository;
import it.korea.app_bmpc.store.entity.StoreEntity;
import it.korea.app_bmpc.store.repository.StoreRepository;
import it.korea.app_bmpc.user.entity.UserEntity;
import it.korea.app_bmpc.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final WebConfig webConfig;

    private final MenuRepository menuRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuOptionGroupRepository menuOptionGroupRepository;
    private final MenuOptionRepository menuOptionRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final FileUtils fileUtils;

    /**
     * 메뉴 상세정보 가져오기
     * @param menuId 메뉴 아이디
     * @return
     * @throws Exception
     */
    @Transactional(readOnly = true)
    public MenuDTO.Detail getMenu(int menuId) throws Exception {
        return MenuDTO.Detail.of(menuRepository.getMenu(menuId)
            .orElseThrow(()-> new RuntimeException("해당 메뉴가 존재하지 않습니다.")));
    }

    /**
     * 메뉴 카테고리 등록하기
     * @param request 메뉴 카테고리 객체
     * @param userId 사용자 아이디
     * @throws Exception
     */
    @Transactional
    public void createMenuCategory(MenuCategoryDTO.Request request, String userId) throws Exception {

        StoreEntity storeEntity = storeRepository.findById(request.getStoreId())
            .orElseThrow(()-> new RuntimeException("해당 가게가 존재하지 않습니다."));

        if ("Y".equals(storeEntity.getDelYn())) {
            throw new RuntimeException("삭제된 가게 하위엔 메뉴 카테고리를 등록할 수 없습니다.");
        }

        // 점주 소유 여부 체크
        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        if (userEntity.getStore() == null || userEntity.getStore().getStoreId() != storeEntity.getStoreId()) {
            throw new RuntimeException("해당 가게에 대한 권한이 없습니다.");
        }

        // 기존 카테고리 리스트 displayOrder 오름차순으로 가져오기
        List<MenuCategoryEntity> categories = menuCategoryRepository
            .findByStoreAndDelYnOrderByDisplayOrderAsc(storeEntity, "N");

        int newOrder = request.getDisplayOrder();

        // 만약 새로운 displayOrder 값이 기존 카테고리 리스트 사이즈보다 크다면 고정시키기
        if (newOrder > categories.size() + 1) {
            newOrder = categories.size() + 1;
        }

        // displayOrder가 newOrder 이상인 항목들은 모두 +1 씩 밀어내기
        for (MenuCategoryEntity category : categories) {
            if (category.getDisplayOrder() >= newOrder) {
                category.setDisplayOrder(category.getDisplayOrder() + 1);
            }
        }
        
        MenuCategoryEntity entity = new MenuCategoryEntity();
        entity.setMenuCaName(request.getMenuCaName());
        entity.setDisplayOrder(newOrder);
        entity.setDelYn("N");

        storeEntity.addMenuCategory(entity, false);

        menuCategoryRepository.save(entity);
    }

    /**
     * 메뉴 카테고리 수정하기
     * @param request 메뉴 카테고리 객체
     * @param userId 사용자 아이디
     * @throws Exception
     */
    @Transactional
    public void updateMenuCategory(MenuCategoryDTO.Request request, String userId) throws Exception {

        MenuCategoryEntity entity = menuCategoryRepository.findById(request.getMenuCaId())
            .orElseThrow(() -> new RuntimeException("해당 메뉴 카테고리가 존재하지 않습니다."));
        
        // 삭제 여부 확인
        if ("Y".equals(entity.getDelYn())) {
            throw new RuntimeException("삭제된 메뉴 카테고리는 수정할 수 없습니다.");
        }

        StoreEntity storeEntity = entity.getStore();
        if (storeEntity == null || "Y".equals(storeEntity.getDelYn())) {
            throw new RuntimeException("삭제된 가게 하위에 있는 메뉴 카테고리는 수정할 수 없습니다.");
        }

        // 점주 소유 여부 체크
        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        if (userEntity.getStore() == null || userEntity.getStore().getStoreId() != storeEntity.getStoreId()) {
            throw new RuntimeException("해당 가게에 대한 권한이 없습니다.");
        }

        // 기존 카테고리 리스트 가져오기 (삭제되지 않은 것만)
        List<MenuCategoryEntity> menuCategoryList = menuCategoryRepository
            .findByStoreAndDelYnOrderByDisplayOrderAsc(storeEntity, "N");

        // 원래 정렬 순서
        int oldOrder = entity.getDisplayOrder();
        // 새로운 정렬 순서
        int newOrder = request.getDisplayOrder();

        if (newOrder > menuCategoryList.size()) {
            newOrder = menuCategoryList.size();
        }

        // displayOrder 재정렬
        for (MenuCategoryEntity menuCategory : menuCategoryList) {
            // 나는 비교할 필요가 없으니 continue
            if (menuCategory.getMenuCaId() == entity.getMenuCaId()) {
                continue;
            }
            // oldOrder 보다 newOrder 가 크면 중간 카테고리들의 정렬순서를 -1 씩 수정하고, 작으면 +1 씩 수정함
            if (oldOrder < newOrder &&
                menuCategory.getDisplayOrder() > oldOrder && 
                menuCategory.getDisplayOrder() <= newOrder) {
                    
                menuCategory.setDisplayOrder(menuCategory.getDisplayOrder() - 1);
            } else if (oldOrder > newOrder &&
                menuCategory.getDisplayOrder() < oldOrder && 
                menuCategory.getDisplayOrder() >= newOrder) {
                    
                menuCategory.setDisplayOrder(menuCategory.getDisplayOrder() + 1);
            }
        }

        entity.setMenuCaName(request.getMenuCaName());
        entity.setDisplayOrder(newOrder);

        menuCategoryRepository.save(entity);
    }

    /**
     * 메뉴 등록하기
     * @param request 메뉴 객체
     * @param userId 사용자 아이디
     * @throws Exception
     */
    @Transactional
    public void createMenu(MenuDTO.Request request, String userId) throws Exception {

        MultipartFile mainImage = request.getMainImage();

        if (mainImage == null || mainImage.isEmpty()) {   // 메인 이미지가 없거나 빈 파일일 경우
            throw new RuntimeException("메인 이미지는 필수입니다.");
        }
        
        // 메뉴 카테고리 조회
        MenuCategoryEntity category = menuCategoryRepository.findById(request.getMenuCategoryId())
            .orElseThrow(() -> new RuntimeException("해당 메뉴 카테고리가 존재하지 않습니다."));

        if ("Y".equals(category.getDelYn())) {
            throw new RuntimeException("삭제된 메뉴 카테고리 하위엔 메뉴를 등록할 수 없습니다.");
        }

        StoreEntity store = category.getStore();
        if (store == null || "Y".equals(store.getDelYn())) {
            throw new RuntimeException("삭제된 가게 하위엔 메뉴를 등록할 수 없습니다.");
        }

        // 점주 소유 여부 체크
        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        if (userEntity.getStore() == null || userEntity.getStore().getStoreId() != store.getStoreId()) {
            throw new RuntimeException("해당 가게에 대한 권한이 없습니다.");
        }

        MenuEntity entity = new MenuEntity();
        entity.setMenuCategory(category);
        entity.setMenuName(request.getMenuName());
        entity.setDescription(request.getDescription());
        entity.setPrice(request.getPrice());
        entity.setSoldoutYn(request.getSoldoutYn());
        entity.setDelYn("N");

        category.addMenu(entity, true);   // 메뉴 카테고리와 메뉴 매핑

        // 메인 이미지 파일 업로드
        Map<String, Object> mainImageMap = fileUtils.uploadImageFiles(request.getMainImage(), webConfig.getMenuPath());

        if (mainImageMap != null) {
            MenuFileEntity fileEntity = new MenuFileEntity();
            fileEntity.setFileName(mainImageMap.get("fileName").toString());
            fileEntity.setStoredName(mainImageMap.get("storedFileName").toString());
            fileEntity.setFilePath(mainImageMap.get("filePath").toString());
            fileEntity.setFileThumbName(mainImageMap.get("thumbName").toString());
            fileEntity.setFileSize(request.getMainImage().getSize());

            entity.addFile(fileEntity, false);   // 메뉴와 메뉴 파일 매핑
        }

        menuRepository.save(entity);
    }

    /**
     * 메뉴 수정
     * @param request 메뉴 객체
     * @param userId 사용자 아이디
     * @throws Exception
     */
    @Transactional
    public void updateMenu(MenuDTO.Request request, String userId) throws Exception {

        // 1. 수정하기 위해 기존 정보를 불러온다 
        MenuEntity entity = menuRepository.getMenu(request.getMenuId())   
            .orElseThrow(()-> new RuntimeException("해당 메뉴가 존재하지 않습니다."));

        // 삭제 여부 확인
        if ("Y".equals(entity.getDelYn())) {
            throw new RuntimeException("삭제된 메뉴는 수정할 수 없습니다.");
        }

        MenuCategoryEntity category = entity.getMenuCategory();
        if (category == null || "Y".equals(category.getDelYn())) {
            throw new RuntimeException("삭제된 메뉴 카테고리 하위에 있는 메뉴는 수정할 수 없습니다.");
        }

        StoreEntity store = category.getStore();
        if (store == null || "Y".equals(store.getDelYn())) {
            throw new RuntimeException("삭제된 가게 하위에 있는 메뉴는 수정할 수 없습니다.");
        }

        // 점주 소유 여부 체크
        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        if (userEntity.getStore() == null || userEntity.getStore().getStoreId() != store.getStoreId()) {
            throw new RuntimeException("해당 가게에 대한 권한이 없습니다.");
        }

        MenuDTO.Detail detail = MenuDTO.Detail.of(entity);

        entity.setMenuName(request.getMenuName());
        entity.setDescription(request.getDescription());
        entity.setPrice(request.getPrice());
        entity.setSoldoutYn(request.getSoldoutYn());

        // 2. 메뉴 카테고리 변경시 수정한다
        if (request.getMenuCategoryId() != category.getMenuCaId()) {
            MenuCategoryEntity newCategory = menuCategoryRepository.findById(request.getMenuCategoryId())
                .orElseThrow(() -> new RuntimeException("해당 메뉴 카테고리가 존재하지 않습니다."));
            
            // 기존 카테고리에서 메뉴 제거
            entity.getMenuCategory().getMenuList().remove(entity);
            
            // 새 카테고리에 메뉴 추가
            newCategory.addMenu(entity, true);
            entity.setMenuCategory(newCategory);
        }
        
        // 3. 업로드 할 메인 이미지 파일이 있으면 업로드
        if (request.getMainImage() != null && !request.getMainImage().isEmpty()) {
     
            // 3-1 파일 업로드
            Map<String, Object> mainImageMap = fileUtils.uploadImageFiles(request.getMainImage(), webConfig.getMenuPath());

            // 3-2. 파일 등록
            // 파일이 있을 경우에만 파일 엔티티 생성
            if (mainImageMap != null) {
                MenuFileEntity fileEntity = new MenuFileEntity();
                fileEntity.setFileName(mainImageMap.get("fileName").toString());
                fileEntity.setStoredName(mainImageMap.get("storedFileName").toString());
                fileEntity.setFilePath(mainImageMap.get("filePath").toString());
                fileEntity.setFileThumbName(mainImageMap.get("thumbName").toString());
                fileEntity.setFileSize(request.getMainImage().getSize());

                // 기존 파일 연관관계 끊기
                MenuFileEntity oldFile = entity.getFile();
                
                if (oldFile != null) {
                    oldFile.setMenu(null);
                }

                entity.setFile(null);

                entity.addFile(fileEntity, true);
            }
        }

        menuRepository.save(entity);

        if (request.getMainImage() != null && !request.getMainImage().isEmpty()) {
            // 3-3. 기존 파일 삭제 (작업 도중 DB에 문제가 생길 수도 있기 때문에 물리적 파일 삭제는 제일 마지막에 진행)
            // 메뉴 상세 정보 DTO 가 가지고 있는 파일 정보로 삭제
            deleteImageFiles(detail.getFilePath(), detail.getStoredName(), detail.getFileThumbName());
        }
    }

    /**
     * 메뉴 옵션 그룹 등록하기
     * @param request 메뉴 옵션 그룹 객체
     * @param userId 사용자 아이디
     * @throws Exception
     */
    @Transactional
    public void createMenuOptionGroup(MenuOptionGroupDTO.Request request, String userId) throws Exception {

        // 메뉴 조회
        MenuEntity menuEntity = menuRepository.findById(request.getMenuId())
            .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));

        if ("Y".equals(menuEntity.getDelYn())) {
            throw new RuntimeException("삭제된 메뉴 하위엔 새로운 옵션 그룹을 등록할 수 없습니다.");
        }

        MenuCategoryEntity category = menuEntity.getMenuCategory();
        if (category == null || "Y".equals(category.getDelYn())) {
            throw new RuntimeException("삭제된 메뉴 카테고리 하위엔 새로운 옵션 그룹을 등록할 수 없습니다.");
        }

        StoreEntity store = category.getStore();
        if (store == null || "Y".equals(store.getDelYn())) {
            throw new RuntimeException("삭제된 가게 하위엔 새로운 옵션 그룹을 등록할 수 없습니다.");
        }

        // 점주 소유 여부 체크
        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        if (userEntity.getStore() == null || userEntity.getStore().getStoreId() != store.getStoreId()) {
            throw new RuntimeException("해당 가게에 대한 권한이 없습니다.");
        }

        // 기존 옵션 그룹 리스트 displayOrder 오름차순으로 가져오기
        List<MenuOptionGroupEntity> optionGroupList = menuOptionGroupRepository
            .findByMenuAndDelYnOrderByDisplayOrderAsc(menuEntity, "N");

        int newOrder = request.getDisplayOrder();

        // displayOrder 값이 기존 옵션 그룹 리스트 사이즈보다 크면 고정
        if (newOrder > optionGroupList.size() + 1) {
            newOrder = optionGroupList.size() + 1;
        }

        // displayOrder가 newOrder 이상인 항목들은 모두 +1 씩 밀어내기
        for (MenuOptionGroupEntity group : optionGroupList) {
            if (group.getDisplayOrder() >= newOrder) {
                group.setDisplayOrder(group.getDisplayOrder() + 1);
            }
        }

        // 옵션 그룹 엔티티 생성
        MenuOptionGroupEntity entity = new MenuOptionGroupEntity();
        entity.setMenu(menuEntity);
        entity.setMenuOptGrpName(request.getMenuOptGrpName());
        entity.setRequiredYn(request.getRequiredYn());
        entity.setDelYn("N");
        entity.setMinSelect(request.getMinSelect());
        entity.setMaxSelect(request.getMaxSelect());
        entity.setDisplayOrder(newOrder);

        menuEntity.addMenuOptionGroup(entity, true);   // 메뉴와 메뉴 옵션 그룹 매핑

        menuOptionGroupRepository.save(entity);
    }

    /**
     * 메뉴 옵션 그룹 수정하기
     * @param request 메뉴 옵션 그룹 객체
     * @param userId 사용자 아이디
     * @throws Exception
     */
    @Transactional
    public void updateMenuOptionGroup(MenuOptionGroupDTO.Request request, String userId) throws Exception {

        // 기존 옵션 그룹 조회
        MenuOptionGroupEntity entity = menuOptionGroupRepository.findById(request.getMenuOptGrpId())
            .orElseThrow(() -> new RuntimeException("해당 메뉴 옵션 그룹이 존재하지 않습니다."));
        
        // 삭제 여부 확인
        if ("Y".equals(entity.getDelYn())) {
            throw new RuntimeException("삭제된 옵션 그룹은 수정할 수 없습니다.");
        }

        MenuEntity menuEntity = entity.getMenu();
        if (menuEntity == null || "Y".equals(menuEntity.getDelYn())) {
            throw new RuntimeException("삭제된 메뉴 하위에 있는 옵션 그룹은 수정할 수 없습니다.");
        }

        MenuCategoryEntity category = menuEntity.getMenuCategory();
        if (category == null || "Y".equals(category.getDelYn())) {
            throw new RuntimeException("삭제된 메뉴 카테고리 하위에 있는 옵션 그룹은 수정할 수 없습니다.");
        }

        StoreEntity store = category.getStore();
        if (store == null || "Y".equals(store.getDelYn())) {
            throw new RuntimeException("삭제된 가게 하위에 있는 옵션 그룹은 수정할 수 없습니다.");
        }

        // 점주 소유 여부 체크
        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        if (userEntity.getStore() == null || userEntity.getStore().getStoreId() != store.getStoreId()) {
            throw new RuntimeException("해당 가게에 대한 권한이 없습니다.");
        }

        // 기존 옵션 그룹 리스트 가져오기 (삭제되지 않은 것만)
        List<MenuOptionGroupEntity> optionGroupList = menuOptionGroupRepository
            .findByMenuAndDelYnOrderByDisplayOrderAsc(menuEntity, "N");

        int oldOrder = entity.getDisplayOrder();
        int newOrder = request.getDisplayOrder();

        if (newOrder > optionGroupList.size()) {
            newOrder = optionGroupList.size();
        }

        // displayOrder 재정렬
        for (MenuOptionGroupEntity optionGroup : optionGroupList) {
            // 나는 비교할 필요가 없으니 continue
            if (optionGroup.getMenuOptGrpId() == entity.getMenuOptGrpId()) {
                continue;
            }

            // oldOrder 보다 newOrder 가 크면 중간 카테고리들의 정렬순서를 -1 씩 수정하고, 작으면 +1 씩 수정함
            if (oldOrder < newOrder &&
                optionGroup.getDisplayOrder() > oldOrder &&
                optionGroup.getDisplayOrder() <= newOrder) {

                optionGroup.setDisplayOrder(optionGroup.getDisplayOrder() - 1);
            } else if (oldOrder > newOrder &&
                    optionGroup.getDisplayOrder() < oldOrder &&
                    optionGroup.getDisplayOrder() >= newOrder) {

                optionGroup.setDisplayOrder(optionGroup.getDisplayOrder() + 1);
            }
        }

        entity.setMenuOptGrpName(request.getMenuOptGrpName());
        entity.setRequiredYn(request.getRequiredYn());
        entity.setMinSelect(request.getMinSelect());
        entity.setMaxSelect(request.getMaxSelect());
        entity.setDisplayOrder(newOrder);

        menuOptionGroupRepository.save(entity);
    }

    /**
     * 메뉴 옵션 등록하기
     * @param request 메뉴 옵션 객체
     * @param userId 사용자 아이디
     * @throws Exception
     */
    @Transactional
    public void createMenuOption(MenuOptionDTO.Request request, String userId) throws Exception {

        // 옵션 그룹 조회
        MenuOptionGroupEntity groupEntity = menuOptionGroupRepository.findById(request.getMenuOptGrpId())
            .orElseThrow(() -> new RuntimeException("해당 메뉴 옵션 그룹이 존재하지 않습니다."));

        if ("Y".equals(groupEntity.getDelYn())) {
            throw new RuntimeException("삭제된 메뉴 옵션 그룹 하위엔 새로운 메뉴 옵션을 등록할 수 없습니다.");
        }
        
        MenuEntity menuEntity = groupEntity.getMenu();
        if (menuEntity == null || "Y".equals(menuEntity.getDelYn())) {
            throw new RuntimeException("삭제된 메뉴 하위엔 새로운 메뉴 옵션을 등록할 수 없습니다.");
        }

        MenuCategoryEntity category = menuEntity.getMenuCategory();
        if (category == null || "Y".equals(category.getDelYn())) {
            throw new RuntimeException("삭제된 메뉴 카테고리 하위엔 새로운 메뉴 옵션을 등록할 수 없습니다.");
        }

        StoreEntity store = category.getStore();
        if (store == null || "Y".equals(store.getDelYn())) {
            throw new RuntimeException("삭제된 가게 하위엔 새로운 메뉴 옵션을 등록할 수 없습니다.");
        }

        // 점주 소유 여부 체크
        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        if (userEntity.getStore() == null || userEntity.getStore().getStoreId() != store.getStoreId()) {
            throw new RuntimeException("해당 가게에 대한 권한이 없습니다.");
        }
        

        // 옵션 엔티티 생성
        MenuOptionEntity entity = new MenuOptionEntity();
        entity.setMenuOptionGroup(groupEntity);
        entity.setMenuOptName(request.getMenuOptName());
        entity.setPrice(request.getPrice());
        entity.setAvailableYn(request.getAvailableYn());
        entity.setDelYn("N");
        entity.setMaxSelect(request.getMaxSelect());
        entity.setDisplayOrder(request.getDisplayOrder());

        groupEntity.addMenuOption(entity, true);  // 메뉴 옵션 그룹과 메뉴 옵션 매핑

        menuOptionRepository.save(entity);
    }

    /**
     * 메뉴 옵션 수정하기
     * @param request 메뉴 옵션 객체
     * @param userId 사용자 아이디
     * @throws Exception
     */
    @Transactional
    public void updateMenuOption(MenuOptionDTO.Request request, String userId) throws Exception {

        MenuOptionEntity entity = menuOptionRepository.findById(request.getMenuOptId())
            .orElseThrow(() -> new RuntimeException("해당 메뉴 옵션이 존재하지 않습니다."));

        // 삭제 여부 확인
        if ("Y".equals(entity.getDelYn())) {
            throw new RuntimeException("삭제된 옵션은 수정할 수 없습니다.");
        }

        MenuOptionGroupEntity groupEntity = entity.getMenuOptionGroup();
        if (groupEntity == null || "Y".equals(groupEntity.getDelYn())) {
            throw new RuntimeException("삭제된 메뉴 옵션 그룹 하위에 있는 메뉴 옵션은 수정할 수 없습니다.");
        }

        MenuEntity menuEntity = groupEntity.getMenu();
        if (menuEntity == null || "Y".equals(menuEntity.getDelYn())) {
            throw new RuntimeException("삭제된 메뉴 하위에 있는 메뉴 옵션은 수정할 수 없습니다.");
        }

        MenuCategoryEntity category = menuEntity.getMenuCategory();
        if (category == null || "Y".equals(category.getDelYn())) {
            throw new RuntimeException("삭제된 메뉴 카테고리 하위에 있는 메뉴 옵션은 수정할 수 없습니다.");
        }

        StoreEntity store = category.getStore();
        if (store == null || "Y".equals(store.getDelYn())) {
            throw new RuntimeException("삭제된 가게 하위에 있는 메뉴 옵션은 수정할 수 없습니다.");
        }

        // 점주 소유 여부 체크
        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        if (userEntity.getStore() == null || userEntity.getStore().getStoreId() != store.getStoreId()) {
            throw new RuntimeException("해당 가게에 대한 권한이 없습니다.");
        }

        entity.setMenuOptName(request.getMenuOptName());
        entity.setPrice(request.getPrice());
        entity.setAvailableYn(request.getAvailableYn());
        entity.setMaxSelect(request.getMaxSelect());
        entity.setDisplayOrder(request.getDisplayOrder());

        menuOptionRepository.save(entity);
    }

    /**
     * 메뉴 카테고리 삭제하기
     * @param menuCategoryId 메뉴 카테고리 아이디
     * @param userId 사용자 아이디
     * @throws Exception
     */
    @Transactional
    public void deleteMenuCategory(int menuCategoryId, String userId) throws Exception {

        MenuCategoryEntity entity = menuCategoryRepository.findById(menuCategoryId)
            .orElseThrow(() -> new RuntimeException("해당 메뉴 카테고리가 존재하지 않습니다."));

        // 이미 삭제된 경우 예외처리
        if ("Y".equals(entity.getDelYn())) {
            throw new RuntimeException("이미 삭제된 메뉴 카테고리입니다.");
        }

        StoreEntity storeEntity = entity.getStore();
        if (storeEntity == null || "Y".equals(storeEntity.getDelYn())) {
            throw new RuntimeException("삭제된 가게 하위에 있는 메뉴 카테고리는 삭제할 수 없습니다.");
        }

        // 점주 소유 여부 체크
        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        if (userEntity.getStore() == null || userEntity.getStore().getStoreId() != storeEntity.getStoreId()) {
            throw new RuntimeException("해당 가게에 대한 권한이 없습니다.");
        }

        int oldOrder = entity.getDisplayOrder();

        entity.setDelYn("Y");

        // 하위 메뉴까지 전부 삭제 처리
        if (entity.getMenuList() != null) {
            entity.getMenuList().forEach(menu -> menu.setDelYn("Y"));
        }

        // 기존 카테고리 리스트 가져오기
        List<MenuCategoryEntity> menuCategoryList = menuCategoryRepository
            .findByStoreAndDelYnOrderByDisplayOrderAsc(storeEntity, "N");

        // 삭제된 위치 이후의 카테고리들의 displayOrder를 -1 씩 당기기
        for (MenuCategoryEntity menuCategory : menuCategoryList) {
            if (menuCategory.getDisplayOrder() > oldOrder) {
                menuCategory.setDisplayOrder(menuCategory.getDisplayOrder() - 1);
            }
        }

        menuCategoryRepository.save(entity);
    }

    /**
     * 메뉴 삭제하기
     * @param menuId 메뉴 아이디
     * @param userId 사용자 아이디
     * @throws Exception
     */
    @Transactional
    public void deleteMenu(int menuId, String userId) throws Exception {

        MenuEntity entity = menuRepository.findById(menuId)
            .orElseThrow(() -> new RuntimeException("해당 메뉴가 존재하지 않습니다."));

        // 이미 삭제된 경우 예외처리
        if ("Y".equals(entity.getDelYn())) {
            throw new RuntimeException("이미 삭제된 메뉴입니다.");
        }
        
        MenuCategoryEntity category = entity.getMenuCategory();
        if (category == null || "Y".equals(category.getDelYn())) {
            throw new RuntimeException("삭제된 메뉴 카테고리 하위에 있는 메뉴는 삭제할 수 없습니다.");
        }

        StoreEntity store = category.getStore();
        if (store == null || "Y".equals(store.getDelYn())) {
            throw new RuntimeException("삭제된 가게 하위에 있는 메뉴는 삭제할 수 없습니다.");
        }

        // 점주 소유 여부 체크
        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        if (userEntity.getStore() == null || userEntity.getStore().getStoreId() != store.getStoreId()) {
            throw new RuntimeException("해당 가게에 대한 권한이 없습니다.");
        }

        entity.setDelYn("Y");

        // 하위 옵션 그룹까지 전부 삭제 처리
        if (entity.getMenuOptionGroupList() != null) {
            entity.getMenuOptionGroupList().forEach(group -> group.setDelYn("Y"));
        }

        menuRepository.save(entity);
    }

    /**
     * 메뉴 옵션 그룹 삭제하기
     * @param menuOptGrpId 메뉴 옵션 그룹 아이디
     * @param userId 사용자 아이디
     * @throws Exception
     */
    @Transactional
    public void deleteMenuOptionGroup(int menuOptGrpId, String userId) throws Exception {

        MenuOptionGroupEntity entity = menuOptionGroupRepository.findById(menuOptGrpId)
            .orElseThrow(() -> new RuntimeException("해당 메뉴 옵션 그룹이 존재하지 않습니다."));

        // 이미 삭제된 경우 예외처리
        if ("Y".equals(entity.getDelYn())) {
            throw new RuntimeException("이미 삭제된 옵션 그룹입니다.");
        }

        MenuEntity menuEntity = entity.getMenu();
        if (menuEntity == null || "Y".equals(menuEntity.getDelYn())) {
            throw new RuntimeException("삭제된 메뉴 하위에 있는 메뉴 옵션 그룹은 삭제할 수 없습니다.");
        }

        MenuCategoryEntity category = menuEntity.getMenuCategory();
        if (category == null || "Y".equals(category.getDelYn())) {
            throw new RuntimeException("삭제된 메뉴 카테고리 하위에 있는 메뉴 옵션 그룹은 삭제할 수 없습니다.");
        }

        StoreEntity store = category.getStore();
        if (store == null || "Y".equals(store.getDelYn())) {
            throw new RuntimeException("삭제된 가게 하위에 있는 메뉴 옵션 그룹은 삭제할 수 없습니다.");
        }

        // 점주 소유 여부 체크
        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        if (userEntity.getStore() == null || userEntity.getStore().getStoreId() != store.getStoreId()) {
            throw new RuntimeException("해당 가게에 대한 권한이 없습니다.");
        }

        entity.setDelYn("Y");

        // 하위 메뉴 옵션까지 전부 삭제 처리
        if (entity.getMenuOptionList() != null) {
            entity.getMenuOptionList().forEach(option -> option.setDelYn("Y"));
        }

        // 삭제된 위치 이후의 옵션 그룹들의 displayOrder를 -1 씩 당기기
        List<MenuOptionGroupEntity> optionGroupList = menuOptionGroupRepository
            .findByMenuAndDelYnOrderByDisplayOrderAsc(menuEntity, "N");

        int oldOrder = entity.getDisplayOrder();

        for (MenuOptionGroupEntity optionGroup : optionGroupList) {
            if (optionGroup.getDisplayOrder() > oldOrder) {
                optionGroup.setDisplayOrder(optionGroup.getDisplayOrder() - 1);
            }
        }

        menuOptionGroupRepository.save(entity);
    }

    /**
     * 메뉴 옵션 삭제하기
     * @param menuOptId 메뉴 옵션 아이디
     * @param userId 사용자 아이디
     * @throws Exception
     */
    @Transactional
    public void deleteMenuOption(int menuOptId, String userId) throws Exception {

        MenuOptionEntity entity = menuOptionRepository.findById(menuOptId)
            .orElseThrow(() -> new RuntimeException("해당 메뉴 옵션이 존재하지 않습니다."));

        // 이미 삭제된 경우 예외처리
        if ("Y".equals(entity.getDelYn())) {
            throw new RuntimeException("이미 삭제된 메뉴 옵션입니다.");
        }

        MenuOptionGroupEntity groupEntity = entity.getMenuOptionGroup();
        if (groupEntity == null || "Y".equals(groupEntity.getDelYn())) {
            throw new RuntimeException("삭제된 메뉴 옵션 그룹 하위에 있는 메뉴 옵션은 삭제할 수 없습니다.");
        }

        MenuEntity menuEntity = groupEntity.getMenu();
        if (menuEntity == null || "Y".equals(menuEntity.getDelYn())) {
            throw new RuntimeException("삭제된 메뉴 하위에 있는 메뉴 옵션은 삭제할 수 없습니다.");
        }

        MenuCategoryEntity category = menuEntity.getMenuCategory();
        if (category == null || "Y".equals(category.getDelYn())) {
            throw new RuntimeException("삭제된 메뉴 카테고리 하위에 있는 메뉴 옵션은 삭제할 수 없습니다.");
        }

        StoreEntity store = category.getStore();
        if (store == null || "Y".equals(store.getDelYn())) {
            throw new RuntimeException("삭제된 가게 하위에 있는 메뉴 옵션은 삭제할 수 없습니다.");
        }

        // 점주 소유 여부 체크
        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        if (userEntity.getStore() == null || userEntity.getStore().getStoreId() != store.getStoreId()) {
            throw new RuntimeException("해당 가게에 대한 권한이 없습니다.");
        }

        entity.setDelYn("Y");

        menuOptionRepository.save(entity);
    }

    /**
     * 파일 삭제과정 공통화해서 분리
     * @param imageFilePath 이미지 파일 경로
     * @param storedName 저장 파일명
     * @param fileThumbName 파일 썸네일명
     * @throws Exception
     */
    private void deleteImageFiles(String imageFilePath, String storedName, String fileThumbName) throws Exception {
        // 파일 정보
        String fullPath = imageFilePath + storedName;
        String thumbFilePath = webConfig.getMenuPath() + "thumb" + File.separator + fileThumbName;

        try {
            File file = new File(fullPath);

            if (file.exists()) {
                // 원본 파일 삭제
                fileUtils.deleteFile(fullPath);
            } else {
                log.warn("삭제하려는 원본 파일이 없습니다. path={}", fullPath);
            }

            File thumbFile = new File(thumbFilePath);

            if (thumbFile.exists()) {
                // 썸네일 파일 삭제
                fileUtils.deleteFile(thumbFilePath);
            } else {
                log.warn("삭제하려는 썸네일 파일이 없습니다. path={}", thumbFilePath);
            }

        } catch (Exception e) {
            // 파일 삭제 실패 시 전체 트랜잭션을 깨뜨리지 않도록 함
            log.error("파일 삭제 중 오류가 발생했습니다. {}", e.getMessage(), e);
        }
    }
}
