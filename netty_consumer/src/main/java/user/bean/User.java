package user.bean;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class User {
    Long id;
    String name;
}
