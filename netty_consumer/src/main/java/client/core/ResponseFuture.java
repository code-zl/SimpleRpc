package client.core;

import client.constant.code;
import client.param.ClientRequest;
import client.param.Response;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
@Slf4j
public class ResponseFuture {//服务端的异步响应类
    //存放所有对于客户端的响应
    static final ConcurrentHashMap<Long,ResponseFuture> allResponseFuture=new ConcurrentHashMap<Long, ResponseFuture>();
    final Lock lock=new ReentrantLock();
    public Condition condition=lock.newCondition();//该可重入锁对应的条件队列
    public Response response;
    private long timeout=2*60*1000;//默认2分钟
    private long startTime=System.currentTimeMillis();

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getTimeout() {
        return timeout;
    }

    public long getStartTime() {
        return startTime;
    }

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
    public Response get(long time){
        lock.lock();
        try {
            while (!done()){
                condition.await(time, TimeUnit.SECONDS);//只要没有满足条件就阻塞等待,设置超时时间
                if ((System.currentTimeMillis()-startTime)>time) {
                    log.info("客户端连接超时");
                    break;
                }
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
    static class FutureThread extends  Thread{//因为有的响应可能很久没有过来，所以需要设置超时，超时过后需要将responseFuture删除掉，这里专门创建一个线程用于删除
        @Override
        public void run() {
            Set<Long> ids=allResponseFuture.keySet();//得到所有请求id和响应
            for(long id:ids){
                ResponseFuture sf=allResponseFuture.get(id);
                if (sf==null)
                    allResponseFuture.remove(sf);//为空就进行删除
                else {
                    if (sf.getTimeout()<(System.currentTimeMillis()-sf.getStartTime())){
                        Response resp=new Response();
                        resp.setId(id);
                        resp.setCode(code.LINK_TIMEOUT);//超时的代码设置为33333
                        resp.setMessage("链路请求超时");
                        sf.receice(resp);//将超时响应发到map中，作为对于请求的响应
                    }
                }
            }
        }
    }
    static{
        FutureThread futureThread=new FutureThread();
        futureThread.setDaemon(true);//设置为守护线程，来不断的扫描map中的内容，判断是不是响应为空，是不是超时了？
        futureThread.start();
    }
}
