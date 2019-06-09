package medium;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.lang.reflect.Method;

@Component
public class InitMedium implements BeanPostProcessor {
    //geibean添加后置处理器，只要将这个对象注入到容器中，那么所有的bean在初始化前后都可以有额外的操作。
    //可以利用bean的方法来对那些bean进行处理进行设置
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(Controller.class)) {
            Method[] methods=bean.getClass().getDeclaredMethods();//得到该类所有声明的方法
            for (Method m:methods){
               String key= bean.getClass().getName()+"."+ m.getName();
               BeanMethod beanMethod=new BeanMethod(bean,m);
               Media.beanmap.put(key,beanMethod);
            }
        }
        return bean;
    }
}
