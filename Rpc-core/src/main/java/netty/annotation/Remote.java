package netty.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.METHOD})//注解可以放在类上面，也可以放在方法上面
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Remote {
    String value() default "";
}
