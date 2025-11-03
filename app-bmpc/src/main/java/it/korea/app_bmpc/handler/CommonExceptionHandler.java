package it.korea.app_bmpc.handler;

import java.util.List;

import javax.naming.AuthenticationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import it.korea.app_bmpc.common.dto.ApiErrorResponse;
import it.korea.app_bmpc.common.dto.ErrorCodeEnum;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler {

    /**
     * 메서드 파라미터 항목이 틀렸을 경우
     * RequestBody 일 때 @Valid 를 통해서 에러 발생
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        log.error("===== MethodArgumentNotValidException : {} =====", e.getMessage());

        List<ApiErrorResponse.ApiFieldError> fieldErrors =
                e.getBindingResult().getFieldErrors().stream().map(ApiErrorResponse.ApiFieldError::error).toList();

        ApiErrorResponse apiErrorResponse = getApiErrorResponse(ErrorCodeEnum.INVALID_PARAMETER, fieldErrors);
        return ResponseEntity.status(ErrorCodeEnum.INVALID_PARAMETER.getStatus()).body(apiErrorResponse);
    }

    /**
     * 메서드 파라미터 항목이 틀렸을 경우
     * RequestParam 항목이 틀렸을 경우
     * PathVariable 항목이 틀렸을 경우
     * @param e
     * @return
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ApiErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {

        log.error("===== ConstraintViolationException : {} =====", e.getMessage());

        List<ApiErrorResponse.ApiFieldError> fieldErrors =
                e.getConstraintViolations().stream().map(ApiErrorResponse.ApiFieldError::error).toList();

        ApiErrorResponse apiErrorResponse = getApiErrorResponse(ErrorCodeEnum.INVALID_PARAMETER, fieldErrors);
        return ResponseEntity.status(ErrorCodeEnum.INVALID_PARAMETER.getStatus()).body(apiErrorResponse);
    }

    /**
     * GET QUERY STRING 방식에서 파라미터를 부여하지 않은 경우
     * @param e
     * @return
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ApiErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {

        log.error("===== MissingServletRequestParameterException : {} =====", e.getMessage());
        ApiErrorResponse.ApiFieldError fieldError = ApiErrorResponse.ApiFieldError.error(e.getParameterName(), "", e.getMessage());

        ApiErrorResponse apiErrorResponse = getApiErrorResponse(ErrorCodeEnum.INVALID_PARAMETER, List.of(fieldError));
        return ResponseEntity.status(ErrorCodeEnum.INVALID_PARAMETER.getStatus()).body(apiErrorResponse);
    }

     /**
      * RequestParam 타입 불일치
      * @param e
      * @return
      */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("===== MethodArgumentTypeMismatchException : {} =====", e.getMessage());
        ApiErrorResponse.ApiFieldError fieldError =
                ApiErrorResponse.ApiFieldError.error(e.getName(), "", e.getMessage());

        ApiErrorResponse apiErrorResponse = getApiErrorResponse(ErrorCodeEnum.INVALID_PARAMETER, List.of(fieldError));
        return ResponseEntity.status(ErrorCodeEnum.INVALID_PARAMETER.getStatus()).body(apiErrorResponse);
    }

    /**
     * 지원하지 않는 HTTP Method
     * @param e
     * @return
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ApiErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("===== HttpRequestMethodNotSupportedException : {} =====", e.getMessage());

        ApiErrorResponse apiErrorResponse = getApiErrorResponse(ErrorCodeEnum.METHOD_NOT_ALLOWED);
        return ResponseEntity.status(ErrorCodeEnum.METHOD_NOT_ALLOWED.getStatus()).body(apiErrorResponse);
    }

    /**
     * DB 제약조건 위반
     * @param e
     * @return
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<ApiErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("===== DataIntegrityViolationException : {} =====", e.getMessage());

        String message = "DB 제약조건을 위반했습니다.";

        Throwable cause = e.getRootCause();
        if (cause != null && cause.getMessage() != null) {
            String msg = cause.getMessage();

            if (msg.contains("uq_users_phone")) {
                message = "이미 존재하는 전화번호입니다.";
            } else if (msg.contains("uq_users_email")) {
                message = "이미 존재하는 이메일입니다.";
            }
        }

        ApiErrorResponse apiErrorResponse = getApiErrorResponse("E409", message);
        return ResponseEntity.status(ErrorCodeEnum.DATABASE_ERROR.getStatus()).body(apiErrorResponse);
    }

    /**
     * 런타임 예외
     * @param e
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<ApiErrorResponse> handleRuntimeException(RuntimeException e) {
        log.error("===== RuntimeException : {} =====", e.getMessage());

        String message = "Access Denied".equals(e.getMessage()) ? "권한이 없습니다." : e.getMessage();
        ApiErrorResponse apiErrorResponse = getApiErrorResponse("E500", message);
        return ResponseEntity.status(ErrorCodeEnum.INTERNAL_SERVER_ERROR.getStatus()).body(apiErrorResponse);
    }

    /**
     * 기타 예외
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiErrorResponse> handleUnknownException(Exception e) {
        log.error("===== Exception : {} =====", e.getMessage());

        // JSON 대신 multipart/form-data 로 보낸 경우
        if (e.getMessage() != null && e.getMessage().contains("multipart/form-data")) {
            ApiErrorResponse apiErrorResponse = getApiErrorResponse("E500", "JSON 형식으로 보내야 합니다.");
            return ResponseEntity.status(ErrorCodeEnum.INTERNAL_SERVER_ERROR.getStatus()).body(apiErrorResponse);
        }

        if (isSecurityException(e)) {
            throw (RuntimeException) e;  // security 예외는 다시 던짐
        }

        String message = e.getMessage() != null ? e.getMessage() : "서버에서 에러가 발생했습니다.";

        ApiErrorResponse apiErrorResponse = getApiErrorResponse("E500", message);
        return ResponseEntity.status(ErrorCodeEnum.INTERNAL_SERVER_ERROR.getStatus()).body(apiErrorResponse);
    }

    private ApiErrorResponse getApiErrorResponse(String errorCode, String message) {
        return ApiErrorResponse.error(errorCode, message);
    }

    private ApiErrorResponse getApiErrorResponse(ErrorCodeEnum errorCodeEnum) {
        return ApiErrorResponse.error(errorCodeEnum.getErrorCode(), errorCodeEnum.getMessage());
    }

    private ApiErrorResponse getApiErrorResponse(ErrorCodeEnum errorCodeEnum,
                                                 List<ApiErrorResponse.ApiFieldError> fieldErrors) {
        return ApiErrorResponse.error(errorCodeEnum.getErrorCode(), errorCodeEnum.getMessage(), fieldErrors);
    }

    private boolean isSecurityException(Exception e) {
        return e instanceof AuthenticationException ||
            e instanceof AccessDeniedException ||
            e instanceof AuthenticationCredentialsNotFoundException;
    }
}
