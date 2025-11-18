package it.korea.app_bmpc.common.utils;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.mortennobel.imagescaling.AdvancedResizeOp;
import com.mortennobel.imagescaling.MultiStepRescaleOp;

@Component
public class FileUtils {

    private List<String> extensions = Arrays.asList("jpg", "jpeg", "gif", "png", "webp", "bmp");

    /**
     * 파일 업로드
     * @param file 파일 
     * @param type 게시판 타입
     * @return
     * @throws Exception
     */
    public Map<String, Object> uploadFiles(MultipartFile file, String filePath) throws Exception {

        Map<String, Object> resultMap = new HashMap<>();
        
        if (file == null || file.isEmpty()) {
            return null;
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return null;
        }
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        String randName = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);
        String storedFileName = randName + "." + extension;

        String newfilePath = filePath;
        String fullPath = newfilePath + storedFileName;
        
        File newFile = new File(fullPath);

        // 경로가 없다면 만들어준다
        if (!newFile.getParentFile().exists()) {

            // 모든 부모 파일 경로 만들기
            newFile.getParentFile().mkdirs();
        }

        newFile.createNewFile();  // 빈 파일 생성
        file.transferTo(newFile);  // 파일 복사

        resultMap.put("fileName", fileName);
        resultMap.put("storedFileName", storedFileName);
        resultMap.put("filePath", newfilePath);

        return resultMap;
    }

    /**
     * 파일 삭제
     * @param deleteFilePath 삭제할 파일 경로
     * @throws Exception
     */
    public void deleteFile(String deleteFilePath) throws Exception {
        File deleteFile = new File(deleteFilePath);

        if (deleteFile.exists()) {
            deleteFile.delete();
        }
    }

    /**
     * 썸네일 만들기
     * @param width 가로 픽셀
     * @param height 세로 픽셀
     * @param originFile 원본 파일
     * @param thumbPath 썸네일 경로
     * @return
     */
    public String thumbNailFile(int width, int height, File originFile, String thumbPath) throws Exception {
        String thumbFileName = "";

        String fileName = originFile.getName();
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        String randName = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);
        thumbFileName = randName + "." + extension;

        try (
            InputStream in = new FileInputStream(originFile);
            BufferedInputStream bf = new BufferedInputStream(in);
        ) {

            // 원본 이미지 파일 뜨기
            BufferedImage originImage = ImageIO.read(originFile);

            // 이미지 사이즈 줄이기
            MultiStepRescaleOp scaleImage = new MultiStepRescaleOp(width, height);

            // 마스킹 처리
            scaleImage.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Soft);

            // 리사이즈 이미지 생성
            BufferedImage resizeImage = scaleImage.filter(originImage, null);

            String thumbFilePath = thumbPath + thumbFileName;
            File resizeFile = new File(thumbFilePath);

            // 경로가 없다면 만들어준다
            if (!resizeFile.getParentFile().exists()) {

                // 모든 부모 파일 경로 만들기
                resizeFile.getParentFile().mkdirs();
            }

            // 리사이즈한 파일을 실제 경로에 생성. 결과를 리턴해준다.
            boolean isWrite = ImageIO.write(resizeImage, extension, resizeFile);

            if (!isWrite) {
                throw new RuntimeException("썸네일 생성 오류");
            }
            
        } catch (Exception e) {
            thumbFileName = null;
            e.printStackTrace();
            throw new RuntimeException("썸네일 생성 오류");
        }

        return thumbFileName;
    }

    /**
     * 파일 업로드 과정 공통화해서 분리
     * @param file request 에서 넘어온 파일
     * @return
     * @throws Exception
     */
    public Map<String, Object> uploadImageFiles(MultipartFile file, String filePath) throws Exception {
        String fileName = file.getOriginalFilename();

        if (StringUtils.isBlank(fileName)) {
            return null;
        }

        String ext = fileName.substring(fileName.lastIndexOf(".") + 1);

        if (!extensions.contains(ext.toLowerCase())) {
            throw new RuntimeException("파일 형식이 맞지 않습니다. 이미지 파일만 가능합니다.");
        }

        Map<String, Object> fileMap = this.uploadFiles(file, filePath);

        if (fileMap == null) {
            throw new RuntimeException("파일 업로드 실패");
        }

        String thumbFilePath = filePath + "thumb" + File.separator;
        String storedFilePath = filePath + fileMap.get("storedFileName").toString();

        File thumbFile = new File(storedFilePath);

        if (!thumbFile.exists()) {
            throw new RuntimeException("업로드 파일이 존재하지 않음");
        }

        String thumbName = this.thumbNailFile(225, 270, thumbFile, thumbFilePath);
        fileMap.put("thumbName", thumbName);

        return fileMap;
    }

    /**
     * 이미지 파일 복사 (메뉴 복사용)
     * @param sourceStoredName 원본 파일 저장명
     * @param filePath 파일 저장 경로
     * @return
     * @throws Exception
     */
    public Map<String, Object> copyImageFile(String sourceStoredName, String filePath) throws Exception {

        File sourceFile = new File(filePath + sourceStoredName);
        if (!sourceFile.exists()) {
            throw new RuntimeException("복사할 원본 이미지 파일이 존재하지 않습니다.");
        }

        String ext = sourceStoredName.substring(sourceStoredName.lastIndexOf(".") + 1);
        String newStoredName = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16) + "." + ext;
        
        File targetFile = new File(filePath + newStoredName);

        Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        String thumbPath = filePath + "thumb" + File.separator;
        String newThumbName = this.thumbNailFile(225, 270, targetFile, thumbPath);

        Map<String, Object> fileMap = new HashMap<>();
        fileMap.put("fileName", sourceStoredName);     // 원본 파일명
        fileMap.put("storedFileName", newStoredName);  // 새로 저장된 실제 파일명
        fileMap.put("filePath", filePath);
        fileMap.put("thumbName", newThumbName);

        return fileMap;
    }
}
