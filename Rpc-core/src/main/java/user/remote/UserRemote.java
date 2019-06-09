package user.remote;

import netty.domain.Response;
import user.bean.User;

import java.util.List;

public interface UserRemote {
    public Response saveUser(User user);
    public Response saveUsers(List<User> users);
}
