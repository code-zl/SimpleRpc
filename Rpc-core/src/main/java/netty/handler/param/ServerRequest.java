package netty.handler.param;

import lombok.Data;

//其实作用和response差不多
@Data
public class ServerRequest {
    private long id;
    Object content;//方法的参数
    private String command;//某一个类的某一个方法全称，如A.B方法
}
