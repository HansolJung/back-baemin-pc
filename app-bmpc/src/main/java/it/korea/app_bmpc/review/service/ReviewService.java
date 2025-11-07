package it.korea.app_bmpc.review.service;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import it.korea.app_bmpc.admin.dto.AdminReviewSearchDTO;
import it.korea.app_bmpc.common.dto.PageVO;
import it.korea.app_bmpc.common.utils.FileUtils;
import it.korea.app_bmpc.config.WebConfig;
import it.korea.app_bmpc.order.entity.OrderEntity;
import it.korea.app_bmpc.order.repository.OrderRepository;
import it.korea.app_bmpc.review.dto.ReviewDTO;
import it.korea.app_bmpc.review.dto.ReviewFileDTO;
import it.korea.app_bmpc.review.dto.ReviewReplyDTO;
import it.korea.app_bmpc.review.entity.ReviewEntity;
import it.korea.app_bmpc.review.entity.ReviewFileEntity;
import it.korea.app_bmpc.review.entity.ReviewReplyEntity;
import it.korea.app_bmpc.review.repository.ReviewReplyRepository;
import it.korea.app_bmpc.review.repository.ReviewRepository;
import it.korea.app_bmpc.review.repository.ReviewSearchSpecification;
import it.korea.app_bmpc.store.entity.StoreEntity;
import it.korea.app_bmpc.store.repository.StoreRepository;
import it.korea.app_bmpc.user.entity.UserEntity;
import it.korea.app_bmpc.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final WebConfig webConfig;

    private final ReviewRepository reviewRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final FileUtils fileUtils;

    /**
     * 가게의 리뷰 리스트 가져오기
     * @param pageable 페이징 객체
     * @param storeId 가게 아이디
     * @return
     * @throws Exception
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStoreReviewList(Pageable pageable, int storeId) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();

        //Page<ReviewEntity> pageList = reviewRepository.findAllByStore_storeIdAndDelYn(storeId, "N", pageable);
        Page<ReviewEntity> pageList = reviewRepository.findAllByStoreId(storeId, "N", pageable);

        List<ReviewDTO.Response> reviewList = pageList.getContent().stream().map(ReviewDTO.Response::of).toList();

        PageVO pageVO = new PageVO();
        pageVO.setData(pageList.getNumber(), (int) pageList.getTotalElements());

        resultMap.put("total", pageList.getTotalElements());
        resultMap.put("content", reviewList);
        resultMap.put("pageHTML", pageVO.pageHTML());
        resultMap.put("page", pageList.getNumber());
        
        return resultMap;
    }

    /**
     * 내 가게의 리뷰 리스트 가져오기
     * @param pageable 페이징 객체
     * @param storeId 가게 아이디
     * @return
     * @throws Exception
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getOwnerStoreReviewList(Pageable pageable, String userId) throws Exception {

        // 점주 소유 여부 확인
        UserEntity ownerEntity = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        StoreEntity storeEntity = ownerEntity.getStore();
        if (storeEntity == null) {
            throw new RuntimeException("등록된 가게가 없습니다.");
        }

        Map<String, Object> resultMap = new HashMap<>();

        //Page<ReviewEntity> pageList = reviewRepository.findAllByStore_storeIdAndDelYn(storeEntity.getStoreId(), "N", pageable);
        Page<ReviewEntity> pageList = reviewRepository.findAllByStoreId(storeEntity.getStoreId(), "N", pageable);

        List<ReviewDTO.DetailResponse> reviewList = pageList.getContent().stream().map(ReviewDTO.DetailResponse::of).toList();

        PageVO pageVO = new PageVO();
        pageVO.setData(pageList.getNumber(), (int) pageList.getTotalElements());

        resultMap.put("total", pageList.getTotalElements());
        resultMap.put("content", reviewList);
        resultMap.put("pageHTML", pageVO.pageHTML());
        resultMap.put("page", pageList.getNumber());
        
        return resultMap;
    }

    /**
     * 내가 작성한 리뷰 리스트 가져오기
     * @param pageable 페이징 객체
     * @param userId 사용자 아이디
     * @return
     * @throws Exception
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserReviewList(Pageable pageable, String userId) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();

        Page<ReviewEntity> pageList = reviewRepository.findAllByUserId(userId, "N", pageable);

        List<ReviewDTO.DetailResponse> reviewList = pageList.getContent().stream().map(ReviewDTO.DetailResponse::of).toList();

        PageVO pageVO = new PageVO();
        pageVO.setData(pageList.getNumber(), (int) pageList.getTotalElements());

        resultMap.put("total", pageList.getTotalElements());
        resultMap.put("content", reviewList);
        resultMap.put("pageHTML", pageVO.pageHTML());
        resultMap.put("page", pageList.getNumber());

        return resultMap;
    }

    /**
     * 리뷰 등록하기
     * @param request 리뷰 객체
     * @throws Exception
     */
    @Transactional
    public void createReview(ReviewDTO.Request request) throws Exception {
        
        UserEntity userEntity = userRepository.findById(request.getUserId())
            .orElseThrow(()-> new RuntimeException("해당 사용자가 존재하지 않습니다."));
        
        OrderEntity orderEntity = orderRepository.findById(request.getOrderId())
            .orElseThrow(()-> new RuntimeException("해당 주문이 존재하지 않습니다."));

        // 이미 리뷰가 존재하는지 체크
        if (reviewRepository.existsByOrder_orderId(request.getOrderId())) {
            throw new RuntimeException("이미 해당 주문에 대한 리뷰가 존재합니다.");
        }

        StoreEntity storeEntity = orderEntity.getStore();

        ReviewEntity reviewEntity = new ReviewEntity();
        reviewEntity.setUser(userEntity);
        reviewEntity.setOrder(orderEntity);
        reviewEntity.setStore(storeEntity);
        reviewEntity.setRating(request.getRating());
        reviewEntity.setContent(request.getContent());
        reviewEntity.setDelYn("N");

        List<ReviewDTO.InnerRequest> imageList = request.getImageList();

        if (imageList != null && imageList.size() > 0) {
            for (ReviewDTO.InnerRequest innerRequest : imageList) {
                MultipartFile image = innerRequest.getImage();

                if (image != null && !image.isEmpty()) {
                    // 이미지 파일 업로드
                    Map<String, Object> imageMap = fileUtils.uploadImageFiles(image, webConfig.getReviewPath());

                    // 이미지 파일이 있을 경우에만 파일 엔티티 생성
                    if (imageMap != null) {
                        ReviewFileEntity fileEntity = new ReviewFileEntity();
                        fileEntity.setFileName(imageMap.get("fileName").toString());
                        fileEntity.setStoredName(imageMap.get("storedFileName").toString());
                        fileEntity.setFilePath(imageMap.get("filePath").toString());
                        fileEntity.setFileThumbName(imageMap.get("thumbName").toString());
                        fileEntity.setFileSize(image.getSize());
                        fileEntity.setDisplayOrder(innerRequest.getDisplayOrder());

                        reviewEntity.addFiles(fileEntity, false);  // 리뷰 엔티티와 파일 엔티티 관계를 맺어줌
                    }
                }
            }
        }

        reviewRepository.save(reviewEntity);

        // 가게 평균 평점 및 리뷰 수 업데이트
        updateStoreRatingAvg(reviewEntity);
    }

    /**
     * 리뷰 수정하기
     * @param request 리뷰 객체
     * @throws Exception
     */
    @Transactional
    public void updateReview(ReviewDTO.Request request) throws Exception {

        ReviewEntity reviewEntity = reviewRepository.findById(request.getReviewId())
            .orElseThrow(() -> new RuntimeException("해당 리뷰가 존재하지 않습니다."));

        if (!reviewEntity.getUser().getUserId().equals(request.getUserId())) {
            throw new RuntimeException("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        reviewEntity.setRating(request.getRating());
        reviewEntity.setContent(request.getContent());

        ReviewDTO.Response response = ReviewDTO.Response.of(reviewEntity);

        // 이미지 수정
        List<ReviewDTO.InnerRequest> imageList = request.getImageList();
        if (imageList != null && !imageList.isEmpty()) {

            // 기존 파일 제거
            reviewEntity.getFileList().clear();

            for (ReviewDTO.InnerRequest innerRequest : imageList) {
                MultipartFile image = innerRequest.getImage();
                if (image != null && !image.isEmpty()) {
                    Map<String, Object> imageMap = fileUtils.uploadImageFiles(image, webConfig.getReviewPath());
                    if (imageMap != null) {
                        ReviewFileEntity fileEntity = new ReviewFileEntity();
                        fileEntity.setFileName(imageMap.get("fileName").toString());
                        fileEntity.setStoredName(imageMap.get("storedFileName").toString());
                        fileEntity.setFilePath(imageMap.get("filePath").toString());
                        fileEntity.setFileThumbName(imageMap.get("thumbName").toString());
                        fileEntity.setFileSize(image.getSize());
                        fileEntity.setDisplayOrder(innerRequest.getDisplayOrder());
                        reviewEntity.addFiles(fileEntity, true);
                    }
                }
            }
        }

        reviewRepository.save(reviewEntity);

        // 가게 평균 평점 및 리뷰 수 업데이트
        updateStoreRatingAvg(reviewEntity);

        // 기존 리뷰 파일 제거
        if (imageList != null && !imageList.isEmpty()) {

            // 기존 파일 삭제 (작업 도중 DB에 문제가 생길 수도 있기 때문에 물리적 파일 삭제는 제일 마지막에 진행)
            // 리뷰 정보 DTO 가 가지고 있는 파일 정보로 삭제
            for (ReviewDTO.InnerRequest innerRequest : imageList) {
                MultipartFile image = innerRequest.getImage();
                if (image != null && !image.isEmpty()) {
                    if (response.getFileList() != null && response.getFileList().size() > 0) {
                        for (ReviewFileDTO fileDTO : response.getFileList()) {
                            deleteImageFiles(fileDTO.getFilePath(), 
                                fileDTO.getStoredName(), fileDTO.getFileThumbName());
                        }

                        break;
                    }
                }
            }   
        }
    }

    /**
     * 리뷰 삭제하기
     * @param userId 사용자 아이디
     * @param reviewId 리뷰 아이디
     * @throws Exception
     */
    @Transactional
    public void deleteReview(String userId, int reviewId) throws Exception {

        ReviewEntity reviewEntity = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("해당 리뷰가 존재하지 않습니다."));

        if (!reviewEntity.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        reviewEntity.setDelYn("Y");
        reviewRepository.save(reviewEntity);

        // 가게 평균 평점 및 리뷰 수 업데이트
        updateStoreRatingAvg(reviewEntity);
    }

    /**
     * 리뷰 답변 등록하기
     * @param request 리뷰 답변 객체
     * @throws Exception
     */
    @Transactional
    public void createReviewReply(ReviewReplyDTO.Request request) throws Exception {

        ReviewEntity reviewEntity = reviewRepository.findById(request.getReviewId())
            .orElseThrow(() -> new RuntimeException("해당 리뷰가 존재하지 않습니다."));

        // 리뷰 삭제 여부 확인
        if ("Y".equals(reviewEntity.getDelYn())) {
            throw new RuntimeException("이미 삭제된 리뷰에는 답변을 작성할 수 없습니다.");
        }

        // 점주 소유 여부 확인
        UserEntity ownerEntity = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        StoreEntity storeEntity = reviewEntity.getStore();

        if (ownerEntity.getStore() == null || ownerEntity.getStore().getStoreId() != storeEntity.getStoreId()) {
            throw new RuntimeException("해당 리뷰에 답변을 작성할 권한이 없습니다.");
        }

        // 이미 답변이 존재하는지 체크
        if (reviewEntity.getReply() != null) {
            throw new RuntimeException("이미 해당 리뷰에 대한 답변이 존재합니다.");
        }

        // 답변 엔티티 생성
        ReviewReplyEntity reviewReplyEntity = new ReviewReplyEntity();
        reviewReplyEntity.setUser(ownerEntity);
        reviewReplyEntity.setReview(reviewEntity);
        reviewReplyEntity.setContent(request.getContent());
        reviewReplyEntity.setDelYn("N");

        // 리뷰에 매핑
        reviewEntity.addReply(reviewReplyEntity);

        reviewReplyRepository.save(reviewReplyEntity);
    }

    /**
     * 리뷰 답변 수정하기
     * @param request 리뷰 답변 객체
     * @throws Exception
     */
    @Transactional
    public void updateReviewReply(ReviewReplyDTO.Request request) throws Exception {

        ReviewReplyEntity reviewReplyEntity = reviewReplyRepository.findById(request.getReviewReplyId())
            .orElseThrow(() -> new RuntimeException("해당 리뷰 답변이 존재하지 않습니다."));

        // 답변 삭제 여부 확인
        if ("Y".equals(reviewReplyEntity.getDelYn())) {
            throw new RuntimeException("삭제된 리뷰 답변은 수정할 수 없습니다.");
        }

        ReviewEntity reviewEntity = reviewReplyEntity.getReview();

        // 리뷰 삭제 여부 확인
        if ("Y".equals(reviewEntity.getDelYn())) {
            throw new RuntimeException("이미 삭제된 리뷰에는 답변을 수정할 수 없습니다.");
        }

        // 점주 소유 여부 확인
        UserEntity ownerEntity = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        StoreEntity storeEntity = reviewEntity.getStore();

        if (ownerEntity.getStore() == null || ownerEntity.getStore().getStoreId() != storeEntity.getStoreId()) {
            throw new RuntimeException("해당 리뷰 답변을 수정할 권한이 없습니다.");
        }

        reviewReplyEntity.setContent(request.getContent());

        reviewReplyRepository.save(reviewReplyEntity);
    }

    /**
     * 리뷰 답변 삭제하기
     * @param reviewReplyId 리뷰 답변 아이디
     * @param userId 사용자 아이디
     * @throws Exception
     */
    @Transactional
    public void deleteReviewReply(int reviewReplyId, String userId) throws Exception {

        // 삭제할 답글 조회
        ReviewReplyEntity reviewReplyEntity = reviewReplyRepository.findById(reviewReplyId)
            .orElseThrow(() -> new RuntimeException("해당 리뷰 답변이 존재하지 않습니다."));

        // 답변 삭제 여부 확인
        if ("Y".equals(reviewReplyEntity.getDelYn())) {
            throw new RuntimeException("이미 삭제된 리뷰 답변입니다.");
        }

        ReviewEntity reviewEntity = reviewReplyEntity.getReview();

        // 리뷰 삭제 여부 확인
        if ("Y".equals(reviewEntity.getDelYn())) {
            throw new RuntimeException("삭제된 리뷰의 답변은 삭제할 수 없습니다.");
        }

        // 점주 권한 확인
        UserEntity ownerEntity = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        StoreEntity storeEntity = reviewEntity.getStore();
        if (ownerEntity.getStore() == null || ownerEntity.getStore().getStoreId() != storeEntity.getStoreId()) {
            throw new RuntimeException("해당 리뷰 답변을 삭제할 권한이 없습니다.");
        }

        reviewReplyEntity.setDelYn("Y");

        reviewReplyRepository.save(reviewReplyEntity);
    }

    /**
     * 가게 평균 평점 및 리뷰 수 업데이트
     * @param reviewEntity 리뷰 엔티티
     */
    private void updateStoreRatingAvg(ReviewEntity reviewEntity) {
        StoreEntity storeEntity = reviewEntity.getStore();
        Double ratingAvg = reviewRepository.findRatingAvgByStoreId(storeEntity.getStoreId());
        Long reviewCount = reviewRepository.countByStore_storeIdAndDelYn(storeEntity.getStoreId(), "N");

        if (ratingAvg != null) {
            storeEntity.setRatingAvg(BigDecimal.valueOf(ratingAvg).setScale(1, RoundingMode.HALF_UP));
            storeEntity.setReviewCount(reviewCount.intValue());
            storeRepository.save(storeEntity);
        }
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
        String thumbFilePath = webConfig.getReviewPath() + "thumb" + File.separator + fileThumbName;

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


    ///////////////// 어드민 전용 메서드들 /////////////////
    
    /**
     * 모든 리뷰 리스트 가져오기
     * @param pageable 페이징 객체
     * @param searchDTO 검색 내용
     * @return
     * @throws Exception
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getReviewList(Pageable pageable, AdminReviewSearchDTO searchDTO) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();

        Page<ReviewEntity> pageList = null;

        ReviewSearchSpecification searchSpecification = new ReviewSearchSpecification(searchDTO);
        pageList = reviewRepository.findAll(searchSpecification, pageable);

        List<ReviewDTO.Response> reviewList = pageList.getContent().stream().map(ReviewDTO.Response::of).toList();

        PageVO pageVO = new PageVO();
        pageVO.setData(pageList.getNumber(), (int) pageList.getTotalElements());

        resultMap.put("total", pageList.getTotalElements());
        resultMap.put("content", reviewList);
        resultMap.put("pageHTML", pageVO.pageHTML());
        resultMap.put("page", pageList.getNumber());
        
        return resultMap;
    }

    /**
     * 리뷰 삭제하기 (어드민)
     * @param reviewId 리뷰 아이디
     * @throws Exception
     */
    @Transactional
    public void deleteReviewByAdmin(int reviewId) throws Exception {

        ReviewEntity reviewEntity = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("해당 리뷰가 존재하지 않습니다."));

        reviewEntity.setDelYn("Y");
        reviewRepository.save(reviewEntity);

        // 가게 평균 평점 및 리뷰 수 업데이트
        updateStoreRatingAvg(reviewEntity);
    }

    /**
     * 리뷰 답변 삭제하기 (어드민)
     * @param reviewReplyId 리뷰 답변 아이디
     * @throws Exception
     */
    @Transactional
    public void deleteReviewReplyByAdmin(int reviewReplyId) throws Exception {

        // 삭제할 답글 조회
        ReviewReplyEntity reviewReplyEntity = reviewReplyRepository.findById(reviewReplyId)
            .orElseThrow(() -> new RuntimeException("해당 리뷰 답변이 존재하지 않습니다."));

        // 답변 삭제 여부 확인
        if ("Y".equals(reviewReplyEntity.getDelYn())) {
            throw new RuntimeException("이미 삭제된 리뷰 답변입니다.");
        }

        ReviewEntity reviewEntity = reviewReplyEntity.getReview();

        if (reviewEntity == null) {
            throw new RuntimeException("해당 답변이 달린 리뷰가 존재하지 않습니다.");
        }

        // 리뷰 삭제 여부 확인
        if ("Y".equals(reviewEntity.getDelYn())) {
            throw new RuntimeException("삭제된 리뷰의 답변은 삭제할 수 없습니다.");
        }

        reviewReplyEntity.setDelYn("Y");

        reviewReplyRepository.save(reviewReplyEntity);
    }
}
