package netty.factory;

import com.sun.security.ntlm.Client;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.awt.datatransfer.Clipboard;

public class ZookeeperFactory {
    public static CuratorFramework create(){
        //RetryPolicy接口来自定义重试策略
        RetryPolicy retryPolicy=new ExponentialBackoffRetry(1000,3);//表示重试的次数测试
        CuratorFramework clent = CuratorFrameworkFactory.newClient("localhost:2181", retryPolicy);
        clent.start();//调用start开启一个客户端的会话
        return clent;//得到
    }

/*    public static void main(String[] args) throws Exception {
        CuratorFramework client=create();
        client.create().forPath("/netty");//客户端在连接的zookeeper服务器上创建节点
    }*/
}
