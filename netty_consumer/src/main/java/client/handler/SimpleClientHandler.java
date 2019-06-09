package client.handler;

import client.core.ResponseFuture;
import client.param.Response;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;


public class SimpleClientHandler extends SimpleChannelInboundHandler {
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        channelHandlerContext.channel().attr(AttributeKey.valueOf("response")).set(o);
        if ("ping".equals(o.toString())){
            channelHandlerContext.channel().writeAndFlush("ping\r\n");
            return;
        }
        Response response= JSONObject.parseObject(o.toString(),Response.class);//利用json将接收到的数据转换为需要的pojo对象
        ResponseFuture.receice(response);//客户端在收到服务器响应之后就把它将入到客户端总体的future中

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().writeAndFlush("hello server\r\n");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

    }
}
