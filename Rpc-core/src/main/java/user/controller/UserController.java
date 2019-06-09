package user.controller;

import netty.domain.Response;
import netty.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import user.bean.User;
import user.service.UserService;

import java.util.List;

@Controller
public class UserController {
    @Autowired
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
