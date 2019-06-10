package user.remote;

import netty.annotation.Remote;
import netty.util.ResponseUtil;
import user.model.User;
import user.service.UserService;

import javax.annotation.Resource;
import java.util.List;

@Remote
public class UserRemoteImpl implements UserRemote{
    @Resource
    UserService userService;
    public Object saveUser(User user){
        userService.save(user);
        return ResponseUtil.createSuccessResponse(user);
    }
    public Object saveUsers(List<User> users){
        userService.saveList(users);
        return ResponseUtil.createSuccessResponse(users);
    }
}
