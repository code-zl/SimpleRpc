package client.core;

import client.zookeeper.ZookeeperFactory;
import io.netty.channel.ChannelFuture;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.HashSet;
import java.util.List;

public class ServerWatcher implements CuratorWatcher {

    public void process(WatchedEvent watchedEvent) throws Exception {
        CuratorFramework client= ZookeeperFactory.create();
        String path = watchedEvent.getPath();//得到监听事件所在的路径，这里其实就是定义的常量SERVER_PATH
        client.getChildren().usingWatcher(this).forPath(path);//循环监听
        List<String> serverPaths=client.getChildren().forPath(path);//得到服务端变化后的所有子路径
        ChannelManager.realServerPaths.clear();//进入到wather中说明服务端出现了问题，就需要将旧的节点全部清空，重新加入新的节点。
        for (String serverPath:serverPaths){
            String[] str=serverPath.split("#");
            ChannelManager.realServerPaths.add(str[0]+"#"+str[1]);//用#分割服务端ip地址和端口号
        }
        ChannelManager.clear();//因为客户端连接的服务器要换，所以将前面的内容情况，放入新服务器的连接响应
        for (String realServer:ChannelManager.realServerPaths){
            String[] str=realServer.split("#");
            ChannelFuture channelFuture=TCPClient.b.connect(str[0],Integer.valueOf(str[1]));//当发生watch事件，客户端就从新的zookeeper子节点选择一个进行连接，实现断线重连功能
            ChannelManager.channelFutures.add(channelFuture);//将创建连接的反馈事件放到channelManager中进行管理
        }
    }
}
