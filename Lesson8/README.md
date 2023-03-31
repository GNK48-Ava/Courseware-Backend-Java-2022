

# Netty

## 什么是netty?

这个是官方文档对netty的定义:

> Netty致力于提供一个异步事件驱动的网络应用框架和工具，用于快速开发可维护的高性能和高可伸缩性协议服务器和客户端。

## echo服务器

我们从一个简单的echo(回声)服务器来对netty有个简单的印象吧。

首先，新建一个maven项目，在pom.xml中引入netty的依赖:

```xml
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-all</artifactId>
    <version>4.1.39.Final</version>
</dependency>
```

首先，创建出一个EchoServerHandler:

```java
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.EventExecutorGroup;


public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ctx.write(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
```

然后创建一个EchoServer:

```java
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;


public class EchoServer {

    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new EchoServerHandler());
                        }
                    });

            ChannelFuture f = b.bind().sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws Exception {
        new EchoServer(8081).run();
    }
}
```

运行程序，连接到我们的echo服务器，这里我使用cmd的nc进行连接，可以看到，这里我们输入了什么数据，服务器就给我们返回了相同的数据



![](https://raw.githubusercontent.com/GNK48-Ava/imgs/main/fasjkfbb%26%24%40%7BfAFS454mkl)



我们先来简单讲讲nio吧，毕竟netty就是基于nio构建的，了解nio有助于我们更好了解netty，



## NIO

### Buffer

buffer一般用来存放缓冲的读写数据，而其中，我们用的较多的是

#### ByteBuffer

```java
import java.nio.ByteBuffer;


public class ByteBufferDemo {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put((byte) 1);
        buffer.put((byte) 2);
        buffer.put((byte) 3);

        buffer.flip();//切换至读模式

        System.out.println(buffer.get());
        System.out.println(buffer.get());
        
    }
}
```

可以看到中间我们调用了flip()方法来切换到读模式，这是跟它特殊的结构有关:

Buffer有以下三个重要的属性:

- capacity :顾名思义，即Buffer的容量，可最多存放的数据数大小，一旦确定后，无法更改

- position：Buffer 当前读写的位置，即下一个要读取或写入的位置。Buffer 初始的 position 值为 0。

- limit：Buffer 读写的限制，即可以读写的数据范围。对于读操作，limit 是已经读取的数据的上限，对于写操作，limit 是还可以写入的数据的上限。Buffer 初始的 limit 值等于 capacity。

  

  ![](https://raw.githubusercontent.com/GNK48-Ava/imgs/main/lesson8/QVV4ZMPEHRH.png)

  

但我们依次存入1、2、3时，ByteBuffer的结构如下变化:

![](https://raw.githubusercontent.com/GNK48-Ava/imgs/main/lesson8/C4MJX9JCGUYUE9XDTD.png)

![](https://raw.githubusercontent.com/GNK48-Ava/imgs/main/lesson8/T9UIX%401PKZZDEJ%7EE%600.png)

![](https://raw.githubusercontent.com/GNK48-Ava/imgs/main/lesson8/DVU%5B%24D6M1G%404G%60%5DZ07PVE.png)

可以看出这个过程只有position在不断前移。并且现在没有一个指针指向我们想取出的数据123的，如果不进行一点操作的话，肯定是拿不到我们的数据的。这个时候，就需要调用flip()了。调用后，Buffer的结构如下:

![](https://raw.githubusercontent.com/GNK48-Ava/imgs/main/lesson8/XP2B8_4LX9SAZ987CB23M.png)

可以看到，调用flip()后，limit指向position的原位置，而position则指向了0位，即数组的第一个元素。这样子我们就能愉快的调用get()来获得ByteBuffer中的数据了。

我们调用get()方法时的变化我就不一一演示出来了，只展示最终结果:

![](https://raw.githubusercontent.com/GNK48-Ava/imgs/main/lesson8/CZID%7EXT9BVB9BV%25OVF6P.png)

那么如果我们现在想要往buffer里写数据呢?也很简单，切换至写模式就好了。与切换至读模式不同，写模式有两个方法可供我们选择:

```java
buffer.clear();
buffer.compact();
```

clear()方法实际上就是将buffer设为初始状态。

而compact方法会将未读完的数据前移，然后再进入读模式。调用compact方法后Buffer的结构如下:

![](https://raw.githubusercontent.com/GNK48-Ava/imgs/main/lesson8/MQEJB6FM7%60HE36H3TJ%7EEM%7E7.png)

无论是上面哪两个方法，我们都会发现，我们读取过的数据都已经消失了。

那么，如果我们想重复读取ByteBuffer 的数据呢？

实际上，Buffer还有一个重要的属性，那就是mark，他能够标记当前position的位置。

```java
ByteBuffer.mark();
ByteBuffer.reset()
```

当调用 `ByteBuffer.mark()` 时，它将设置一个标记位置（默认为零），该位置通常是当前位置。之后，当调用 `ByteBuffer.reset()` 时，`ByteBuffer` 的位置将被重置为标记位置，以便可以重新读取或写入相同的数据。

`mark()` 和 `reset()` 方法通常用于一些需要多次读取或写入的场景中，可以减少代码的复杂性和提高效率。

#### 堆上内存与直接内存

实际上，ByteBuffer分为两种类型，一种是堆内存ByteBuffer，另一种是直接内存ByteBuffer。它们分别可以通过以下的方法获得:

```java
ByteBuffer.allocate(capacity);  //堆内存
ByteBuffer.allocateDirect(capacity);  //直接内存
```

那么直接内存和堆内存有什么区别呢？

* 堆内存是指Java虚拟机中的常规堆内存，ByteBuffer实例存储在其中。当ByteBuffer实例创建时，它会在Java堆上分配一段连续的内存空间。由于Java堆是由Java虚拟机管理的，因此在进行内存分配和回收时，需要进行垃圾回收操作。因此，使用堆内存ByteBuffer会产生额外的垃圾回收操作，并且在进行大量数据读写时，由于Java堆的分配和回收机制，可能会影响性能。
* 直接内存是指在操作系统的本地内存中分配的内存，与Java虚拟机的堆内存相互独立。因此，使用直接内存的ByteBuffer不需要进行Java垃圾回收操作，这样可以避免在进行大量数据读写时由于垃圾回收导致的性能下降。此外，由于直接内存分配在操作系统本地内存中，因此可以通过一些特殊的方式在Java程序和本地程序之间进行高效的数据传输。当然，由于直接内存不直接受java虚拟机管控，它分配更加耗时一些，并且需要我们手动进行内存释放，否则会有内存泄漏的风险。

### Channel

Channel是与数据源和目标的连接，可以用来读取和写入数据。它类似于IO中的流，但有一些不同之处。在IO中，流是单向的，而在NIO中，Channel可以同时用于读和写。此外，Channel可以更好地支持异步操作。

Channel有许多不同的实现，如FileChannel，SocketChannel和ServerSocketChannel等。每种类型的Channel都支持不同的操作。例如，FileChannel只能从文件读取数据，而SocketChannel则支持网络连接。

Channel通常会与Buffer一起使用，以便读取或写入数据。在使用Channel时，需要创建一个缓冲区，然后将其连接到Channel，然后可以开始读取或写入数据

这里我们为了方便演示，就以FileChannel来进行简单的展示。

这里主要是给大家感受一下channel是与buffer配套读写，和复习一下buffer。api的相关使用可以自己查阅资料

```java
package com.example;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;


public class FileChannelDemo {
    public static void main(String[] args) throws IOException {

        FileChannel from = new FileOutputStream("hello.txt").getChannel();
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode("hello");//获取一个ByteBuffer
        from.write(byteBuffer);


        FileChannel to = new FileInputStream("hello.txt").getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(16);
        to.read(buffer);
        buffer.flip();//切换至读模式

        while (buffer.hasRemaining()) {//当还有未读的元素
            System.out.print((char) buffer.get());
        }

        from.close();
        to.close();
    }
}

```









## EventLoop&EventLoopGroup

`EventLoop`是实现异步事件处理的关键组件。每个`Channel`都会被分配到一个`EventLoop`上，而一个`EventLoop`可以处理多个`Channel`，这使得Netty可以在一个线程中处理多个并发连接。

`EventLoop`主要有两个任务：

1. 监听注册在其上的`Channel`的IO事件，如读取、写入等，并调度相关的事件处理器来处理这些事件。
2. 定时执行定时任务，如心跳、超时检测等。

每个`EventLoop`内都封装好了Selector来监听并处理事件。

而`EventLoopGroup`就是负责管理多个`EventLoop`的容器，当有一个channel进来时，`EventLoopGroup`就会选择一个`EventLoop`将其绑定，日后这个channel有IO事件发生后，就由这个`EventLoop`负责IO事件。

有了这两个东西，我们就不需要过多关心于多线程处理的细节了。

## ChannelHandler

`ChannelHandler`则是负责进行处理Channel上的各种事件。

其主要有两种类型：

1. InboundHandler：处理入站数据，也就是从远程端口传输到本地端口的数据。它可以处理的事件包括 channelRegistered、channelActive、channelRead、channelReadComplete 等。
2. OutboundHandler：处理出站数据，也就是从本地端口传输到远程端口的数据。它可以处理的事件包括 write、flush 等。



## ChannelPipeline

而`ChannelPipeline`则是"一串"`ChannelHandler`。我们可以认为它是一个双向链表，上面的一个节点就是一个`ChannelHandler`

当一个事件被传递给`ChannelPipeline`时，它会从链表头开始遍历，直到找到合适的`ChannelHandler`处理该事件。

其结构可以大致抽象为下图:

![](https://raw.githubusercontent.com/GNK48-Ava/imgs/main/lesson8/K(EPGB%5D1AP)UZF_NFYCYBJN.png)

## Channel

channel 的主要方法

* close() 可以用来关闭 channel
* closeFuture() 用来处理 channel 的关闭
  * sync 方法作用是同步等待 channel 关闭
  * 而 addListener 方法是异步等待 channel 关闭
* pipeline() 方法添加处理器
* write() 方法将数据写入
* writeAndFlush() 方法将数据写入并刷出

### ChannelFuture

`ChannelFuture` 是一个异步操作的结果，它代表了一个特定的 I/O 操作的状态和结果。每当一个 I/O 操作被调用时，它会返回一个 `ChannelFuture` 对象，以便在未来的某个时间点通知该操作的状态和结果。可以将 `ChannelFuture` 看作是 I/O 操作的未来的结果。

## ByteBuf

`ByteBuf`的结构如下:

可以看到相对于`ByteBuffer`，netty的`ByteBuf`做出了较大的优化，可扩容，且使用起来应该是更加简单的了。

![](https://raw.githubusercontent.com/GNK48-Ava/imgs/main/lesson8/W%4017TUEU3N%40D%7EZG7FHGKF9.png)

同样的ByteBuf也是分堆内存和直接内存的版本的，这两个内存的区别我们之前已经讲过了，这里就不赘述了。

```java
ByteBuf heapBuffer = ByteBufAllocator.DEFAULT.heapBuffer(10);  //分配堆内存
ByteBuf directBuffer = ByteBufAllocator.DEFAULT.directBuffer(10);  //分配直接内存
```



### 直接内存ByteBuf的释放

Netty中通过引用计数来管理 ByteBuf 的释放。每个 ByteBuf 都会有一个整数类型的引用计数器，初始值为 1。当调用 retain() 方法时，引用计数器将增加 1；当调用 release() 方法时，引用计数器将减少 1。只有当引用计数器的值变为 0 时，才能将 ByteBuf 释放。

通过使用引用计数，Netty 可以避免在多个线程之间共享同一个 ByteBuf 时出现的问题。例如，当一个线程正在读取一个 ByteBuf，而另一个线程正在处理同一个 ByteBuf 时，如果一个线程在处理完毕后释放了 ByteBuf，那么另一个线程在读取该 ByteBuf 时将会遇到问题。

在传递一个 `ByteBuf` 对象给其他对象使用时，应该调用 `retain()` 方法，以增加引用计数，表示该 `ByteBuf` 对象被多个对象使用。

在使用完毕一个 `ByteBuf` 对象后，应该调用 `release()` 方法，以减少引用计数。当引用计数减少至 0 时，该 `ByteBuf` 对象就可以被释放。

再大概总结一下，那就是：

1. 谁使用，谁`retain()`;
2. 谁`retain()`,谁`release() `;
3. 谁是最后使用者，谁负责 `release()`

### 切片

在Netty中，可以通过`slice()`方法来实现ByteBuf的切片操作。该方法会返回一个新的ByteBuf实例，该实例**共享**原始ByteBuf的底层内存，但是其读写指针和容量信息是独立的。

![](https://raw.githubusercontent.com/GNK48-Ava/imgs/main/lesson8/9UUT%7EBSHKJ75EG%252XIHD%7B5.png)

```java
ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(10);
buf.writeBytes("hello".getBytes());

// 切片
ByteBuf sliceBuf = buf.slice(1, 3);//从索引1开始，长度为3的切片

// 输出切片内容
byte[] bytes = new byte[sliceBuf.readableBytes()];
sliceBuf.readBytes(bytes);
System.out.println(new String(bytes)); // 输出 "ell"

// 修改原始ByteBuf的内容
buf.setByte(1, 'E');

// 输出切片内容
bytes = new byte[sliceBuf.readableBytes()];
sliceBuf.readBytes(bytes);
System.out.println(new String(bytes)); // 输出 "Ell"
```





## 粘包/半包

这一段自认为时间和能力不允许，可以看看下面的文章:

这两篇结合阅读，也可以看看第一篇文章的评论

[Socket粘包问题的3种解决方案](https://juejin.cn/post/6914449958494011406#heading-0)  

[Socket粘包问题终极解决方案—Netty版](https://juejin.cn/post/6917043797684584461#heading-20)









参考文章:

[(九)Java网络编程无冕之王-这回把大名鼎鼎的Netty框架一网打尽！ - 掘金 (juejin.cn)](https://juejin.cn/post/7176869085521641509#heading-31)

[彻底理解Netty，这一篇文章就够了 - 掘金 (juejin.cn)](https://juejin.cn/post/6844903703183360008#heading-17)

[万字长文带你深入理解netty，史上最强详解！](https://zhuanlan.zhihu.com/p/389034303)



# RPC

## 什么是RPC

RPC，即**远程过程调用**（英语：**R**emote **P**rocedure **C**all，**RPC**)，是一种计算机通信协议。

当我们开发一个分布式系统时，我们可能需要在不同的计算机之间进行通信和数据传输。在这种情况下，RPC（Remote Procedure Call）是一种常用的通信协议。RPC允许客户端程序调用远程服务器上的函数或过程，就像调用本地函数一样。

在RPC中，客户端和服务器之间的交互通过网络进行。当客户端调用远程过程时，它将参数传递给服务器，服务器执行相应的操作并返回结果。这个过程就像在本地执行函数一样，但是实际上是在远程计算机上执行的。

## 为什么要用RPC？

当我们的系统访问量增大、业务增多时，单一应用已经无法承受，这时我们可能需要将业务拆分成多个互不关联的应用，分别部署在各自的机器上，以划清逻辑并减小压力。此时，就需要一种高效的应用程序之间的通讯手段来完成这种需求，RPC就成为了一种有效的选择。

## 本地函数的调用

我们来看看这一段代码:

```java
    public static int getSum(int num1, int num2) {
        int sum = num1 + num2;
        return sum;
    }

    public static void main(String[] args) {
        System.out.println(getSum(1, 5));
    }
```

想想运行这一串代码时都发生了什么？

我们将其中的过程大致简化如下:

1.当调用main函数时，JVM会为其创建一个栈帧（stack frame），并压入主线程的栈顶。

2.当调用getSum(1,5)时，JVM会为其创建另一个栈帧，而的1和5就会被压入到其中的局部变量表里。

3.getSum(1,5)计算出sum值，并将其作为返回值压入到栈顶的位置。

4.getSum执行完毕后，该方法的栈帧被销毁，返回值6弹出栈顶。

5.回到main方法中，通过System.out.println将getSum()的返回值输出到控制台。



## **远程过程调用带来的新问题**

在进行远程调用时，需要执行的函数体通常是在远程机器上的。这就引出了一些新问题：

1. **调用标识映射**。如何告诉远程机器需要调用哪个函数？在本地调用中，函数体是直接通过函数指针来指定的，因此调用正确的函数是自动完成的。但在远程调用中，两个进程的地址空间是完全不同的，因此不能使用函数指针来进行函数调用。相反，在远程过程调用(RPC)中，每个函数都必须有一个唯一的ID，以便在所有进程中都能确定该函数的标识。客户端在发起远程调用时，需要指定函数ID，并且在客户端和服务端之间维护一个函数ID与函数的对应表。这两个表不需要完全相同，但相同的函数必须对应相同的ID。客户端在发起远程调用时，需要在其本地表中查找相应的函数ID，并将其传递给服务端。服务端则通过查找其本地表，确定需要调用的函数，并执行相应函数的代码。
2. **序列化和反序列化**。客户端如何将参数传递给远程函数？在本地调用中，参数可以直接通过堆栈传递给函数。但在远程调用中，客户端和服务端是不同的进程，不能直接共享内存，因此需要通过其他方式将参数传递给函数。在某些情况下，客户端和服务端可能使用不同的编程语言(例如，服务端使用go，客户端使用Java或C++)。因此，客户端需要将参数转换为字节流，然后将其传递给服务端，服务端再将其转换为可读取的格式。这个过程称为序列化和反序列化。同样，返回值也需要序列化和反序列化。
3. **网络传输**。远程调用通常在网络上进行，因此需要使用网络连接来连接客户端和服务端。所有数据都需要通过网络进行传输，因此需要使用网络传输层。网络传输层需要将函数ID和序列化后的参数字节流传递给服务端，然后再将序列化后的调用结果传递回客户端。传输层所使用的协议可以是任何能完成这两个任务的协议。虽然大部分RPC框架都使用TCP协议，但其实UDP也是可以使用的，gRPC则选择了HTTP2。例如，我们刚刚讲的Netty框架就在网络传输层。



通过以上三个机制的运用，我们可以实现RPC的功能。就以刚刚的例子为例，大致的实现流程如下：

客户端：

1. 将需要调用的函数getSum映射为一个唯一的Call ID，比如使用字符串"getSum"
2. 将Call ID和函数参数进行序列化，比如将"getSum"、1和5序列化为二进制数据
3. 通过网络将2中序列化得到的数据包发送到服务器端
4. 等待服务器端返回结果
5. 如果服务器端调用成功，客户端将反序列化结果，并使用计算结果进行后续操作

服务器端：

1. 在本地维护一个Call ID到函数指针的映射表call_id_map，将"getSum"映射到对应的getSum函数
2. 等待客户端请求
3. 从数据包中反序列化获取调用参数，得到num1和num2的值
4. 根据Call ID在call_id_map中查找，得到相应的函数指针getSum
5. 本地调用getSum函数，计算num1和num2的和sum，将sum序列化后返回给客户端

实现一个RPC框架只需要按照以上流程实现即可。需要注意的是，以上的实现过程只是一个基本的框架，具体实现还需要考虑很多细节问题，比如网络传输协议的选择、序列化算法的优化等。



## HTTP与RPC

HTTP（Hypertext Transfer Protocol，超文本传输协议）是一种用于传输超文本数据的应用层协议，它通常用于在客户端和服务器之间传输Web页面和资源。HTTP是一个无状态的协议，客户端和服务器之间的通信都是通过请求和响应实现的。HTTP的通信模式是请求-响应模式，客户端发送一个HTTP请求，服务器响应一个HTTP响应。HTTP常用于Web应用开发和浏览器和服务器之间的通信。

RPC（Remote Procedure Call，远程过程调用）是一种进程间通信的机制，它允许程序调用另一个进程中的函数，就像调用本地函数一样。在RPC中，客户端程序发起一个远程调用请求，将请求发送到服务端程序，服务端程序执行请求的函数并将结果返回给客户端。



HTTP和RPC是两个**不同的维度**。HTTP是一种通信协议，通常用于Web浏览器和Web服务器之间的通信，而RPC是一种远程调用协议，用于在不同的计算机或进程之间进行函数调用。

但其实二者并不是泾渭分明，HTTP也可以用于RPC通信，但是RPC不受HTTP协议的约束，可以使用自定义协议进行通信。此外，RPC通信的范围从传输层到应用层都有涉及，而HTTP仅限于应用层。

而使用HTTP作为RPC的通信协议有以下的优点

1. 可扩展性：HTTP协议使用起来非常简单，同时也支持RESTful API的规范，使得API设计更加简洁易用。另外，HTTP协议也支持对数据的压缩和流式传输等功能，能够满足各种不同的业务需求。
2. 协议穿透性：HTTP协议是一种应用层协议，其请求和响应的内容是可见的，可以通过代理和防火墙等网络设备，具有很好的协议穿透性。而其他的RPC协议，如Thrift和gRPC等，则需要额外的代理或者协议转换层来支持穿透。
3. 跨语言支持：由于HTTP协议被广泛地使用，因此可以在各种语言和框架中使用。同时，很多现代编程语言都已经内置了对HTTP协议的支持，使得使用HTTP作为RPC通信的载体更加方便。
4. 安全性：由于HTTP协议可以使用TLS/SSL协议进行加密传输，因此可以保障数据的安全性。同时，HTTP也支持对请求进行认证和授权等操作，可以更好地保障数据的安全性。

乍一看HTTP已经无敌了，但其实使用它作为RPC通信协议还是具备一定缺点的。 XD

- 第一个问题是**有用信息占比少**，因为HTTP协议包含了大量的头部信息和元数据，这些信息占据了HTTP报文的一部分，导致实际传输的数据量减少。

- 第二个问题是**效率低**。因为HTTP协议是构建在TCP协议之上的，而TCP协议需要建立连接和维护状态，这些都会增加网络延迟和开销。此外，HTTP协议的报文格式也比较冗长，传输效率低。

- 第三个问题是使用HTTP协议调用远程方法比较复杂，需要封装各种参数名和参数值。虽然可以使用RESTful API来简化调用过程，但是在处理复杂的数据结构和RPC功能时，需要编写更多的代码来处理请求和响应。

  

  不过以上的大多数问题在HTTP2中都得到了一定程度的解决，比如第一个问题，http2使用了头部压缩技术来减少了头部信息的大小;而第二个问题HTTP2则通过多路复用来避免了建立多个TCP连接的开销。



所以，一般更多的是使用自定义TCP协议的RPC通信，因为使用自定义的TCP协议可以在传输数据时精简传输内容，减少无用字段，从而提高传输效率。同时，自定义TCP协议可以根据具体需求进行定制化，满足各种不同的通信场景和需求。不过自定义TCP协议也就意味着需要进行额外的开发和维护工作，对于跨语言和跨平台的通信也需要进行一定的兼容性考虑。



**结论:**HTTP的特点是比较通用，其他公司或第三方能够很轻松的通过HTTP请求获得我们的服务。而在公司内部，还是使用自定义的TCP协议进行服务调用较多。



## RPC框架

### Dubbo

Dubbo是一款高性能、轻量级的开源分布式服务框架，由阿里巴巴公司开发并贡献到Apache基金会。Dubbo支持多种协议和传输方式，例如TCP、HTTP、Dubbo协议等.

![](https://raw.githubusercontent.com/GNK48-Ava/imgs/main/lesson8/FGXCSYI%257V85%5B3RR73QN.png)

[Dubbo文档](https://cn.dubbo.apache.org/zh-cn/overview/home//)

### grpc

gRPC是一个高性能、通用的开源RPC框架，其由Google 2015年主要面向移动应用开发并基于HTTP/2协议标准而设计，基于ProtoBuf序列化协议开发，且支持众多开发语言。



<img src="https://raw.githubusercontent.com/GNK48-Ava/imgs/main/lesson8/grpc.png" width="300px" height="300px"/>

[gRPC 官网](https://grpc.io/docs/languages/java/quickstart/)

