package medium;

import com.alibaba.fastjson.JSONObject;
import netty.domain.Response;
import netty.handler.param.ServerRequest;

import java.awt.geom.RectangularShape;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Media {
    public static Map<String,BeanMethod> beanmap;
    private static Media media=null;
    static {
        beanmap=new ConcurrentHashMap<String, BeanMethod>();
        media=new Media();
    }
    //因为这是服务端整体的处理类，所以设置为单例模式，得到单例对象
    public static Media newInstance(){
        return media;
    }

    public Response process(ServerRequest request) {
        Response res=null;
        String command= request.getCommand();
        BeanMethod beanMethod=beanmap.get(command);
        if (beanMethod==null)
            return null;
        Object bean=beanMethod.getBean();
        Method m=beanMethod.getMethod();
        Object content=request.getContent();//得到接收的内容
        Class paramTypes=m.getParameterTypes()[0];//得到该方法所有的参数类型(注意，这里只取了第一个参数的类型)
        Object args= JSONObject.parseObject(JSONObject.toJSONString(content),paramTypes);
        try {
            res= (Response) m.invoke(bean,args);//传入该方法的执行对象和参数,返回结果，在方法调用的时候就转换为了response，实际上应该自己包装为response，将结果包装进去
            res.setId(request.getId());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return res;
    }
}
