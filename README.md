## Summer Framework
对 *Spring Framework* 的简易实现 <br/>

各模块功能(以及核心组件):

- **Summer-Context**: 对应Spring-IoC模块
  - `com.xuxin.summer`
    - `io`
      - `ResourceResolver`: 在指定包下扫描所有 `Class`
      - `PropertyResolver`: 注入将一系列的配置
    - `context`
      - `AnnotationConfigApplicationContext`: 核心容器
      - `BeanPostProcessor`: 实现对`bean`的代理接口
      - `BeanDefinition`: 存储从注解中提取到的`bean`各方面信息

- **Summer-Aop**: 对应Spring-Aop模块 
  - <p>
    使用第三方库 [net.bytebuddy.byte-buddy] 实现 <br>
    结合了设计模式 适配器 和 代理模式 </p>
  
- **Summer-JDBC**: 通过定义**模板方法**实现数据库功能
  - <p>提供大量以回调作为参数的模板方法 </p>

- Summer-parent: `pom`文件的父模块
 
- Summer-build: 对多`pom`项目进行构建的模块

This project is inspired by the work of [Crypto Michael](https://github.com/michaelliao/summer-framework)