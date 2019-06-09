package netty.domain;

import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;
@Data
//添加data注解，使其自动生成set和get方法
public class ClientRequest {
    private final long id;
    private Object content;
    private String command;
    private final AtomicLong aid=new AtomicLong();
    public ClientRequest(){//每生产一个客户端请求就会自动的增加id值，因为是并发请求，所以需要用cas的方式
        id=  aid.incrementAndGet();
    }

}
