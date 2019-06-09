package netty.util;

import netty.domain.Response;

public class ResponseUtil {
    public static Response createSuccessResponse(){
        return new Response();
    }
    public static Response createSuccessResponse(Object content){
        Response response=new Response();
        response.setContent(content);
        return response;
    }
    public static Response createFailedResponse(String code,String message){
        Response response=new Response();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
}
