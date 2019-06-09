package netty.annotation;

import java.lang.annotation.*;

//客户端的调用注解，基于域变量的
@Target({ElementType.FIELD})//注解可以放在类上面，也可以放在方法上面
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RemoteInvoke {
}
