package it.korea.app_bmpc.admin.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.korea.app_bmpc.admin.dto.AdminUserDTO;
import it.korea.app_bmpc.admin.dto.AdminUserRequestDTO;
import it.korea.app_bmpc.admin.dto.AdminUserSearchDTO;
import it.korea.app_bmpc.admin.dto.AdminUserUpdateRequestDTO;
import it.korea.app_bmpc.common.dto.PageInfo;
import it.korea.app_bmpc.store.service.StoreService;
import it.korea.app_bmpc.user.entity.UserEntity;
import it.korea.app_bmpc.user.entity.UserRoleEntity;
import it.korea.app_bmpc.user.repository.UserRepository;
import it.korea.app_bmpc.user.repository.UserRoleRepository;
import it.korea.app_bmpc.user.repository.UserSearchSpecification;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final StoreService storeService;
    private final PasswordEncoder passwordEncoder;   // security config 에서 bean 으로 등록했기 때문에 bcrypt를 똑같이 사용할 수 있음

    /**
     * 회원 리스트 가져오기
     * @param pageable 페이징 객체
     * @return
     * @throws Exception
     */
    @Transactional   // LAZY 모드로 가져오려면 Transactional 이어야 한다
    public Map<String, Object> getUserList(Pageable pageable) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();

        Page<UserEntity> pageList = userRepository.findAll(pageable);

        List<AdminUserDTO> userList = pageList.getContent().stream().map(AdminUserDTO::of).toList();

        resultMap.put("content", userList);
        resultMap.put("pageInfo", PageInfo.of(pageList));
        
        return resultMap;
    }

    /**
     * 회원 리스트 가져오기 (with 검색)
     * @param pageable 페이징 객체
     * @param searchDTO 검색 내용
     * @return
     * @throws Exception
     */
    @Transactional  // LAZY 모드로 가져오려면 Transactional 이어야 한다
    public Map<String, Object> getUserList(Pageable pageable, AdminUserSearchDTO searchDTO) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();

        Page<UserEntity> pageList = null;
        UserSearchSpecification searchSpecification = new UserSearchSpecification(searchDTO);
        pageList = userRepository.findAll(searchSpecification, pageable);

        List<AdminUserDTO> userList = pageList.getContent().stream().map(AdminUserDTO::of).toList();

        resultMap.put("content", userList);
        resultMap.put("pageInfo", PageInfo.of(pageList));

        return resultMap;
    }

    /**
     * 회원 상세정보 가져오기
     * @param userId 회원 아이디
     * @return
     * @throws Exception
     */
    @Transactional(readOnly = true)   // LAZY 모드로 가져오려면 Transactional 이어야 한다
    public AdminUserDTO getUser(String userId) throws Exception {
        
        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(()-> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        return AdminUserDTO.of(userEntity);
    }

    /**
     * 회원 등록하기
     * @param userRequestDTO 회원 등록 내용 DTO
     * @throws Exception
     */
    @Transactional
    public void createUser(AdminUserRequestDTO userRequestDTO) throws Exception {

        UserRoleEntity userRoleEntity = userRoleRepository.findById(userRequestDTO.getUserRole())  // 해당하는 권한이 존재하는지 체크
            .orElseThrow(()-> new RuntimeException("해당 권한이 존재하지 않습니다."));

        if (!userRepository.findById(userRequestDTO.getUserId()).isPresent()) {   // 등록하려는 아이디와 동일한 회원이 없을 경우에만 회원 등록
            UserEntity userEntity = new UserEntity();
            userEntity.setUserId(userRequestDTO.getUserId());
            userEntity.setUserName(userRequestDTO.getUserName());
            userEntity.setPasswd(passwordEncoder.encode(userRequestDTO.getPasswd()));
            userEntity.setBirth(userRequestDTO.getBirth());
            userEntity.setGender(userRequestDTO.getGender());
            userEntity.setPhone(userRequestDTO.getPhone());
            userEntity.setEmail(userRequestDTO.getEmail());
            userEntity.setUseYn("Y");
            userEntity.setDelYn(userRequestDTO.getDelYn());
            userEntity.setDeposit(0);
            userEntity.setBalance(0);
            userEntity.setRole(userRoleEntity);
            
            // 점주일 때만 사업자 번호 등록 가능
            String roleId = userRoleEntity.getRoleId();
            if ("OWNER".equals(roleId)) {
                if (StringUtils.isNotBlank(userRequestDTO.getBusinessNo())) {
                    userEntity.setBusinessNo(userRequestDTO.getBusinessNo());
                } else {
                    throw new RuntimeException("점주 회원은 사업자 번호를 반드시 입력해야 합니다.");
                }
            }
            
            userRepository.save(userEntity);
        } else {
            throw new RuntimeException("해당 아이디를 가진 회원이 이미 존재합니다.");
        }
    }

    /**
     * 회원 정보 수정하기
     * @param userRequestDTO 회원 정보 수정 내용 DTO
     * @throws Exception
     */
    @Transactional
    public void updateUser(AdminUserUpdateRequestDTO userRequestDTO) throws Exception {
        
        UserEntity userEntity = userRepository.findById(userRequestDTO.getUserId())
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        userEntity.setUserName(userRequestDTO.getUserName());
        userEntity.setPhone(userRequestDTO.getPhone());
        userEntity.setEmail(userRequestDTO.getEmail());
        userEntity.setGender(userRequestDTO.getGender());

        if (StringUtils.isNotBlank(userRequestDTO.getPasswd())) {
            userEntity.setPasswd(passwordEncoder.encode(userRequestDTO.getPasswd()));
        }

        userEntity.setUseYn("Y");

        // 점주일 때만 사업자번호 설정
        if ("OWNER".equals(userEntity.getRole().getRoleId())) {
            userEntity.setBusinessNo(userRequestDTO.getBusinessNo());
        } else {
            userEntity.setBusinessNo(null);
        }

        userRepository.save(userEntity);
    }

    /**
     * 회원 삭제하기
     * @param userId 회원 아이디
     * @throws Exception
     */
    @Transactional
    public void deleteUser(String userId) throws Exception {

        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(()-> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        userEntity.setUseYn("N");  // 사용 여부 N로 변경
        userEntity.setDelYn("Y");  // 삭제 여부 Y로 변경

        // 점주 권한을 가지고 있고 연결된 가게가 있다면 해당 가게도 삭제 처리
        if ("OWNER".equals(userEntity.getRole().getRoleId()) && userEntity.getStore() != null) {
            storeService.deleteStoreByAdmin(userEntity.getStore().getStoreId());
        }

        userRepository.save(userEntity);
    }
}
