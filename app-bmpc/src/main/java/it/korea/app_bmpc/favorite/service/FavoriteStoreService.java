package it.korea.app_bmpc.favorite.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.korea.app_bmpc.common.dto.PageInfo;
import it.korea.app_bmpc.favorite.dto.FavoriteStoreDTO;
import it.korea.app_bmpc.favorite.entity.FavoriteStoreEntity;
import it.korea.app_bmpc.favorite.repository.FavoriteStoreRepository;
import it.korea.app_bmpc.store.entity.StoreEntity;
import it.korea.app_bmpc.store.repository.StoreRepository;
import it.korea.app_bmpc.user.entity.UserEntity;
import it.korea.app_bmpc.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteStoreService {

    private final FavoriteStoreRepository favoriteStoreRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    /**
     * 찜 리스트 가져오기
     * @param pageable 페이징 객체
     * @param userId 사용자 아이디
     * @return
     * @throws Exception
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getFavoriteStoreList(Pageable pageable, String userId) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();

        Page<FavoriteStoreEntity> pageList = favoriteStoreRepository.findAllByUser_userIdAndStore_delYn(userId, "N", pageable);

        List<FavoriteStoreDTO.Response> favoriteList = pageList.getContent().stream().map(FavoriteStoreDTO.Response::of).toList();

        resultMap.put("content", favoriteList);
        resultMap.put("pageInfo", PageInfo.of(pageList));
        
        return resultMap;
    }

    /**
     * 찜 목록에 존재하는지 여부 체크하기
     * @param userId 사용자 아이디
     * @param storeId 가게 아이디
     * @return
     * @throws Exception
     */
    @Transactional(readOnly = true)
    public boolean isFavoriteStore(String userId, int storeId) throws Exception {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        StoreEntity store = storeRepository.findById(storeId)
            .orElseThrow(() -> new RuntimeException("해당 가게가 존재하지 않습니다."));

        // 이미 찜한 가게인지 여부 체크
        return favoriteStoreRepository.existsByUserAndStore(user, store);
    }

    /**
     * 찜 등록하기
     * @param requestDTO 찜 객체
     * @throws Exception
     */
    @Transactional
    public void addFavoriteStore(FavoriteStoreDTO.Request requestDTO) throws Exception {

        UserEntity user = userRepository.findById(requestDTO.getUserId())
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        StoreEntity store = storeRepository.findById(requestDTO.getStoreId())
            .orElseThrow(() -> new RuntimeException("해당 가게가 존재하지 않습니다."));

        // 가게 삭제 여부 판단
        if ("Y".equals(store.getDelYn())) {
            throw new RuntimeException("삭제된 가게는 찜할 수 없습니다.");
        }

        // 이미 찜한 가게인지 중복 체크
        boolean exists = favoriteStoreRepository.existsByUserAndStore(user, store);
        if (exists) {
            throw new RuntimeException("이미 찜한 가게입니다.");
        }

        FavoriteStoreEntity favoriteStoreEntity = new FavoriteStoreEntity();
        favoriteStoreEntity.setUser(user);
        favoriteStoreEntity.setStore(store);

        favoriteStoreRepository.save(favoriteStoreEntity);
    }

    /**
     * 찜 목록에서 삭제하기
     * @param userId 사용자 아이디
     * @param storeId 가게 아이디
     * @throws Exception
     */
    @Transactional
    public void deleteFavoriteStore(String userId, int storeId) throws Exception {

        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        StoreEntity store = storeRepository.findById(storeId)
            .orElseThrow(() -> new RuntimeException("해당 가게가 존재하지 않습니다."));

        // 찜 엔티티 가져오기
        FavoriteStoreEntity favoriteStoreEntity = favoriteStoreRepository.findByUserAndStore(user, store)
            .orElseThrow(() -> new RuntimeException("해당 가게가 찜 목록에 없습니다."));

        favoriteStoreRepository.delete(favoriteStoreEntity);
    }
}
