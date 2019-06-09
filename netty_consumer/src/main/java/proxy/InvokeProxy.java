package proxy;

import client.annotation.RemoteInvoke;
import client.core.TCPClient;
import client.param.ClientRequest;
import client.param.Response;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InvokeProxy implements BeanPostProcessor {
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields=bean.getClass().getFields();//反射得到待初始化的bean的所有域对象
        for (final Field field:fields){
            if (field.isAnnotationPresent(RemoteInvoke.class)){
                field.setAccessible(true);//设置为可以改变的，这里将其改变为其动态代理对象
                final Map<Method,Class> methodClassMap=new ConcurrentHashMap<Method, Class>();
                putMethodClassMap(methodClassMap,field);//定义方法来实现访问一个需要的域变量的时候将它的所有方法都放在map中
                Enhancer enhancer=new Enhancer();//cglib方式来实现动态代理
                enhancer.setInterfaces(new Class[]{field.getType()});//得到域变量的类型，其实就是它是什么类
                enhancer.setCallback(new MethodInterceptor() {//设置回调函数，代理对象的方法执行该回调函数就会执行
                    public Object intercept(Object instance, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
                        //采用netty客户端去调用服务器
                        ClientRequest request=new ClientRequest();
                        request.setCommand(methodClassMap.get(method).getName()+"."+method.getName());//command就是需要调用方法的全路径名，为了不写死并且简单起见，这里需要根据调用的方法名称得到它对应的类名，这个是用map记录的
                        request.setContent(args[0]);//这里简单的假设方法只有一个参数
                        Response resp= TCPClient.send(request);//完全屏蔽底层通信，实际上是netty通信返回调用结果，这里直接得到结果
                        return resp;//该返回的结果直接就只外面它的代理对象方法的返回结果
                    }
                });
                try {
                    field.set(bean,enhancer.create());//将所有的属性bean变为其动态代理类
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }

    /**
     * 将属性的所有方法和属性接口类型放入到一个map中
     * @param methodClassMap
     * @param field
     */
    private void putMethodClassMap(Map<Method, Class> methodClassMap, Field field) {
        Method[] methods=field.getType().getMethods();
        for(Method method:methods)
            methodClassMap.put(method,field.getType());
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return null;
    }
}
