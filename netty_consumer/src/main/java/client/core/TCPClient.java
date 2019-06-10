package client.core;

import client.constant.Constants;
import client.handler.SimpleClientHandler;
import client.param.ClientRequest;
import client.param.Response;
import client.zookeeper.ZookeeperFactory;
import com.alibaba.fastjson.JSONObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.Watcher;


import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/*
TCP长连接的案例，异步获取响应结果。
 */
public class TCPClient {
    static final Bootstrap b= new Bootstrap();
    static  ChannelFuture f=null;//异步获取连接结果

    static {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        b.group( workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() { // (4)
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Delimiters.lineDelimiter()[0]));
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new StringEncoder());
                        ch.pipeline().addLast(new SimpleClientHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128);        // (5)
        String host="localhost";
        int port=8080;
        //首先要从注册中心中去获取服务器地址
        CuratorFramework client= ZookeeperFactory.create();//创建zookeeper客户端，这里只是设置了一台zookeeper服务器，端口号为2181.
        try {
            List<String> serverPaths = client.getChildren().forPath(Constants.SERVER_Path);//得到该路径下的所有子节点
            CuratorWatcher watcher=new ServerWatcher();
            client.getChildren().usingWatcher(watcher).forPath(Constants.SERVER_Path);//加入监听
            for (String serverPath:serverPaths){//这里对所有的服务器节点进行了遍历，客户端都尝试连接，并将连接的异步结果都放在容器中
                String[] str=serverPath.split("#");
                ChannelManager.realServerPaths.add(str[0]+"#"+str[1]);//用#分割服务端ip地址和端口号
                ChannelFuture channelFuture=b.connect(str[0],Integer.valueOf(str[1]));//当发生watch事件，客户端就从新的zookeeper子节点选择一个进行连接，实现断线重连功能
                ChannelManager.channelFutures.add(channelFuture);//将创建连接的反馈事件放到channelManager中进行管理
            }
  /*          if (realServerPaths.size()>0){
                Iterator<String> iterator =realServerPaths.iterator();
                String[] hostAndPort=iterator.next().split("#");//这里只取了所有子节点下的第一个服务器，可以加入其它的负载均衡算法
                host=hostAndPort[0];
                port=Integer.valueOf(hostAndPort[1]);
            }*/

        } catch (Exception e) {
            e.printStackTrace();
        }
/*        try {
            f = b.connect(host,port).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

    public static Response send(ClientRequest request){//客户端发送消息
        f=ChannelManager.get(ChannelManager.position);//轮询的方式从list中去拿channelfuture，来不断和list中的服务器尝试通信
        f.channel().writeAndFlush(JSONObject.toJSONString(request)+"\r\n");//直接发送Object对象
        ResponseFuture responseFuture = new ResponseFuture(request);
        return responseFuture.get(2*60);//得到服务端的异步响应，需要传入自动超时的时间，这里设置的是2s
    }
}
