package netty.init;

import netty.constant.Constants;
import netty.factory.ZookeeperFactory;
import netty.handler.ServerHandler;
import netty.handler.SimpleServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
@Component
public class NettyInitial implements ApplicationListener<ContextRefreshedEvent> {
    //实现applicationListener接口，表示在applicationcontext加载之后，就会调用其onApplicationEvent方法
    public  void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Delimiters.lineDelimiter()[0]));
                            ch.pipeline().addLast(new IdleStateHandler(60,45,20,TimeUnit.SECONDS));
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new StringEncoder());
                            ch.pipeline().addLast(new ServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // 自己实现心跳
            int port=8080;
            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)
            //将服务注册到zookeeper上这里只注册了一个
           CuratorFramework client= ZookeeperFactory.create();
           InetAddress address=InetAddress.getLocalHost();
           client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(Constants.SERVER_Path+"/"+address.getHostAddress()+"#"+port+"#");
           //设置为EPHEMERAL_SEQUENTIAL类型是因为如果不给临时节点加序号的话，就算服务器关闭了，zookeeper节点也可能会有一段时间不会消失。
            //给ip地址加#号是因为这里用的是递增序号类型的临时节点，会在后面加上由父节点维护的编号，这里加上#就可以方便将编号和前面的ip地址区分，方便取出
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.start();
    }
}
