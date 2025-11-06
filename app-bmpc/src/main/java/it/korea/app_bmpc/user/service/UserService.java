package it.korea.app_bmpc.user.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.korea.app_bmpc.store.entity.StoreEntity;
import it.korea.app_bmpc.user.dto.UserDepositRequestDTO;
import it.korea.app_bmpc.user.dto.UserRequestDTO;
import it.korea.app_bmpc.user.dto.UserResponseDTO;
import it.korea.app_bmpc.user.dto.UserUpdateDTO;
import it.korea.app_bmpc.user.entity.UserEntity;
import it.korea.app_bmpc.user.entity.UserRoleEntity;
import it.korea.app_bmpc.user.repository.UserRepository;
import it.korea.app_bmpc.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원 가입하기
     * @param userRequestDTO 사용자 객체
     * @throws Exception
     */
    @Transactional
    public void register(UserRequestDTO userRequestDTO) throws Exception {

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
            userEntity.setUseYn(userRequestDTO.getUseYn());
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
     * 보유금 충전하기
     * @param request 보유금 객체
     * @throws Exception
     */
    @Transactional
    public void increaseDeposit(UserDepositRequestDTO request) throws Exception {

        UserEntity userEntity = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        if ("Y".equals(userEntity.getDelYn())) {
            throw new RuntimeException("삭제된 사용자는 보유금을 충전할 수 없습니다.");
        }

        // 충전 금액이 0보다 큰지 검증
        if (request.getDeposit() <= 0) {
            throw new RuntimeException("충전 금액은 0보다 커야 합니다.");
        }

        // 기존 보유금 + 충전 금액
        int newDeposit = userEntity.getDeposit() + request.getDeposit();
        userEntity.setDeposit(newDeposit);

        userRepository.save(userEntity);
    }

    /**
     * 나의 정보 가져오기
     * @param userId 사용자 아이디
     * @return
     * @throws Exception
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUser(String userId) throws Exception {

        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        if ("Y".equals(userEntity.getDelYn())) {
            throw new RuntimeException("삭제된 사용자의 정보는 가져올 수 없습니다.");
        }

        return UserResponseDTO.of(userEntity);
    }

    /**
     * 나의 정보 수정하기
     * @param request 사용자 객체
     * @throws Exception
     */
    @Transactional
    public void updateUser(UserUpdateDTO request) throws Exception {

        UserEntity userEntity = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        // 삭제된 사용자 여부 체크
        if ("Y".equals(userEntity.getDelYn())) {
            throw new RuntimeException("삭제된 사용자의 정보는 수정할 수 없습니다.");
        }

        userEntity.setUserName(request.getUserName());
        userEntity.setBirth(request.getBirth());
        userEntity.setPhone(request.getPhone());
        userEntity.setEmail(request.getEmail());

        String roleId = userEntity.getRole().getRoleId();
    
        // 점주일때만 사업자 번호 수정
        if ("OWNER".equals(roleId)) {
            userEntity.setBusinessNo(request.getBusinessNo());
        } else {
            if (request.getBusinessNo() != null && !request.getBusinessNo().isBlank()) {
                throw new RuntimeException("사업자 번호는 점주만 수정할 수 있습니다.");
            }
        }

        userRepository.save(userEntity);
    }

    /**
     * 나의 계정 탈퇴하기
     * @param userId 사용자 아이디
     * @throws Exception
     */
    @Transactional
    public void deleteUser(String userId) throws Exception {

        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));

        // 사용자 삭제 여부 확인
        if ("Y".equals(userEntity.getDelYn())) {
            throw new RuntimeException("이미 탈퇴된 사용자입니다.");
        }
        
        // 만약 점주라면...
        if ("OWNER".equals(userEntity.getRole().getRoleId())) {
            // 점주가 등록한 가게가 아직 존재하는지 확인
            StoreEntity storeEntity = userEntity.getStore();
            if (storeEntity != null && !"Y".equals(storeEntity.getDelYn())) {
                throw new RuntimeException("등록된 가게가 아직 존재합니다. 가게를 먼저 삭제한 후 계정을 탈퇴해주세요.");
            }
        }

        userEntity.setUseYn("N");  // 사용 여부 N
        userEntity.setDelYn("Y");  // 삭제 여부 Y

        userRepository.save(userEntity);
    }
}
