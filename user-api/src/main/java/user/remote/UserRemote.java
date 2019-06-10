package user.remote;




import user.model.User;

import java.util.List;

public interface UserRemote {
    public Object saveUser(User user);
    public Object saveUsers(List<User> users);
}
