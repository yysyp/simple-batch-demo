package ps.demo.simplebatchdemo.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ps.demo.simplebatchdemo.common.BaseResponse;
import ps.demo.simplebatchdemo.common.ClientErrorException;
import ps.demo.simplebatchdemo.common.CodeEnum;
import ps.demo.simplebatchdemo.common.ServerErrorException;


import java.util.HashMap;
import java.util.Map;


@Slf4j
@ControllerAdvice
public class GlobalControllerExceptionHandler {

    public ResponseEntity<BaseResponse> constructResponseEntity(CodeEnum codeEnum) {
        BaseResponse errorResponse = BaseResponse.of(codeEnum);
        HttpStatus httpStatus = HttpStatus.valueOf(codeEnum.getHttpCode());
        return new ResponseEntity<BaseResponse>(errorResponse, httpStatus);
    }

    public ResponseEntity<BaseResponse> constructResponseEntity(Exception e) {
        if (e instanceof ClientErrorException) {
            return constructResponseEntity(((ClientErrorException)e).getCodeEnum());
        } else if (e instanceof ServerErrorException) {
            return constructResponseEntity(((ServerErrorException)e).getCodeEnum());
        }
        return constructResponseEntity(CodeEnum.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = ServletRequestBindingException.class)
    public ResponseEntity<BaseResponse> handleException(ServletRequestBindingException e) {
        log.error("ServletRequestBindingException handling, message={}", e.getMessage(), e);
        return constructResponseEntity(CodeEnum.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error("MethodArgumentNotValidException handling, message={}", ex.getMessage(), ex);
        BaseResponse errorResponse = BaseResponse.of(CodeEnum.BAD_REQUEST);
        HttpStatus httpStatus = HttpStatus.valueOf(CodeEnum.BAD_REQUEST.getHttpCode());
        errorResponse.setMessage(errors.toString());
        return new ResponseEntity<BaseResponse>(errorResponse, httpStatus);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse> handleThrowable(Exception e) {
        log.error("Exception handleThrowable, message={}", e.getMessage(), e);
        return constructResponseEntity(e);
    }

}