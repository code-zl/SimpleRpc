package client.core;

import io.netty.channel.ChannelFuture;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

//进行连接的管理，实际上就是管理连接的异步响应对象ChannnelFuture
public class ChannelManager {
    //用CopyOnWriteArrayList是因为他可以在并发时同时进行读写
    public static CopyOnWriteArrayList<ChannelFuture> channelFutures=new CopyOnWriteArrayList<ChannelFuture>();
    static Set<String> realServerPaths=new HashSet<String>();//因为服务端可能会重复注册，所以这里需要用set来去重
    static AtomicInteger position=new AtomicInteger(0);//用于记录轮询到了哪一个服务器
    public static void add(ChannelFuture future){
        channelFutures.add(future);
    }
    public static void remove(ChannelFuture future){
        channelFutures.remove(future);
    }
    public static void clear(){
        channelFutures.clear();
    }
    public static ChannelFuture get(AtomicInteger i){
        int size=channelFutures.size();
        ChannelFuture channelFuture=null;
        if(i.get()>size){
            channelFuture=channelFutures.get(0);
            position=new AtomicInteger(1);//原子的设置为1
        }
        else{
            channelFuture=channelFutures.get(i.getAndIncrement());
        }
        if (!channelFuture.channel().isActive()){//判断通道是否活跃
            channelFutures.remove(channelFuture);
            return get(position);//不活跃需要重新获取服务器节点，轮询到下一个
        }
        return channelFuture;
    }
}
