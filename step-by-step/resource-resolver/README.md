# `@ComponentScan`

## 1 `@ComponentScan` 

Spring 注解，扫描指定包及其子包下的所有 `class`，把所有标注为 `@Component` 的 Bean 构建出来。

若未指定包的路径，则自动扫描该注解标记的类所在的包及其子包。

## 2 实现

`@ComponentScan` 本质上就是在指定包下扫描所有的 Class，然后加载到 JVM，最后实例化。

要在 Classpath 下扫描包及其子包下的所有 Class, 实际上就是在 ClassPath 中搜索所有的文件，找出文件名匹配的 .class 文件。

例如，Classpath 中搜索的文件 org/example/Hello.class 符合包名 org.example，根据文件路径我们可以得到 org.example.Hello，就相当于获得了类名。所以，搜索 Class 就变成了搜索文件。

## 3 知识点

### classpath

#### classpath 是什么？

JVM 的一个环境变量，JVM 根据 classpath 决定搜索 class 的路径和顺序。

JVM 需要知道，如果要加载一个 abc.xyz.Hello 的类，应该去哪搜索对应的 Hello.class 文件。

#### classpath 的形式？

classpath 是一组目录的集合。

现在我们假设 classpath 是 `.;C:\work\project1\bin;C:\shared`，当 JVM 在加载abc.xyz.Hello这个类时，会依次查找：

```
<当前目录>\abc\xyz\Hello.class
C:\work\project1\bin\abc\xyz\Hello.class
C:\shared\abc\xyz\Hello.class
```

注意到 `.` 代表当前目录。如果JVM在某个路径下找到了对应的 class文 件，就不再往后继续搜索。如果所有路径下都没有找到，就报错。

#### classpath 的设置？

```
java -classpath .;C:\work\project1\bin;C:\shared abc.xyz.Hello
java -cp .;C:\work\project1\bin;C:\shared abc.xyz.Hello

# 如果没有设置，默认是 ., 即当前目录
java abc.xyz.Hello
```

#### 示例

假设我们有一个编译后的 Hello.class，它的包名是 com.example，当前目录是 C:\work，那么，目录结构必须如下：

```
C:\work
└─ com
   └─ example
      └─ Hello.class
```

运行这个 Hello.class 必须在当前目录下使用如下命令：

```
C:\work> java -cp . com.example.Hello
```

JVM 根据 classpath 设置的 `.` 在当前目录下查找 com.example.Hello，即实际搜索文件必须位于 com/example/Hello.class。

如果指定的 .class 文件不存在，或者目录结构和包名对不上，均会报错。

### ClassLoader: JVM 的类加载器机制

**ClassLoader**: 在 Java 中，所有的类，都是由 ClassLoader 加载到 JVM 中执行的，但是 JVM 中存在不止一种 ClassLoader。

* `BootClassLoader`: 启动类加载器，用于加载 Java 核心类，如 `java.lang.String`
* `PlatformClassLoader`: 用于加载非核心的 JDK 类，如 `javax.sql.DataSource`
* `AppClassLoader`: 用于加载用户编写的类，如 `Main`

```java
public class Main {
    public static void main(String[] args) {
        System.out.println(String.class.getClassLoader()); // null
        System.out.println(DataSource.class.getClassLoader()); // PlatformClassLoader
        System.out.println(Main.class.getClassLoader()); // AppClassLoader
    }
}
```

我们常说的 classpath 机制，即 JVM 在哪些目录及 jar 包中查找 class, 实际上是指 `AppClassLoader` 使用的 classpath。

**双亲委派模型**：用 `AppClassLoader` 加载一个 class 时，首先会委托给父级 ClassLoader 加载，如果加载失败，再由自己尝试加载。

使用双亲委派模型的目的在于，防止用 `AppClassLoader` 加载用户自己编写的 `java.lang.String`，导致破坏 JDK 的核心类。

对于一个 Class 而言，它始终关联一个加载它自己的 ClassLoader。

## Ref
* classpath 和 jar: https://www.liaoxuefeng.com/wiki/1252599548343744/1260466914339296
* 实现 ClassLoader: https://www.liaoxuefeng.com/wiki/1545956031987744/1545956487069728
* classpath scan: https://stackoverflow.com/questions/520328/can-you-find-all-classes-in-a-package-using-reflection#58773038
