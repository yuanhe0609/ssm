# ssm项目快速搭建 #
## 项目配置 ##
### 开发环境 ###
* server：tomcat 9.0.93
* maven：maven 3.9.8
* jdk：jdk 22
* mysql：mysql 8.0.28
* mybatis：mybatis 3.5.2
* spring：spring 4.3.6.RELEASE
### 技术整合 ###
* druid连接池
* fastJSON2
## 配置文件 ##
* web.xml
```
<web-app xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
                      http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
         version="3.0"
         metadata-complete="true">
    <!--启动后初始页面设置-->
    <welcome-file-list>
        <welcome-file>/jsp/index.jsp</welcome-file>
    </welcome-file-list>
    <!--配置前端控制器-->
    <servlet>
        <servlet-name>springMVC</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>

        <!--配置springmvc的配置文件-->
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>classpath:spring-*.xml</param-value>
        </init-param>
        <load-on-startup>
            1
        </load-on-startup>
    </servlet>
    <servlet-mapping>
        <servlet-name>springMVC</servlet-name>
        <!--直接拦截所有请求，不再采用spring2.0的/*或者*.do方式-->
        <url-pattern>/</url-pattern>
    </servlet-mapping>
</web-app>
```
