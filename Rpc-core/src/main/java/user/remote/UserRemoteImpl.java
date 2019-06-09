package user.remote;

import netty.annotation.Remote;
import netty.domain.Response;
import netty.util.ResponseUtil;
import user.bean.User;
import user.service.UserService;

import javax.annotation.Resource;
import java.util.List;
@Remote
public class UserRemoteImpl implements UserRemote{
    @Resource
    UserService userService;
    public Response saveUser(User user){
        userService.save(user);
        return ResponseUtil.createSuccessResponse(user);
    }
    public Response saveUsers(List<User> users){
        userService.saveList(users);
        return ResponseUtil.createSuccessResponse(users);
    }
}
