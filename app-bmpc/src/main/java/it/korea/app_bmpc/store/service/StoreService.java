package it.korea.app_bmpc.store.service;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import it.korea.app_bmpc.common.dto.PageVO;
import it.korea.app_bmpc.common.utils.FileUtils;
import it.korea.app_bmpc.config.WebConfig;
import it.korea.app_bmpc.order.entity.OrderEntity;
import it.korea.app_bmpc.order.repository.OrderRepository;
import it.korea.app_bmpc.store.dto.CategoryDTO;
import it.korea.app_bmpc.store.dto.StoreDTO;
import it.korea.app_bmpc.store.dto.StoreFileDTO;
import it.korea.app_bmpc.store.dto.StoreSearchDTO;
import it.korea.app_bmpc.store.entity.StoreCategoryEntity;
import it.korea.app_bmpc.store.entity.StoreEntity;
import it.korea.app_bmpc.store.entity.StoreFileEntity;
import it.korea.app_bmpc.store.entity.StoreHourEntity;
import it.korea.app_bmpc.store.repository.CategoryRepository;
import it.korea.app_bmpc.store.repository.StoreRepository;
import it.korea.app_bmpc.store.repository.StoreSearchSpecification;
import it.korea.app_bmpc.user.entity.UserEntity;
import it.korea.app_bmpc.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {

    private final WebConfig webConfig;

    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final FileUtils fileUtils;

    /**
     * 가게 카테고리 리스트 가져오기 
     * @return
     * @throws Exception
     */
    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoryList() throws Exception {
        return categoryRepository.findAll().stream().map(CategoryDTO::of).toList();
    }

    /**
     * 가게 리스트 가져오기
     * @param pageable 페이징 객체
     * @param searchDTO 검색 내용
     * @return
     * @throws Exception
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStoreList(Pageable pageable, StoreSearchDTO searchDTO) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();

        Page<StoreEntity> pageList = null;

        StoreSearchSpecification searchSpecification = new StoreSearchSpecification(searchDTO);
        pageList = storeRepository.findAll(searchSpecification, pageable);

        List<StoreDTO.Response> storeList = pageList.getContent().stream().map(StoreDTO.Response::of).toList();

        PageVO pageVO = new PageVO();
        pageVO.setData(pageList.getNumber(), (int) pageList.getTotalElements());

        resultMap.put("total", pageList.getTotalElements());
        resultMap.put("content", storeList);
        resultMap.put("pageHTML", pageVO.pageHTML());
        resultMap.put("page", pageList.getNumber());
        
        return resultMap;
    }

    /**
     * 가게 상세정보 가져오기
     * @param storeId 가게 아이디
     * @return
     * @throws Exception
     */
    @Transactional(readOnly = true)
    public StoreDTO.Detail getStore(int storeId) throws Exception {
        return StoreDTO.Detail.of(storeRepository.getStore(storeId)
            .orElseThrow(()-> new RuntimeException("해당 가게가 존재하지 않습니다.")));
    }

    /**
     * 내 가게 상세정보 가져오기
     * @param userId 사용자 아이디
     * @return
     * @throws Exception
     */
    @Transactional(readOnly = true)
    public StoreDTO.OwnerDetail getOwnerStore(String userId) throws Exception {
        
        // 호출한 사용자 조회
        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        // 가게를 가지고 있는지 확인
        StoreEntity storeEntity = userEntity.getStore();
        if (storeEntity == null) {
            // 등록된 가게가 없으면 그냥 빈 데이터 돌려주기
            return new StoreDTO.OwnerDetail();
        }

        int storeId = storeEntity.getStoreId();

        return StoreDTO.OwnerDetail.of(storeRepository.getStore(storeId)
            .orElseThrow(()-> new RuntimeException("해당 가게가 존재하지 않습니다.")));
    }

    /**
     * 가게 등록하기
     * @param request 가게 객체
     * @param userId 사용자 아이디
     * @throws Exception
     */
    @Transactional
    public void createStore(StoreDTO.Request request, String userId) throws Exception {

        // 호출한 사용자 조회
        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        // 중복 가게 등록 방지
        if (userEntity.getStore() != null) {
            throw new RuntimeException("이미 등록한 가게가 있습니다. 한 점주는 하나의 가게만 등록 가능합니다.");
        }
        
        MultipartFile mainImage = request.getMainImage();

        if (mainImage == null || mainImage.isEmpty()) {   // 메인 이미지가 없거나 빈 파일일 경우
            throw new RuntimeException("메인 이미지는 필수입니다.");
        }

        StoreEntity entity = new StoreEntity();
        entity.setStoreName(request.getStoreName());
        entity.setBranchName(request.getBranchName());
        entity.setPhone(request.getPhone());
        entity.setAddr(request.getAddr());
        entity.setAddrDetail(request.getAddrDetail());
        entity.setRatingAvg(BigDecimal.ZERO);
        entity.setReviewCount(0);
        entity.setMinPrice(request.getMinPrice());
        entity.setOrigin(request.getOrigin());
        entity.setNotice(request.getNotice());
        entity.setDelYn("N");

        // 카테고리 매핑
        for (Integer caId : request.getCategoryIds()) {
            StoreCategoryEntity sc = new StoreCategoryEntity();
            sc.setCategory(categoryRepository.findById(caId)
                .orElseThrow(()-> new RuntimeException("해당 카테고리가 존재하지 않습니다.")));

            entity.addCategory(sc, false);
        }

        // 영업시간 매핑
        for (StoreDTO.HourDTO hourDto : request.getHourList()) {
            StoreHourEntity hourEntity = new StoreHourEntity();
            hourEntity.setDayOfWeek(hourDto.getDayOfWeek());
            hourEntity.setOpenTime(hourDto.getOpenTime());
            hourEntity.setCloseTime(hourDto.getCloseTime());
            hourEntity.setCloseYn(hourDto.getCloseYn());

            entity.addHour(hourEntity, false);
        }

        // 메인 이미지 파일 업로드
        Map<String, Object> mainImageMap = fileUtils.uploadImageFiles(request.getMainImage(), webConfig.getStorePath());


        // 메인 이미지 파일이 있을 경우에만 파일 엔티티 생성
        if (mainImageMap != null) {  
            StoreFileEntity fileEntity = new StoreFileEntity();
            fileEntity.setFileName(mainImageMap.get("fileName").toString());
            fileEntity.setStoredName(mainImageMap.get("storedFileName").toString());
            fileEntity.setFilePath(mainImageMap.get("filePath").toString());
            fileEntity.setFileThumbName(mainImageMap.get("thumbName").toString());
            fileEntity.setFileSize(request.getMainImage().getSize());
            fileEntity.setMainYn("Y");

            entity.addFiles(fileEntity, false);  // 가게 엔티티와 파일 엔티티 관계를 맺어줌
        }

        List<MultipartFile> imageList = request.getImage();

        if (imageList != null && imageList.size() > 0) {
            for (MultipartFile image : imageList) {
                if (image != null && !image.isEmpty()) {
                    // 기타 이미지 파일 업로드
                    Map<String, Object> imageMap = fileUtils.uploadImageFiles(image, webConfig.getStorePath());

                    // 기타 이미지 파일이 있을 경우에만 파일 엔티티 생성
                    if (imageMap != null) {
                        StoreFileEntity fileEntity = new StoreFileEntity();
                        fileEntity.setFileName(imageMap.get("fileName").toString());
                        fileEntity.setStoredName(imageMap.get("storedFileName").toString());
                        fileEntity.setFilePath(imageMap.get("filePath").toString());
                        fileEntity.setFileThumbName(imageMap.get("thumbName").toString());
                        fileEntity.setFileSize(image.getSize());
                        fileEntity.setMainYn("N");
                        
                        entity.addFiles(fileEntity, false);  // 가게 엔티티와 파일 엔티티 관계를 맺어줌
                    }
                }
            }
        }

        // 가게 저장
        storeRepository.save(entity);

        // 사용자와 가게 매핑
        userEntity.setStore(entity);
        userRepository.save(userEntity);
    }

    /**
     * 가게 수정하기
     * @param request 가게 객체
     * @param userId 사용자 아이디
     * @throws Exception
     */
    @Transactional
    public void updateStore(StoreDTO.Request request, String userId) throws Exception {

        // 1. 수정하기 위해 기존 정보를 불러온다 
        StoreEntity entity = storeRepository.getStore(request.getStoreId())   
            .orElseThrow(()-> new RuntimeException("해당 가게가 존재하지 않습니다."));

        if ("Y".equals(entity.getDelYn())) {
            throw new RuntimeException("삭제된 가게는 수정할 수 없습니다.");
        }

        // 점주 소유 여부 확인
        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        if (userEntity.getStore() == null || userEntity.getStore().getStoreId() != entity.getStoreId()) {
            throw new RuntimeException("해당 가게를 수정할 권한이 없습니다.");
        }

        StoreDTO.Detail detail = StoreDTO.Detail.of(entity);

        entity.setStoreName(request.getStoreName());
        entity.setBranchName(request.getBranchName());
        entity.setPhone(request.getPhone());
        entity.setAddr(request.getAddr());
        entity.setAddrDetail(request.getAddrDetail());
        entity.setMinPrice(request.getMinPrice());
        entity.setOrigin(request.getOrigin());
        entity.setNotice(request.getNotice());

        // 카테고리 수정
        entity.getCategoryList().clear(); // 기존 카테고리 전부 삭제
        for (Integer caId : request.getCategoryIds()) {
            StoreCategoryEntity sc = new StoreCategoryEntity();
            sc.setCategory(categoryRepository.findById(caId)
                .orElseThrow(() -> new RuntimeException("해당 카테고리가 존재하지 않습니다.")));
            entity.addCategory(sc, true);
        }

        // 영업시간 수정
        entity.getHourList().clear(); // 기존 영업시간 초기화
        for (StoreDTO.HourDTO hourDto : request.getHourList()) {
            StoreHourEntity hourEntity = new StoreHourEntity();
            hourEntity.setDayOfWeek(hourDto.getDayOfWeek());
            hourEntity.setOpenTime(hourDto.getOpenTime());
            hourEntity.setCloseTime(hourDto.getCloseTime());
            hourEntity.setCloseYn(hourDto.getCloseYn());

            entity.addHour(hourEntity, true);
        }

        // 2. 업로드 할 메인 이미지 파일이 있으면 업로드
        if (request.getMainImage() != null && !request.getMainImage().isEmpty()) {
            // 2-1. 파일 업로드
            Map<String, Object> fileMap = fileUtils.uploadImageFiles(request.getMainImage(), webConfig.getStorePath());   // 파일 업로드 과정 공통화해서 분리

            entity.getFileList().removeIf(file -> "Y".equals(file.getMainYn()));  // 메인 이미지만 삭제
            
            // 2-2. 파일 등록
            // 파일이 있을 경우에만 파일 엔티티 생성
            if (fileMap != null) {  
                StoreFileEntity fileEntity = new StoreFileEntity();
                fileEntity.setFileName(fileMap.get("fileName").toString());
                fileEntity.setStoredName(fileMap.get("storedFileName").toString());
                fileEntity.setFilePath(fileMap.get("filePath").toString());
                fileEntity.setFileThumbName(fileMap.get("thumbName").toString());
                fileEntity.setFileSize(request.getMainImage().getSize());
                fileEntity.setMainYn("Y");

                // 파일만 수정했을 경우에도 가게 entity 의 updateDate 를 갱신하기 위해서 isUpdate 값을 true 로 줌.
                entity.addFiles(fileEntity, true);  // 가게 엔티티와 파일 엔티티 관계를 맺어줌
            } else {
                throw new RuntimeException("메인 이미지 업로드 중 오류가 발생했습니다.");
            }
        }

        List<MultipartFile> imageList = request.getImage();
        boolean isFirst = true;   // 기타 이미지 파일 업로드를 처음 시도하는지 여부

        // 2. 업로드 할 기타 이미지 파일이 있으면 업로드
        if (imageList != null && imageList.size() > 0) {
            for (MultipartFile image : imageList) {
                if (image != null && !image.isEmpty()) {
                    // 2-1. 파일 업로드
                    Map<String, Object> fileMap = fileUtils.uploadImageFiles(image, webConfig.getStorePath());   // 파일 업로드 과정 공통화해서 분리

                    if (isFirst) {   // 기타 이미지 파일 업로드를 처음 시도하는 경우는...
                        entity.getFileList().removeIf(file -> "N".equals(file.getMainYn()));   // 기타 이미지만 삭제
                        isFirst = false;
                    }

                    // 2-2. 파일 등록
                    // 파일이 있을 경우에만 파일 엔티티 생성
                    if (fileMap != null) {  
                        StoreFileEntity fileEntity = new StoreFileEntity();
                        fileEntity.setFileName(fileMap.get("fileName").toString());
                        fileEntity.setStoredName(fileMap.get("storedFileName").toString());
                        fileEntity.setFilePath(fileMap.get("filePath").toString());
                        fileEntity.setFileThumbName(fileMap.get("thumbName").toString());
                        fileEntity.setFileSize(image.getSize());
                        fileEntity.setMainYn("N");

                        // 파일만 수정했을 경우에도 가게 entity 의 updateDate 를 갱신하기 위해서 isUpdate 값을 true 로 줌.
                        entity.addFiles(fileEntity, true);  // 가게 엔티티와 파일 엔티티 관계를 맺어줌
                    } else {
                        throw new RuntimeException("기타 이미지 업로드 중 오류가 발생했습니다.");
                    }
                }
            }
        }

        storeRepository.save(entity);

        if (request.getMainImage() != null && !request.getMainImage().isEmpty()) {
            // 2-3. 기존 파일 삭제 (작업 도중 DB에 문제가 생길 수도 있기 때문에 물리적 파일 삭제는 제일 마지막에 진행)
            // 가게 상세 정보 DTO 가 가지고 있는 파일 DTO 리스트 순회
            if (detail.getFileList() != null && detail.getFileList().size() > 0) {
                for (StoreFileDTO fileDTO : detail.getFileList()) {
                    if (fileDTO.getMainYn().equals("Y")) {
                        deleteImageFiles(fileDTO);

                        break;   // 메인 이미지 파일은 하나뿐이기 때문에 지웠다면 반복문을 더 순회할 필요가 없기 때문에 바로 break;
                    }
                }
            }
        }

        if (imageList != null && imageList.size() > 0) {
            for (MultipartFile image : imageList) {
                if (image != null && !image.isEmpty()) {
                    // 2-3. 기존 파일 삭제 (작업 도중 DB에 문제가 생길 수도 있기 때문에 물리적 파일 삭제는 제일 마지막에 진행)
                    // 가게 상세 정보 DTO 가 가지고 있는 파일 DTO 리스트 순회
                    if (detail.getFileList() != null && detail.getFileList().size() > 0) {
                        for (StoreFileDTO fileDTO : detail.getFileList()) {
                            if (fileDTO.getMainYn().equals("N")) {
                                deleteImageFiles(fileDTO);
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * 가게 삭제하기
     * @param storeId 가게 아이디
     * @param userId 사용자 아이디
     * @throws Exception
     */
    @Transactional
    public void deleteStore(int storeId, String userId) throws Exception {

        StoreEntity entity = storeRepository.getStore(storeId)   // fetch join 을 사용한 getStore 메서드 호출
            .orElseThrow(()-> new RuntimeException("해당 가게가 존재하지 않습니다."));

        if ("Y".equals(entity.getDelYn())) {
            throw new RuntimeException("이미 삭제된 가게입니다.");
        }

        // 점주 소유 여부 확인
        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        if (userEntity.getStore() == null || userEntity.getStore().getStoreId() != entity.getStoreId()) {
            throw new RuntimeException("해당 가게를 수정할 권한이 없습니다.");
        }

        // 현재 내 가게에 주문완료 상태의 주문이 있는지 확인
        boolean isOrderExists = orderRepository.existsByStoreAndStatus(entity, "주문완료");
        if (isOrderExists) {
            throw new RuntimeException("현재 '주문완료' 상태의 주문이 존재하여 아직 가게를 삭제할 수 없습니다.");
        }

        entity.setDelYn("Y");  // 삭제 여부 Y로 변경 
        
        // 메뉴 카테고리 -> 메뉴 -> 메뉴옵션그룹 -> 메뉴옵션의 삭제 여부도 Y로 변경
        if (entity.getMenuCategoryList() != null && !entity.getMenuCategoryList().isEmpty()) {
            entity.getMenuCategoryList().forEach(menuCategory -> {
                menuCategory.setDelYn("Y");

                if (menuCategory.getMenuList() != null && !menuCategory.getMenuList().isEmpty()) {
                    menuCategory.getMenuList().forEach(menu -> {
                        menu.setDelYn("Y");

                        if (menu.getMenuOptionGroupList() != null && !menu.getMenuOptionGroupList().isEmpty()) {
                            menu.getMenuOptionGroupList().forEach(optionGroup -> {
                                optionGroup.setDelYn("Y");

                                if (optionGroup.getMenuOptionList() != null && !optionGroup.getMenuOptionList().isEmpty()) {
                                    optionGroup.getMenuOptionList().forEach(option -> {
                                        option.setDelYn("Y");
                                    });
                                }
                            });
                        }
                    });
                }
            });
        }

        storeRepository.save(entity);


        // 점주의 storeId 비우기
        userEntity.setStore(null);
        userRepository.save(userEntity);

        // 가게의 삭제 여부를 Y 로 변경만하고 가게의 이미지들은 삭제하지 않음
        // 왜냐하면 주문 내역 등에서 해당 가게의 상세 정보를 보여줘야 하기 때문
    }

    /**
     * 가게 삭제하기 (어드민)
     * @param storeId 가게 아이디
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED) // AdminUserService의 회원 삭제 메서드에서 이 메서드를 호출 시 같은 트랜잭션을 공유하도록 함
    public void deleteStoreByAdmin(int storeId) throws Exception {

        StoreEntity entity = storeRepository.getStore(storeId)   // fetch join 을 사용한 getStore 메서드 호출
            .orElseThrow(()-> new RuntimeException("해당 가게가 존재하지 않습니다."));

        if ("Y".equals(entity.getDelYn())) {
            throw new RuntimeException("이미 삭제된 가게입니다.");
        }

        // 주문완료 상태의 주문을 모두 조회
        List<OrderEntity> completedOrders = orderRepository.findAllByStoreAndStatus(entity, "주문완료");

        // 주문완료 상태의 주문들이 아직 존재한다면 모두 취소 처리하고 보유금을 원복시킴
        if (!completedOrders.isEmpty()) {
            completedOrders.forEach(order -> {

                order.setStatus("주문취소");

                UserEntity user = order.getUser();
                int totalPrice = order.getTotalPrice();
                user.setDeposit(user.getDeposit() + totalPrice);  // 보유금 원복

                userRepository.save(user);
                orderRepository.save(order);
            });
        }

        entity.setDelYn("Y");  // 삭제 여부 Y로 변경 
        
        // 메뉴 카테고리 -> 메뉴 -> 메뉴옵션그룹 -> 메뉴옵션의 삭제 여부도 Y로 변경
        if (entity.getMenuCategoryList() != null && !entity.getMenuCategoryList().isEmpty()) {
            entity.getMenuCategoryList().forEach(menuCategory -> {
                menuCategory.setDelYn("Y");

                if (menuCategory.getMenuList() != null && !menuCategory.getMenuList().isEmpty()) {
                    menuCategory.getMenuList().forEach(menu -> {
                        menu.setDelYn("Y");

                        if (menu.getMenuOptionGroupList() != null && !menu.getMenuOptionGroupList().isEmpty()) {
                            menu.getMenuOptionGroupList().forEach(optionGroup -> {
                                optionGroup.setDelYn("Y");

                                if (optionGroup.getMenuOptionList() != null && !optionGroup.getMenuOptionList().isEmpty()) {
                                    optionGroup.getMenuOptionList().forEach(option -> {
                                        option.setDelYn("Y");
                                    });
                                }
                            });
                        }
                    });
                }
            });
        }

        storeRepository.save(entity);

        // 가게를 소유한 점주를 찾아서 점주의 storeId 비우기
        userRepository.findByStore(entity).ifPresent(owner -> {
            owner.setStore(null); 
            userRepository.save(owner);
        });

        // 가게의 삭제 여부를 Y 로 변경만하고 가게의 이미지들은 삭제하지 않음
        // 왜냐하면 주문 내역 등에서 해당 가게의 상세 정보를 보여줘야 하기 때문
    }

    /**
     * 최근 한달간 인기 가게 top 10 리스트 가져오기 (with 캐시 저장)
     * @return
     */
    @Cacheable("popularStoreList")   // 캐시 저장
    @Transactional(readOnly = true)
    public List<StoreDTO.Popular> getPopularStoreList() {

        List<OrderEntity> orderEntityList =
            orderRepository.findByStatusAndOrderDateAfter("배달완료", LocalDateTime.now().minusMonths(1));

        // 가게별 주문수 맵에 저장하기
        Map<StoreEntity, Integer> storeCountMap = new HashMap<>();
        for (OrderEntity orderEntity : orderEntityList) {
            StoreEntity storeEntity = orderEntity.getStore();
            storeCountMap.put(storeEntity, storeCountMap.getOrDefault(storeEntity, 0) + 1);
        }

        // 주문수 기준으로 내림차순 정렬
        List<Map.Entry<StoreEntity, Integer>> storeCountList = new ArrayList<>(storeCountMap.entrySet());
        storeCountList.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // 상위 10개만 DTO로 변환
        List<StoreDTO.Popular> result = new ArrayList<>();
        int count = 0;
        for (Map.Entry<StoreEntity, Integer> entry : storeCountList) {
            if (count++ >= 10) {
                break;
            }
            result.add(StoreDTO.Popular.of(entry.getKey(), entry.getValue()));
        }

        return result;
    }

    /**
     * 파일 삭제과정 공통화해서 분리
     * @param dto 가게 파일 정보 dto
     * @throws Exception
     */
    private void deleteImageFiles(StoreFileDTO dto) throws Exception {
        // 파일 정보
        String fullPath = dto.getFilePath() + dto.getStoredName();
        String thumbFilePath = webConfig.getStorePath() + "thumb" + File.separator + dto.getFileThumbName();

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
