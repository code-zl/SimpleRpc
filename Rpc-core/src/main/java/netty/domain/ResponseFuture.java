package netty.domain;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ResponseFuture {//服务端的异步响应类
    //存放所有对于客户端的响应
    static final ConcurrentHashMap<Long,ResponseFuture> allResponseFuture=new ConcurrentHashMap<Long, ResponseFuture>();
    final Lock lock=new ReentrantLock();
    public Condition condition=lock.newCondition();//该可重入锁对应的条件队列
    public Response response;
    public ResponseFuture(ClientRequest request){
        allResponseFuture.put(request.getId(), this);//将当前请求和它的异步响应放在一起
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public static void receice(Response response){//接受服务端的response，并添加到客户端的future中。注意这是一个静态方法，在客户端只用一个respnsefuture类的receive方法
        ResponseFuture responseFuture = allResponseFuture.get(response.getId());//请求id和它对应的响应id是相同的
        if (responseFuture!=null){//因为只要调用responseFuture的构造方法就会put当前的response进去，所以只要请求id存在，其value值就不会为空
            Lock lock=responseFuture.lock;//得到对象中的重入锁
            lock.lock();
            try {
                responseFuture.setResponse(response);//将响应放到异步事件，利用锁来同步
                responseFuture.condition.signal();//唤醒因为get操作而被阻塞的线程，只有在receive一个响应之后，就会异步的返回，唤醒客户端的处理
                allResponseFuture.remove(responseFuture);//得到响应，并处理以后应该remove掉它，表示后面不会再处理
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                lock.unlock();
            }
        }
    }
    public Response get(){
        lock.lock();
        try {
            while (!done()){
                condition.await();//只要没有满足条件就阻塞等待
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            lock.unlock();//注意可重入锁一定要释放锁
        }
        return this.response;
    }

    private boolean done() {//有响应说明得到了服务端的回馈。
        if (this.response!=null)
            return true;
        return false;
    }
}
