# 秒杀项目技术点概览

## md5加密

客户端--》第一次MD5加密--》后端--》第二次MD5加密--》数据库

MD5（MD5（密码明文+固定盐）+随机盐）

原因：第一次加密是为了客户端向服务器端传入数据时更安全
第二次是因为当数据库被盗用时，可能通过盐推算出明文密码，所以再次加密存入数据库里更安全。就算数据库被盗用，使用盐反编译完了还是一层MD5加密后的密码。


## @Valid参数校验 
实现代码：
```java
public RespBean doLogin(@Valid LoginVo loginVo){
        return userService.doLogin(loginVo);
    }
    
    =================
    
   public class LoginVo {
    @NotNull
    private String mobile;

    @NotNull
    @Length(min = 32)
    private String password;
}
```

## 自定义注解参数校验

1. 添加依赖

2. 对需要校验的对象加上@Valid注解，这样自定义校验注解以及非自定义校验注解才会生效，如3

3. 自定义注解，如@IsMobile

   ```java
   @Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
   @Retention(RetentionPolicy.RUNTIME)
   @Documented
   //@Constraint:自定义校验规则   重中之重
   @Constraint(
           validatedBy = { IsMobileValidator.class}
   )
   public @interface IsMobile {
   
       /**
        * 要求手机号码是否为必填，默认为ture
        * @return
        */
       boolean required() default true;
   
       /**
        * 报错信息
        * @return
        */
       String message() default "手机号码格式错误";
   
       Class<?>[] groups() default {};
   
       //没有payload()会报错
       Class<? extends Payload>[] payload() default {};
   
   }
   ```

4. 自定义规则

   ```java
   public class IsMobileValidator implements ConstraintValidator<IsMobile,String> {
       //是否必填
       private boolean required = false;
   
       //初始化获取参数注解required的值，ture还是false，传给属性
       @Override
       public void initialize(IsMobile constraintAnnotation) {
           required = constraintAnnotation.required();
       }
   
       @Override
       public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
           if(required){
               //如果要求必填，就要对填入的value(手机号)进行校验
               return ValidatorUtil.isMobile(value);
           }else{
               //如果手机号码要求非必填，并且value(手机号码)为空，那就直接返回ture。因为没填肯定为空啊
               if(StringUtils.isEmpty(value)){
                   return true;
               }else{
                   //对填入的value(手机号)进行校验
                   return ValidatorUtil.isMobile(value);
               }
           }
       }
   }
   ```

## 异常处理

### 方式一：

@ControllerAdvise+@ExceptionHandler

@ControllerAdvise只能处理控制器抛出的异常

```java
@RestControllerAdvice//加rest意味返回的responseBody
public class GlobalExceptionHandler {

    //ExceptionHandler(Exception.class): 需要处理异常的类
    @ExceptionHandler(Exception.class)
    public RespBean ExceptionHandler(Exception e){
        if(e instanceof GlobalException){
            GlobalException ex = (GlobalException)e;
            return RespBean.error(ex.getRespBeanEnum());
        }else if(e instanceof BindException){
            BindException ex = (BindException)e;
            RespBean respBean = RespBean.error(RespBeanEnum.BIND_ERROR);
            respBean.setMessage("参数校验异常："+ ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
            return respBean;
        }
        return RespBean.error(RespBeanEnum.ERROR);
    }
}
```



### 方式二：

ErrorController类   
处理所以的异常，包括没有进入控制器的异常比如404
可以定义不同的拦截方法比如页面跳转

```java
@RestController
public class MainsiteErrorController implements ErrorController {
    Logger logger = LoggerFactory.getLogger(MainsiteErrorController.class);

    private final String ERROR_PATH ="/error";

    /**
     * 出现错误，跳转到如下映射中
     * @return
     */
    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }

    /**
     * Web页面错误处理
     */
    @RequestMapping(value = ERROR_PATH, produces = {"text/html"})
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        int code = response.getStatus();
        if (404 == code) {
            return new ModelAndView("error/404");
        } else if (403 == code) {
            return new ModelAndView("error/403");
        } else if (401 == code) {
            return new ModelAndView("login");
        } else {
            return new ModelAndView("error/500");
        }
    }

    @RequestMapping(value =ERROR_PATH)
    public R handleError(HttpServletRequest request, HttpServletResponse response) {
        int code = response.getStatus();
        if (404 == code) {
            return R.error(404, "未找到资源");
        } else if (403 == code) {
            return R.error(403, "没有访问权限");
        } else if (401 == code) {
            return R.error(401, "登录过期");
        } else {
            return R.error(500, "服务器错误");
        }
    }

}

```

## 分布式session

session一般存在服务器中，但负载均衡轮询的策略会导致分配到其他服务器，而其他服务器上没有存session，导致出现无法免登陆啊之类的问题，以下方法可以实现分布式session

1. 使用springsession实现
   无需重写代码 只需导入依赖即可
2. 用redis解决
   不在tomcat服务器里存放了，单独用redis存放，无论轮询到哪个服务器里都要在redis里取出值

## 自定义参数校验

缘由：实现免登陆功能时，控制层每次都需要通过cookie的值，去服务层获取user对象，相对麻烦，所以希望直接参数传入user对象，进行判断(为空第一次说明登录返回login，不为空则转到相应的页面)

首先明白英语两个词

*parameter*=形参
*argument*=实参

步骤

1. 写一个mvc配置类，实现WebMvcConfigurer接口
2. 实现方法addArgumentResolvers (中文直译添加实参解析器)
3. 写个自定义参数解析类，需要实现HandlerMethodArgumentResolver接口，实现两个方法
4. 然后把3添加到2里

```java
@EnableWebMvc
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private UserArgumentResolver userArgumentResolver;
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userArgumentResolver);
    }
}
```

```java
@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
    @Autowired
    private IUserService userService;
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType()== User.class;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        String ticket = CookieUtil.getCookieValue(request, "userTicket");
        if (StringUtils.isEmpty(ticket)){
            return null;
        }
        User user = userService.getUserByCookie(request, response, ticket);
        return user;
    }
}
```



## 优化

### 页面缓存

不直接去后端请求数据再渲染，直接从redis里拿到已经渲染好的数据，如果redis里没有，则手动渲染，存入redis在进行返回。

```java
//页面静态化处理，返回ResponseBody
    @RequestMapping(value = "/toList",produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toList(Model model,User user,HttpServletRequest request,HttpServletResponse response){
        //先从redis里获取商品列表页面，如果不为空，则直接返回
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsList");
        if (!StringUtils.isEmpty(html)){
            return html;
        }
        //如果为空，通过thymeleafViewResolver手动渲染后存入redis，再返回
        model.addAttribute("user",user);
        model.addAttribute("goodsList",goodsService.findGoodsVo());
        //1.获取context对象，model.asMap()为需要传入数据的map，手动渲染
        WebContext context = new WebContext(request, response,
                request.getServletContext(), request.getLocale(),model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", context);
        //2.存入redis，返回
        if (!StringUtils.isEmpty(html)){
            valueOperations.set("goodsList",html,1, TimeUnit.MINUTES);
        }
        return html;
    }
```



### 对象缓存

**缓存和数据库一致性问题**

通过redis实现对象缓存，不过若数据库进行更新，必须要清空缓存，更新完再写入缓存

### 页面静态化

前后端分离

虽然可以通过redis缓存页面提高性能，但毕竟页面数据太大，所以优化也不咋地，所以使页面完全静态化，通过浏览器的缓存，服务器只需要传入所需的vo对象，减少传输量

yum配置文件里配置静态资源处理

```yaml
spring:
  web:
  # 静态资源处理
    resources:
      static-locations: classpath:/static/
      cache:
        cachecontrol:
          # 缓存最大时间
          max-age: 3600
      chain:
        # 资源链启动缓存 默认启动
        cache: true
        # 启动资源链 默认禁用
        enabled: true
        # 启动压缩资源（gzip brotli）解析， 默认禁用
        compressed: true
        # 启用h5应用缓存 默认禁用
        html-application-cache: true
      add-mappings: true
```



### 解决超卖问题

通过加索引的方法
因为秒杀需要用户id和商品id，所以通过用户id＋商品号生成唯一索引，防止重复购买。修改sql语句，加上了库存大于0的判断哪，防止库存变成负数

### rediss实现预减库存

流程：

1. 实现InitializingBean接口，重写afterPropertiesSet方法，先把每个商品和库存通过循环加载到redis里，并对每个商品进行标记，flase代表有库存
2. 进入秒杀方法，判断用户是否为空，判断是否重复抢购，通过标记查看秒杀的商品是否为true，为true说明没有库存，则返回空库存。
3. Redis进行预减库存，当秒杀商品库存小于0时，标记其为true，并返回空库存。
4. 当秒杀商品库存大于0时，使用rabbitmq异步秒杀
   1. 创建rabbitmq配置类，配置topic模式的队列、交换机并进行绑定
   2. 创建生产者MQsender类，编写sendSeckillMessage方法
   3. 控制层调用sendSeckillMessage方法，发送用户和秒杀商品id
   4. 创建消费者MQReceiver来接受队列消息进行逻辑判断
      1. 判断库存，库存小于1直接返回
      2. 判断是否重复下单，是则直接返回
      3. 1和2都不满足，则进行下单操作
5. 异步返回请求，告知用户秒杀成功，正在为您下单，实则通过轮询操作，查看是够下单成功
   1. 前端ajax请求/seckill/result接口，调用getResult方法传入goodsId
   2. getResult方法里调用服务层执行业务逻辑，获取orderId(成功的情况下为orderId，否则为0，-1)
      1. 获取秒杀订单id不为空就返回订单id
      2. 如果秒杀订单id为空，且库存则返回-1，否则返回0。(秒杀失败返回-1，等待中返回0，成功直接返回订单号)
   3. 前端业务逻辑
      1. 获取orderId，小于0弹窗告知失败
      2. 等于0，说明排队中，轮询getResult方法(自己调自己)
      3. 不为0，不为-1，则秒杀成功，跳转详情页

rabbitmq异步操作，流量削峰

### 安全优化

#### 秒杀地址隐藏

流程

1. 点击立即秒杀按钮跳转function getSeckillPath()函数
2. 获取goodsId，通过ajax向接口/seckill/path异步请求，调用getPath方法
3. getPath方法内调用createPath创造地址path，返回公共对象带上地址
   - createPath方法通过UUID生成唯一的地址并存入redis
4. function getSeckillPath()内，如果状态码为200，带上path调用doSeckill方法，否则打印信息或报错
5. doSeckill函数内，ajax异步请求'/seckill/'+path+'/doSeckill'接口
   1. 接口方法内除了之前的业务逻辑外，新增check方法校验秒杀地址，校验方法为通过@PathVariable传入的path是否与redis里一样，不一样秒杀方法返回非法请求错误



## 接口限流

使用计数器算法，并自定义注解实现接口限流

流程

1. 编写注解类AccessLimit，属性有限流时间，限流次数，是否需要登录

2. 编写AccessLimitInterceptor拦截器类，该类需要实现HandlerInterceptor接口并重写preHandle方法

   **preHandler流程**

   1. 首先判断是否属于HandlerMethod，因为要处理方法
   2. 判断AccessLimit是否为空，为空直接跳过拦截
   3. 获取注解信息：限流时、次数、是否需要登录
   4. 需要登录则先获取当前查看用户是否为空，为空返回错误信息，使用render方法用流输出json数据类型
   5. 具体使用redis实现计数器算法的业务逻辑

3. 在web配置类里使用registry.addInterceptor(ccessLimitInterceptor)添加到mvc里使用



## redis实现分布式锁

使用lua脚本

流程

1. 编写配置类
2. 编写lua脚本
3. 通过excuse方法调用脚本


## 压测结果

### 商品列表接口(数据库读取操作)

线程数1000×循环10次×测量3次=3万

吞吐量QPS

- 优化前 207
- 缓存优化后 390 

 

### 秒杀接口(数据库读取·写入操作)

5000个用户：线程数1000×循环10次×测量3次=3万

吞吐量QPS

- 优化前 170 并且出现超卖情况
- 缓存优化后 320 无超卖问题 
- 消息中间件优化后 590









