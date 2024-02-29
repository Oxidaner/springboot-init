# SpringBoot 项目初始模板

基于 Java SpringBoot 的项目初始模板，整合了常用框架和主流业务的示例代码。

只需 1 分钟即可完成内容网站的后端！！！大家还可以在此基础上快速开发自己的项目。

[toc]

## 模板特点

### 主流框架 & 特性

- Spring Boot 2.7.x（贼新）
- Spring MVC
- MyBatis + MyBatis Plus 数据访问（开启分页）
- Spring Boot 调试工具和项目处理器
- Spring AOP 切面编程
- Spring Scheduler 定时任务
- Spring 事务注解

### 数据存储

- MySQL 数据库
- Redis 内存数据库
- Elasticsearch 搜索引擎
- 腾讯云 COS 对象存储

### 工具类

- Easy Excel 表格处理
- Hutool 工具库
- Apache Commons Lang3 工具类
- Lombok 注解

### 业务特性

- Spring Session Redis 分布式登录
- 全局请求响应拦截器（记录日志）
- 全局异常处理器
- 自定义错误码
- 封装通用响应类
- Swagger + Knife4j 接口文档
- 自定义权限注解 + 全局校验
- 全局跨域处理
- 长整数丢失精度解决
- 多环境配置

## 业务功能

- 提供示例 SQL（用户、帖子、帖子点赞、帖子收藏表）
- 用户登录、注册、注销、更新、检索、权限管理
- 帖子创建、删除、编辑、更新、数据库检索、ES 灵活检索
- 帖子点赞、取消点赞
- 帖子收藏、取消收藏、检索已收藏帖子
- 帖子全量同步 ES、增量同步 ES 定时任务
- 支持微信开放平台登录
- 支持微信公众号订阅、收发消息、设置菜单
- 支持分业务的文件上传

### 单元测试

- JUnit5 单元测试
- 示例单元测试类

### 架构设计

- 合理分层

## 快速上手

> 所有需要修改的地方都标记了 `todo`，便于大家找到修改的位置~

### MySQL 数据库

1）修改 `application.yml` 的数据库配置为你自己的：

```yml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/my_db
    username: root
    password: 123456
```

2）执行 `sql/create_table.sql` 中的数据库语句，自动创建库表

3）启动项目，访问 `http://localhost:8101/api/doc.html` 即可打开接口文档，不需要写前端就能在线调试接口了~

![](doc/swagger.png)

### Redis 分布式登录

1）修改 `application.yml` 的 Redis 配置为你自己的：

```yml
spring:
  redis:
    database: 1
    host: localhost
    port: 6379
    timeout: 5000
    password: 123456
```

2）修改 `application.yml` 中的 session 存储方式：

```yml
spring:
  session:
    store-type: redis
```

3）移除 `MainApplication` 类开头 `@SpringBootApplication` 注解内的 exclude 参数：

修改前：

```java
@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
```

修改后：

```java
@SpringBootApplication
```

### Elasticsearch 搜索引擎

1）修改 `application.yml` 的 Elasticsearch 配置为你自己的：

```yml
spring:
  elasticsearch:
    uris: http://localhost:9200
    username: root
    password: 123456
```

2）复制 `sql/post_es_mapping.json` 文件中的内容，通过调用 Elasticsearch 的接口或者 Kibana Dev Tools 来创建索引（相当于数据库建表）

```
PUT post_v1
{
 参数见 sql/post_es_mapping.json 文件
}
```

这步不会操作的话需要补充下 Elasticsearch 的知识，或者自行百度一下~

3）开启同步任务，将数据库的帖子同步到 Elasticsearch

找到 job 目录下的 `FullSyncPostToEs` 和 `IncSyncPostToEs` 文件，取消掉 `@Component` 注解的注释，再次执行程序即可触发同步：

```java
// todo 取消注释开启任务
//@Component
```

# 项目详解

## 项目主入口



全局配置mapper包的扫描路径

```java
@SpringBootApplication
@MapperScan("com.zzb.springbootinit.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class MainApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

}
```

1. `@SpringBootApplication`：这是一个复合注解，用于标记一个 Spring Boot 应用程序的主类。它包括了 `@Configuration`、`@EnableAutoConfiguration` 和 `@ComponentScan` 等注解，用于自动配置 Spring 应用程序上下文和启动自动配置的功能。
2. `@MapperScan("com.zzb.springbootinit.mapper")`：这是 MyBatis 框架的注解，用于扫描指定包下的 Mapper 接口，将其注册为 Spring 的 Bean，并提供给 MyBatis 进行数据库访问操作。
3. `@EnableScheduling`：这个注解启用了 Spring 的定时任务调度功能。使用该注解后，你可以定义定时任务的方法，并通过表达式或固定时间间隔来调度任务的执行。
4. `@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)`：这个注解启用了 Spring 的 AOP（面向切面编程）功能。`proxyTargetClass = true` 表示使用 CGLIB 代理来创建代理对象，`exposeProxy = true` 表示将代理对象暴露给 AOP 代理链中的其他切面。

## aop

### LogInterceptor

这段代码实现了一个日志拦截器，用于记录每个请求的请求日志和响应日志。该拦截器会拦截`com.zzb.springbootinit.controller`包下的所有方法，并记录每个请求的请求路径、请求参数、执行时间等信息。

```java
/**
 * 日志拦截器，用于记录请求和响应日志
 */
@Aspect // 声明该类为切面类
@Component // 声明该类为Spring组件
@Slf4j // 使用Lombok的日志注解
public class LogInterceptor {

    /**
     * 执行拦截
     * @param point 切入点对象，包含了被拦截方法的信息
     * @return 被拦截方法的执行结果
     * @throws Throwable 异常信息
     */
    @Around("execution(* com.zzb.springbootinit.controller.*.*(..))") 
    // 定义拦截器的切入点，即拦截com.zzb.springbootinit.controller包下的所有方法
    public Object doInterceptor(ProceedingJoinPoint point) throws Throwable {
        // 计时
        StopWatch stopWatch = new StopWatch(); // 创建计时器对象
        stopWatch.start(); // 开始计时
        // 获取请求路径
        
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes(); 
        // 获取当前请求的上下文对象
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest(); 
        // 获取HttpServletRequest对象
        
        // 生成请求唯一 id
        String requestId = UUID.randomUUID().toString(); // 生成UUID作为请求唯一标识符
        String url = httpServletRequest.getRequestURI(); // 获取请求的URI
        // 获取请求参数
        Object[] args = point.getArgs(); // 获取方法的参数
        String reqParam = "[" + StringUtils.join(args, ", ") + "]"; // 将参数转换为字符串
        // 输出请求日志
        log.info("request start，id: {}, path: {}, ip: {}, params: {}", requestId, url,
                httpServletRequest.getRemoteHost(), reqParam); // 输出请求日志
        // 执行原方法
        Object result = point.proceed(); // 执行被拦截的方法
        // 输出响应日志
        stopWatch.stop(); // 停止计时器
        long totalTimeMillis = stopWatch.getTotalTimeMillis(); // 获取执行时间
        log.info("request end, id: {}, cost: {}ms", requestId, totalTimeMillis); // 输出响应日志
        return result; // 返回被拦截方法的执行结果
    }
}
```

#### 具体功能

1. 声明了一个`LogInterceptor`类，并使用`@Aspect`注解将其声明为**切面类**，使用`@Component`注解将其声明为Spring组件，使用`@Slf4j`注解使用Lombok的日志功能。
2. 实现了一个`doInterceptor`方法，使用`@Around`注解定义拦截器的切入点，即拦截`com.zzb.springbootinit.controller`包下的所有方法。该方法的参数是一个`ProceedingJoinPoint`对象，包含了被拦截方法的信息，返回值为被拦截方法的执行结果，可能会抛出异常。
3. 在`doInterceptor`方法中，首先创建了一个`StopWatch`对象，用于计时。然后获取当前请求的`HttpServletRequest`对象，并生成一个唯一的请求标识符。接着**获取被拦截方法的参数，将其转换为字符串，并输出请求日志。**
4. 执行被拦截的方法，并**记录执行时间**。最后输出响应日志，并返回被拦截方法的执行结果。



通过这个日志拦截器，我们可以在控制台或者日志文件中看到每个请求的请求路径、请求参数、执行时间等信息，方便我们进行问题排查和性能优化。

### LogInterceptor



**是一个权限拦截器，用于校验用户的访问权限**。该拦截器会拦截使用了`@AuthCheck`注解的方法，并在方法执行前进行权限校验，只有拥有指定角色的用户才能访问该方法。

#### 源码



```java
/**
 * 权限拦截器，用于校验用户权限
 */
@Aspect // 声明该类为切面类
@Component // 声明该类为Spring组件
public class AuthInterceptor {

    @Resource // 注入UserService对象
    private UserService userService;

    /**
     * 执行拦截
     * @param joinPoint 切入点对象，包含了被拦截方法的信息
     * @param authCheck AuthCheck注解对象，包含了必须的角色信息
     * @return 被拦截方法的执行结果
     * @throws Throwable 异常信息
     */
    @Around("@annotation(authCheck)") // 定义拦截器的切入点，即被拦截方法上有AuthCheck注解
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole(); // 获取必须的角色信息
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
         // 获取当前请求的上下文对象
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
         // 获取HttpServletRequest对象
        // 当前登录用户
        User loginUser = userService.getLoginUser(request); // 获取当前登录用户
        // 必须有该权限才通过
        if (StringUtils.isNotBlank(mustRole)) { // 如果必须有角色信息
            UserRoleEnum mustUserRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
             // 将角色信息转换为枚举类型
            if (mustUserRoleEnum == null) { // 如果角色信息不合法
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR); // 抛出权限异常
            }
            String userRole = loginUser.getUserRole(); // 获取当前用户的角色信息
            // 如果被封号，直接拒绝
            if (UserRoleEnum.BAN.equals(mustUserRoleEnum)) { // 如果必须角色是BAN
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR); // 直接拒绝
            }
            // 必须有管理员权限
            if (UserRoleEnum.ADMIN.equals(mustUserRoleEnum)) { // 如果必须角色是管理员
                if (!mustRole.equals(userRole)) { // 如果当前用户不是管理员
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR); // 抛出权限异常
                }
            }
        }
        // 通过权限校验，放行
        return joinPoint.proceed(); // 执行被拦截方法并返回执行结果
    }
}
```



#### 具体功能



1. 声明了一个`AuthInterceptor`类，并使用`@Aspect`注解将其声明为切面类，使用`@Component`注解将其声明为Spring组件。
2. 在`AuthInterceptor`类中，使用`@Resource`注解注入了一个`UserService`对象。
3. 实现了一个`doInterceptor`方法，使用`@Around`注解定义拦截器的切入点，即拦截使用了`@AuthCheck`注解的方法。该方法的参数是一个`ProceedingJoinPoint`对象，包含了被拦截方法的信息和一个`AuthCheck`注解对象，包含了必须的角色信息，返回值为被拦截方法的执行结果，可能会抛出异常。
4. 在`doInterceptor`方法中，首先获取必须的角色信息和当前请求的`HttpServletRequest`对象。然后获取当前登录用户的信息，并根据必须的角色信息进行权限校验。如果必须的角色信息不合法，或者当前用户不满足必须的角色要求，就会抛出权限异常。否则，将会放行，执行被拦截的方法，并返回执行结果。

**总之，这段代码的作用是为使用了`@AuthCheck`注解的方法提供权限校验功能，保证只有拥有指定角色的用户才能访问该方法。**

*通过在主入口类上添加* *`@EnableAspectJAutoProxy`\** *注解，你启用了 Spring 的 AOP 功能，使得拦截器可以被识别和应用到目标方法上。*

## annotation



#### AuthCheck



**自定义一个权限，如果一个方法使用了该注解，并指定了必须的角色信息，那么只有拥有该角色的用户才能访问该方法。**

##### 源码

```java
/**
 * 该注解用于标注需要进行权限校验的方法
 */
@Target(ElementType.METHOD) // 定义该注解只能用于方法上
@Retention(RetentionPolicy.RUNTIME) // 定义该注解在运行时保留
public @interface AuthCheck {

    /**
     * 必须有某个角色才能访问该方法
     * @return 必须的角色信息
     */
    String mustRole() default ""; 
    // 定义一个名为mustRole的属性，表示必须的角色信息，默认为空字符串

}
```

##### 具体功能



该代码是一个自定义注解`@AuthCheck`，用于标记需要进行**权限校验**的方法。该注解只能用于方法上，并在运行时保留。该注解包含一个名为mustRole的属性，表示必须的角色信息，默认为空字符串。

1. `@Target(ElementType.METHOD)`表示这个注解只能应用到方法上。
2. `@Retention(RetentionPolicy.RUNTIME)`表示这个注解在程序运行时仍然有效。

### config包



包括：`JsonConfig、Knife4jConfig、MyBatisPlusConfig、CorsConfig、CosClientConfig`



这个包主要是做一些配置，可以直接作为轮子拿来使用的，我们不用详细看懂代码，会用即可



1. `CorsConfig`：解决全局跨域配置问题
2. `MyBatisPlusConfig`：`@MapperScan`用于指定扫描的路径，需要修改为你自己的，就是我们用mybatisX生成的代码路径，我们统一放在**mapper**层的。
3. `Knife4jConfig` ：用于后端接口文档在线测试的配置类
4. `JsonConfig` ： Long 转 json 精度丢失的配置
5. `CosClientConfig`：腾讯云对象存储客户端，在`application.yml`中配置，如果没用到云存储可以不关注

## common包



**定义一些常量与公共类**

### ResultUtils



**返回工具类**

#### 源码

```java
public class ResultUtils {

    /**
     * 成功
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 失败
     *
     * @param errorCode
     * @return
     */
    public static BaseResponse error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     *
     * @param code
     * @param message
     * @return
     */
    public static BaseResponse error(int code, String message) {
        return new BaseResponse(code, null, message);
    }

    /**
     * 失败
     *
     * @param errorCode
     * @return
     */
    public static BaseResponse error(ErrorCode errorCode, String message) {
        return new BaseResponse(errorCode.getCode(), null, message);
    }
}
```

1. `success(T data)`：该方法用于创建一个成功的响应对象。它接受一个泛型参数 data，表示成功时返回的数据。该方法会创建一个 `BaseResponse` 对象，并设置状态码为 0，数据为传入的 data，消息为 "ok"。
2. `error(ErrorCode errorCode)`：该方法用于创建一个失败的响应对象。它接受一个 `ErrorCode` 参数 `errorCode`，表示失败的错误码。该方法会创建一个 `BaseResponse` 对象，并使用传入的错误码构造响应对象。
3. `error(int code, String message)`：该方法用于创建一个失败的响应对象。它接受一个整型参数 code，表示失败的状态码，以及一个字符串参数 `message`，表示失败的消息。该方法会创建一个 `BaseResponse` 对象，并使用传入的状态码和消息构造响应对象。
4. `error(ErrorCode errorCode, String message)`：该方法用于创建一个失败的响应对象。它接受一个 `ErrorCode` 参数 `errorCode`，表示失败的错误码，以及一个字符串参数 `message`，表示失败的消息。该方法会创建一个 `BaseResponse` 对象，并使用传入的错误码和消息构造响应对象。

`ResultUtils` 类是一个辅助类，用于生成标准的响应结果。它包含了一些静态方法，用于创建成功或失败的响应对象。

### BaseResponse



**通用返回类**

#### 源码

```java
@Data
public class BaseResponse<T> implements Serializable {

    // 状态码
    private int code;

    // 数据
    private T data;

    // 消息
    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
```

#### 具体功能



这段代码定义了一个名为`BaseResponse`的类，该类继承了`Serializable`接口。这个类主要用于封装响应数据，包括状态码、数据和消息。



1. @Data注解：这个注解来自lombok库，用于自动生成getter和setter方法。
2. `public class BaseResponse<T>：`定义一个泛型类`BaseResponse`，其中T是一个类型变量，表示响应数据的具体类型。这个类有一个公共的构造函数，用于初始化状态码、数据和消息。
3. `private int code;`：定义一个私有整型变量code，表示状态码。
4. `private T data;`：定义一个私有泛型变量data，表示响应数据。
5. `private String message;`：定义一个私有字符串变量message，表示消息。
6. `public BaseResponse(int code, T data, String message)：`定义一个公共构造函数，接收三个参数：code表示状态码，data表示响应数据，message表示消息。
7. `public BaseResponse(int code, T data)：`定义一个简化的构造函数，接收两个参数：code表示状态码，data表示响应数据。这个构造函数调用了之前的公共构造函数，并传入了默认的空字符串消息。
8. `public BaseResponse(ErrorCode errorCode)：`定义一个特定的构造函数，接收一个参数`errorCode`，表示错误码。这个构造函数调用了之前的公共构造函数，并将错误码的状态码和消息传递给`BaseResponse`对象。

### DeleteRequest



删除请求

#### 源码

```java
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
```

这个类的作用是定义一个删除请求的结构。id 属性表示要删除的记录的 ID。

### ErrorCode



**自定义错误码，之前封装的通用返回类可以使用这个类定义好的错误码以及返回方法**

#### 源码

```java
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
```

#### 具体功能

这段代码定义了一个名为`ErrorCode`的枚举类，用于表示错误代码及其对应的信息。枚举类是一种特殊的类，其中包含一组命名的值，这些值称为枚举成员。在这个例子中，`ErrorCode`枚举类包含了7个枚举成员，分别表示不同的错误代码及其信息。



每个枚举成员都有一个code和message字段，分别表示错误代码和错误信息。这些字段在枚举成员被创建时被初始化，并且是私有的，外部代码无法直接访问。



`ErrorCode`枚举类提供了两个公有的方法`getCode()`和`getMessage()`，用于获取枚举成员的错误代码和错误信息。这些方法也是私有的，这意味着只能在`ErrorCode`类内部访问，不能在其他类中使用。



最后，`ErrorCode`枚举类有一个构造函数，用于创建枚举成员。这个构造函数是私有的，这意味着不能在其他类中使用，只能通过`ErrorCode`类本身创建枚举成员。

### PageRequest



分页请求：定义一个统一的分页请求类

#### 源码

```java
@Data
public class PageRequest {

    /**
     * 当前页号
     */
    private long current = 1;

    /**
     * 页面大小
     */
    private long pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder = CommonConstant.SORT_ORDER_ASC;
}
```

这段代码定义了一个名为ErrorCode的枚举类，用于表示错误代码及其对应的信息。枚举类是一种特殊的类，其中包含一组命名的值，这些值称为枚举成员。在这个例子中，ErrorCode枚举类包含了7个枚举成员，分别表示不同的错误代码及其信息。



每个枚举成员都有一个code和message字段，分别表示错误代码和错误信息。这些字段在枚举成员被创建时被初始化，并且是私有的，外部代码无法直接访问。



ErrorCode枚举类提供了两个公有的方法getCode()和getMessage()，用于获取枚举成员的错误代码和错误信息。这些方法也是私有的，这意味着只能在ErrorCode类内部访问，不能在其他类中使用。



最后，`ErrorCode`枚举类有一个构造函数，用于创建枚举成员。这个构造函数是私有的，这意味着不能在其他类中使用，只能通过`ErrorCode`类本身创建枚举成员。



## constant包



这个包是定义一些在项目中使用的常量，我们看这个`UserConstant`类

```java
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    // endregion
}
```

1. `USER_LOGIN_STATE` 是一个表示用户登录态的常量，在用户登录时用作存储在会话（Session）或其他存储机制中的键，用于标识用户是否已登录。
2. `DEFAULT_ROLE` 是一个表示默认角色的常量
3. `ADMIN_ROLE` 是一个表示管理员角色的常量
4. `BAN_ROLE` 是一个表示被封号角色的常量

*通过在接口中定义这些常量，我们可以在代码的不同部分引用和使用它们，避免硬编码常量的出现，提高代码的可读性和可维护性。*

## exception包

这个包下主要定义与抛出异常有关的类，也可以直接拿来复用来节省我们的开发时间，比如`ThrowUtils`



这个抛异常工具类与自定义异常类`BusinessException`

```java
public class ThrowUtils {

    /**
     * 条件成立则抛异常
     *
     * @param condition
     * @param runtimeException
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition
     * @param errorCode
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition
     * @param errorCode
     * @param message
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }
}

```

```java
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public int getCode() {
        return code;
    }
}

```

`BusinessException` 类是一个自定义的异常类，用于表示业务异常，并且提供了多个构造方法以适应不同的使用场景。比如我们在判空的时候，就可以用这个通用的抛出异常类



`ThrowUtils` 类，其中包含了几个静态方法用于条件判断并抛出异常。这些方法的设计目的是提供一种简洁的方式来进行条件检查并抛出异常。通过使用这些方法，可以减少代码的重复性，并且使代码更易于阅读和维护。

## model层



这一层下包含了四个包与**数据库交互**有关，我们来分别看一下。



### entity包

这个包是存放与数据库对应的实体类的，我们就拿一个User类来举例好了：

```java
@TableName(value = "user")
@Data
public class User implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 开放平台id
     */
    private String unionId;

    /**
     * 公众号openId
     */
    private String mpOpenId;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
```

这个类的属性是和数据库user表的列明对应起来的，方便后续创建对象对数据库进行增删改查

![img](https://article-images.zsxq.com/FutrbkZmhwVr_jvLMINLUBNZb5ye)



### 枚举类：enums包

**有了枚举类，我们可以增加代码的可读性，减少代码中魔法值的出现。**

#### UserRoleEnum

#### 源码

```java
public enum UserRoleEnum {

    USER("用户", "user"),    // 用户角色，值为"user"
    ADMIN("管理员", "admin"),    // 管理员角色，值为"admin"
    BAN("被封号", "ban");    // 被封号角色，值为"ban"

    private final String text;    // 角色名称
    private final String value;    // 角色值

    /**
     * 构造函数
     *
     * @param text  角色名称
     * @param value 角色值
     */
    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return 值列表
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据值获取枚举
     *
     * @param value 值
     * @return 枚举
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (UserRoleEnum anEnum : UserRoleEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    /**
     * 获取角色值
     *
     * @return 角色值
     */
    public String getValue() {
        return value;
    }

    /**
     * 获取角色名称
     *
     * @return 角色名称
     */
    public String getText() {
        return text;
    }
}

```

#### 具体功能

这个类是一个枚举类，用于表示用户角色的枚举类型。每个枚举值包含一个文本描述和一个值，分别表示用户角色的名称和对应的值。此外，这个类还提供了一些静态方法，用于获取值列表和根据值获取枚举。



### dto包

我们可以看到这个包下的的类名都统一命名为`xxxRequest`，很明显是用来统一封装前端请求的参数的，我们来看`UserLoginRequest`的代码

```java
/**
 * 用户登录请求
 *
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    private String userAccount;

    private String userPassword;
}

```

#### 具体功能

`UserLoginRequest` 类的作用是定义了一个用于封装用户登录请求数据的实体类或数据传输对象。注意到这个类实现了 `Serializable` 接口，表示该类的实例可以被序列化和反序列化。这使得该类的实例可以在分布式系统中进行跨网络传输或持久化存储。也就是说，这个类在前端请求后端的API的时候，在`Controller`层封装成类，方便取出参数：`@RequestBody UserLoginRequest userLoginRequest`

### vo包



我们可以看到这个包下的的类名都统一命名为`xxxVO`，这个包下的类，主要是封装部分信息比如用户信息脱敏后的信息，在某些情况下，我们可能不希望直接将完整的实体类暴露给外部或其他层，比如我们可以看`UserVO`这个类

```java
public class UserVO implements Serializable {

    /**
     * 用户 id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
```

对比我们上面的User实体类，可以发现少了一些字段，比如像用户账号、密码等敏感信息。是**脱敏**后返回给用户的视图。

**当然vo包下的类通常用于在不同的层之间传递数据或封装多个实体类的属性，并不仅仅可以做信息脱敏的需求，它在设计上的目的是为了提供一种轻量级的数据传输对象，以满足特定的业务需求。**

## Controller层

Controller层是用于处理接收到的客户端（浏览器）请求，并根据请求执行相应的操作，也就是写接口的。它是MVC（模型-视图-控制器）架构中的控制器组件。我们还是来看与用户类相关的控制层的部分代码UserController。

```java
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    // region 登录相关

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }
    
 }
```

### 具体功能

1. `@RestController` 是一个组合注解，表示这个类是一个控制器（Controller）类，并且其中的方法都会返回RESTful风格的响应。
2. `@RequestMapping("/user")` 注解是做路径映射的，用于将URL路径 "/user" 映射到该控制器类上。当接收到以 "/user" 开头的请求时，该控制器类将负责处理该请求。
3. `@Resource` 注解用于标记需要注入的依赖对象，注入要调用的service业务层
4. `@PostMapping("/register")` 和 `@PostMapping("/login")` 注解分别将 "/register" 和 "/login" 路径映射到相应的方法上。当接收到以 "/user/register" 或 "/user/login" 开头的POST请求时，对应的方法将被调用。
5. userLogin 方法用于处理用户登录请求。它接收一个 `UserLoginRequest` 对象作为请求体（根据前端传参封装的），根据请求中的用户账号和密码调用 `userService.userLogin` 方法进行用户登录，并返回一个包装了登录用户信息的 `BaseResponse` 对象，这里包装的对象就是我们刚刚看到的脱敏后的信息。



> 关键就是对几个注解使用的掌握，以及写一些实现业务逻辑的接口，具体的业务实现是要交给我们的Service层的。比如像这个注册登录的接口，我们学会之后，在所有需要做登录校验的系统里面，我们是不是就可以直接拿来使用了，最多就是改成我们想要的传参逻辑来修改代码，或者增加一些短信登录的功能。

## Service层



Service层是应用程序的一部分，位于控制器（Controller层）和持久化层（DAO层）之间。它主要**负责处理业务逻辑和业务规则**。我们也具体来看看UserService的代码：

```java
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


}
```

我们发现这个类**继承**了mybatis-plus一系列通用的方法，例如对用户的增删改查等操作。我们可以在这个类里面定义具体的业务接口比如userLogin（也就是刚刚我们在Controller层调用的），然后再Impl包里去实现它，**至于为什么我们要这么定义，这是MVC 开发框架的规范，我们只要遵守就好了。**

### Impl

我们来看看UserServiceImpl类

```java
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "zzb";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }
}
```

这个类使用**@Service** 注解进行标记，表示它是一个服务组件，用来具体实现业务逻辑的。

**@Slf4j** 注解是 lombok 提供的注解，用于自动生成日志对象，可以通过 log 对象进行日志记录。

UserServiceImpl 类实现了 UserService 接口中定义的两个方法：**userRegister 和 userLogin。**

代码不难理解，就是做对参数的校验逻辑，然后对数据库进行操作，持久化后返回结果即可。

## 总结



我们从上述的整个业务开发流程讲解，我们可以总结出来一套万用的接口开发方法：

1. 创建好规范的包：model层、controller层、service层**（MVC）**
2. 使用**mybatisX**生成与数据库对应的pojo实体类，mapper文件，service类，移动到对应的包里
3. 在Controller层里定义好接口访问路径，然后将需要service注入进来，写方法的时候确定好前端传的参数，然后去调用service里具体业务逻辑实现的方法。
4. 在service接口层里定义好需要实现的方法
5. 在serviceImpl里具体实现接口里对应的方法

## 工具类



一共有三个工具类：NetUtils、SpringContextUtils、SqlUtils，我们一个一个来看：

### NetUtils



是一个网络工具类

```java
  public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (ip.equals("127.0.0.1")) {
                // 根据网卡取本机配置的 IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (inet != null) {
                    ip = inet.getHostAddress();
                }
            }
        }
        // 多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        if (ip == null) {
            return "127.0.0.1";
        }
        return ip;
    }
```

作用是从 **`HttpServletRequest`** 对象中提取客户端的真实 IP 地址。由于在实际的网络环境中，可能存在代理、负载均衡等中间层，因此通过 **`request.getRemoteAddr()`** 方法获取的 IP 地址可能不准确。为了获取真实的客户端 IP 地址，该方法通过检查一系列的请求头信息来确定 IP 地址。



### SpringContextUtils

Spring 上下文获取工具类

```java
@Component
public class SpringContextUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        SpringContextUtils.applicationContext = applicationContext;
    }

    /**
     * 通过名称获取 Bean
     *
     * @param beanName
     * @return
     */
    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    /**
     * 通过 class 获取 Bean
     *
     * @param beanClass
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }

    /**
     * 通过名称和类型获取 Bean
     *
     * @param beanName
     * @param beanClass
     * @param <T>
     * @return
     */
    public static <T> T getBean(String beanName, Class<T> beanClass) {
        return applicationContext.getBean(beanName, beanClass);
    }
}
```

作用是用于提供了静态方法通过名称、类型、名称和类型来获取 Spring 应用程序上下文中的 Bean 实例，通过依赖注入 **SpringContextUtils** 类，其他组件可以方便地获取所需的 Bean 实例。



### SqlUtils



SQL 工具类

```java
public class SqlUtils {

    /**
     * 校验排序字段是否合法（防止 SQL 注入）
     *
     * @param sortField
     * @return
     */
    public static boolean validSortField(String sortField) {
        if (StringUtils.isBlank(sortField)) {
            return false;
        }
        return !StringUtils.containsAny(sortField, "=", "(", ")", " ");
    }
}

```

`StringUtils.isBlank(sortField)` 来检查排序字段是否为空或只包含空格。



`StringUtils.containsAny(sortField, "=", "(", ")", " ")` 来检查排序字段是否包含特定的字符，如等号（=）、左括号（(）、右括号（)）和空格。

这段代码用于验证排序字段是否有效。它检查排序字段是否为空或只包含空格，并且不包含特定的字符（等号、左括号、右括号和空格）。如果满足这些条件，则判断排序字段为有效；否则，判断排序字段为无效。

可以有效地防止 SQL 注入

