package medium;

import netty.annotation.Remote;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class InitMedium implements BeanPostProcessor {
    //给bean添加后置处理器，只要将这个对象注入到容器中，那么所有的bean在初始化前后都可以有额外的操作。
    //可以利用bean的方法来对那些bean进行处理进行设置
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(Remote.class)) {
            Method[] methods=bean.getClass().getDeclaredMethods();//得到该类所有声明的方法
            for (Method m:methods){
               String key= bean.getClass().getInterfaces()[0].getName()+"."+ m.getName();//因为采用的是接口和实现类，所以只需要得到实现的第一个接口的接口名即可，根据多态调用的是子类的方法
               BeanMethod beanMethod=new BeanMethod(bean,m);
               Media.beanmap.put(key,beanMethod);
            }
        }
        return bean;
    }
}
