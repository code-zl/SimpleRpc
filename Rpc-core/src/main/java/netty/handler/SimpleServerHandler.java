package netty.handler;

import com.alibaba.fastjson.JSONObject;
import netty.domain.Response;
import netty.handler.param.ServerRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class SimpleServerHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){//判断用户channel的事件，如果是用户空闲的话就判读是读空闲还是写空闲，来进行分别的处理
            IdleStateEvent event= (IdleStateEvent) evt;
            if (event.state().equals(IdleState.READER_IDLE)){
                System.out.println("读空闲===");
                ctx.channel().close();//客户端不再读取，就关闭
            }else if (event.state().equals(IdleState.WRITER_IDLE))
                System.out.println("写空闲===");
            else if (event.state().equals(IdleState.ALL_IDLE)){//都空闲时，需要给客户端发一个心跳包
                System.out.println("读写空闲===");
                ctx.channel().writeAndFlush("ping\r\n");
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            //利用的是stringDecoder，所以会将接收的信号转换为string
        ServerRequest request= JSONObject.parseObject(msg.toString(), ServerRequest.class);
        Response resp=new Response();
        resp.setId(request.getId());
        resp.setResponse("is ok ");
        ctx.channel().writeAndFlush(JSONObject.toJSONString(resp)+"\r\n");//将josn数据转换为string发送出去

    }
}
