package client.core;

import client.handler.SimpleClientHandler;
import client.param.ClientRequest;
import client.param.Response;
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


/*
TCP长连接的案例，异步获取响应结果。
 */
public class TCPClient {
    static final Bootstrap b= new Bootstrap();
    static  ChannelFuture f=null;//异步获取结果
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
        try {
            f = b.connect(host,port).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static Response send(ClientRequest request){//客户端发送消息
        f.channel().writeAndFlush(JSONObject.toJSONString(request)+"\r\n");//直接发送Object对象
        ResponseFuture responseFuture = new ResponseFuture(request);
        return responseFuture.get();//得到服务端的异步响应
    }
}
