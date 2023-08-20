package ps.demo.simplebatchdemo.common;

import lombok.*;

import java.io.Serializable;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse implements Serializable {

    protected String code = CodeEnum.SUCCESS.getCode();
    protected String message = CodeEnum.SUCCESS.getDetailedMessage();
    protected String trace;
    protected String correlationId;
    protected String instance;
    protected String timestamp = MyUtil.getNowDateStr();
    protected String path;

    public static BaseResponse success() {
        return new BaseResponse();
    }

    public static BaseResponse of(CodeEnum codeEnum) {
        return BaseResponse.builder().code(codeEnum.getCode())
                .message(codeEnum.getDetailedMessage()).build();
    }

}
