package medium;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeanMethod {
    private Object bean;
    private Method method;
}
