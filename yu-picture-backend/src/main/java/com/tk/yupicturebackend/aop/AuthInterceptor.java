package com.tk.yupicturebackend.aop;

import com.tk.yupicturebackend.annotation.AuthCheck;
import com.tk.yupicturebackend.exception.BusinessException;
import com.tk.yupicturebackend.exception.ErrorCode;
import com.tk.yupicturebackend.model.entity.User;
import com.tk.yupicturebackend.model.enums.UserRoleEnum;
import com.tk.yupicturebackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
//通过AOP实现权限检查，与业务逻辑分离，提高代码可维护性。

//核心拦截方法 - 执行权限校验
// 执行流程：
//     1. 获取注解配置的必须角色
//     2. 获取当前登录用户
//     3. 角色权限校验：
//         - 无角色要求：直接放行
//         - 管理员权限：严格校验
//         - 普通用户权限：基础校验
//     4. 权限不足时抛出 NO_AUTH_ERROR 异常


// 表示这是一个切面类，用于处理横切关注点（如权限校验）
@Aspect
//让Spring管理这个类的实例，可以自动注入到其他组件中
@Component
public class AuthInterceptor {
    @Resource//从Spring容器中获取UserService实例，用于用户相关操作
    private UserService userService;
    /**
     * 执行拦截
     * @param joinPoint 切入点
     * @param authCheck 权限校验注解
       @Around：环绕通知，可以在方法执行前后添加自定义逻辑
     * @return 方法执行结果
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
      /**
      从注解中获取访问该方法所需的角色
      通过RequestContextHolder获取当前HTTP请求信息
      转换为HttpServletRequest对象，用于获取用户信息
      */
        //获取注解中 mustRole 属性的值，将获取到的值赋值给一个字符串变量 mustRole
        // 从注解中获取访问该方法所需的角色
        String mustRole=authCheck.mustRole();
        // 获取当前请求的属性，用于后续获取请求信息
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        // 从请求属性中获取当前HTTP请求对象
        HttpServletRequest request=((ServletRequestAttributes) requestAttributes).getRequest();
        // 从用户服务中获取当前登录用户
        User loginUser=userService.getLoginUser(request);
        // 当前用户角色，将字符串形式的角色转换为枚举类型
        UserRoleEnum mustRoleEnum=UserRoleEnum.getEnumByValue(mustRole);
        // 不需要权限，放行（如果方法不需要特定权限（注解中未指定或指定了空角色），直接放行）
        if(mustRoleEnum==null){
          return joinPoint.proceed();
        }
        // 以下为：必须有该权限才通过
        // 从数据库中获取当前登录用户的角色信息
        String userRole=loginUser.getUserRole();
        // 将字符串形式的角色转换为枚举类型
        UserRoleEnum userRoleEnum=UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        // 没有权限，拒绝
        if(userRoleEnum==null){
         throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无权限");
        }
        // 要求必须有管理员权限，但用户没有管理员权限，拒绝访问并抛出异常
        if(UserRoleEnum.ADMIN.equals(mustRoleEnum)&&!UserRoleEnum.ADMIN.equals(userRoleEnum)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无权限");
        }
        // 通过权限校验，放行、所有权限检查通过，执行原始方法
        return joinPoint.proceed();
    }
}
/*代码解释
注解与组件声明：
1.@Aspect 表示这是一个切面类，用于处理横切关注点（如权限校验）
2.@Component 使其成为Spring管理的Bean

核心拦截逻辑：
1.@Around("annotation(authCheck)") 拦截所有带有 @AuthCheck 注解的方法
2.通过 RequestContextHolder 获取当前请求和用户信息
3.使用 UserRoleEnum 进行角色比较和权限校验

权限校验流程
1.获取注解中指定的必须角色 (mustRole)
2.从数据库中获取当前登录用户的角色信息
3.将字符串形式的角色转换为枚举类型
4.进行角色匹配校验：
   1.无角色要求：直接放行
   2.管理员权限要求：严格校验
   3.普通用户权限：基础校验
异常处理：
权限不足时抛出 BusinessException(ErrorCode.NO_AUTH_ERROR)


设计思路分析：
1.AOP 设计模式：
2.将权限校验逻辑与业务代码解耦
3.通过注解声明权限要求，提高代码可读性

角色枚举设计：

1.UserRoleEnum 定义了清晰的角色体系（用户/管理员）
2.提供 getEnumByValue 方法方便角色值转换

扩展性考虑：

1.可轻松添加新角色类型
2.校验逻辑可扩展（如添加更多权限维度）

安全性保障：

1.默认拒绝原则（除非明确允许）
2.管理员权限严格校验

与系统整合：
1.配合 @AuthCheck 注解使用
2.与 UserService 和 ErrorCode 紧密集成
这种设计实现了声明式的权限控制，使开发者可以专注于业务逻辑，同时保证了系统的安全性。
*/