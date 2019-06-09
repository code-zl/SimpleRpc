package client.param;

import lombok.Data;

@Data
public class Response {
    private long id;
    private Object content;
    private String code="00000";//反馈信息的状态，00000为成功，其他为失败
    private String message;//失败的原因
}
