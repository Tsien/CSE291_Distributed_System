# Lab 1: Remote Method Invocation \(RMI\)

## TODO
* socket programming
* java reflection()
* serialization
* multithreads
* RMI source code


## Overview

* implement a remote method invocation \(RMI\) library.

## Logistics

submit :

* a single zip archive
* include your modified Skeleton.java and Stub.java, and files containing any helper classes you have created.
* Stub, Skeleton, and all helper classes must be in the package rmi.
* Your RMI library should work on Java 7 virtual machines.

## Detailed Description

The RMI library has two major components:

* one that simplifies the task of making servers remotely-accessible
* and another that simplifies the writing of the corresponding clients.

Note that it is important not to think of the remotely-accessible object as a “server” in the low-level\(socket programming-level\) sense. As you will soon see, a low-level TCP server is implemented in, and hidden by, the RMI library

### On the Server: Skeleton

* It is a multithreaded TCP server which handles all the low-level networking tasks:
* it listens for incoming connections, accepts
them, parses method call requests, and calls the correct methods on the server object.
* When a method returns, the return value \(or exception\) is sent over the network to the client, and the skeleton closes the connection.
* Note that the server object itself need not perform any network I/O — **this is all done entirely by the skeleton, within the RMI library**. The server object does not even have to be aware of the existence of any skeletons that might invoke methods on it.
* What determines which public methods of the server object are accessible remotely?
* The server object implements a certain kind of interface called a remote interface, which will be detailed later.
* The remote interface lists the remotely-accessible methods of the server object. The skeleton object is created for this interface, and only forwards calls to the methods which are listed in it.
Keywords:
* multithreaded
* listen for incoming connections, accepts them
* parses method call request,
* call the correct methods on the server object
* send the return value(or exception) over the network
* close the connection

### On the Client: Stub

* Each one appears to implement a given remote interface. However, instead of implementing the interface in a direct manner, each stub object forwards all method calls to a server by contacting a remote skeleton.
* When the client invokes a method on a stub object, the stub opens a connection to the skeleton, and sends the method name and arguments. As described in the section on skeletons, this causes the remote
skeleton to call the same method on the server object. When the method finishes, the skeleton transmits the result, and the stub returns this result to the caller in the client virtual machine.
* As with the skeleton, the user of the stub need not explicitly perform any network I/O — again, this
is done entirely by the stub object, and implemented within the RMI library.

Keywords:

* open a connection to skeleton
* send method name and arguments
* return the results to the caller in the client


### Diagram

![RMI Diagram](http://i.imgur.com/NPw4kNm.png?1)

### Remote Interfaces

A remote interface is simply a regular interface with the additional requirement that every method be
marked as throwing a special exception, **RMIException**.

* This is because, when using RMI, a method may fail due to a network error, a protocol incompatibility, or for other reasons that are related only to RMI and have nothing to do with the functionality of the server object. These failures are, of course, signaled by throwing RMIException.

### Specification

create two classes within the package rmi: Skeleton and Stub.

Skeleton:

* implements a multithreaded TCP server.
* parametrized by the remote interface for which it accepts calls
* Each of the Skeleton constructors requires the caller to provide a reference to a server object which implements that interface.
* The skeleton object must forward all valid call requests it receives to the server object thus specified.
* The skeleton object must also stop gracefully in response to calls to stop, must be restartable, and must call the stopped, listen error, and service error methods in response to the appropriate events.

Stub:
* a class factory which generates stub objects for remote interfaces. \* The class Stub itself cannot be instantiated. To repeat, it is important to note that stub objects are not instances of the Stub class, for reasons that should become clear after reading the implementation section.
* Stubs must necessarily implement all the methods in their remote interface, and they must do this by forwarding each method call to the remote skeleton.
* Stubs should open a single connection per method call. Arguments should be forwarded as given, and results should be returned to the caller as they were returned to the skeleton from the server.
* If the remote method raises an exception, the stub must raise
the same exception, with the same fields and the same stack trace.
* The stub may additionally raise RMIException if an RMI error occurs while making the method call.
* Stubs must also additionally implement the methods equals, hashCode, and toString.
* Two stubs are considered equal if they implement the same remote interface and connect to the same skeleton.
* The equals and hashCode methods must respect this requirement. The toString method should report the name of the remote interface implemented by the stub, and the remote address \(including hostname and port\) of the skeleton to which the stub connects.
* Stubs must also be serializable.

All the constructors of Skeleton and all versions of Stub.create must reject interfaces which are not remote interfaces.

### Implementation

Skeleton must be able to call any method in any remote interface.
Stub must solve an even greater challenge: it must be capable of generating stub objects that implement any remote interface at run-time. You will need to use **Java Reflection** in order to achieve
this level of flexibility. To start, consult the documentation for the Class and Method classes. Also see **InvocationTargetException**.

The generation of stubs that implement arbitrary remote interfaces at run-time is done by creating proxy objects \(**class Proxy**\). Every proxy object has a reference to an invocation handler \(class **InvocationHandler**\), whose **invoke** method is called whenever a method is called on the proxy object.

The RMI library’s stub objects will therefore be proxy objects, and the marshalling of arguments will be done in their invocation handlers. Please refer to the documentation of these classes.

transmit arguments:

* using Java’s native serialization facility
* the classes ObjectInputStream and ObjectOutputStream.

Helper classes:

* put them in the RMI package
* make them package-private

### Testing: Local

* conformance/ subdirectory : only access the public interface
* unit/ subdirectory : test package-private classes and method
* Run make test to run all tests. For detailed documentation on how to use the test library and write your own tests, run make docs-all and read the documentation for the package test in the Javadoc generated in the javadoc-all/ subdirectory. The Test and Series classes are of particular interest. You should also look at the some of the tests distributed with the starter code as examples.

### Testing: Docker

* Write a PingPongClient and a PingPongServer.

### Notes and Tips

* If you are creating both an ObjectInputStream and an ObjectOutputStream on two sides of a connection, you must ensure that the output stream is created first, and you must flush it before creating the input stream.
* handle invalid arguments \(for example, null\),
* Note that the skeleton is multithreaded, so server objects must be thread-safe.


## Reference

Lecture notes:

---

PPT notes:

* java.net package
* stub, skeleton, serialization
* socket programming
* open/close socket
* accept
* read/write
* send/receive
![TCP Sockets](http://i.imgur.com/dhDLqSn.png)

* implement generic interfaces
* use Java reflection
* java.lang.reflect package
* proxy is useful for creating stub objecst

---

[Trail: RMI](https://docs.oracle.com/javase/tutorial/rmi/)

Overview:

* A typical server program creates some remote objects, makes references to these objects accessible, and waits for clients to invoke methods on these objects.
* A typical client program obtains a remote reference to one or more remote objects on a server and then invokes methods on them.
* TODO:
* Locate remote objects
* Communicate with remote objects
* Load class definitions for objects that are passed around
* Dynamic Code Loading : download the definition of an object's class if the class is not defined in the receiver's Java virtual machine.
* An object becomes remote by implementing a remote interface:
* A remote interface extends the interface java.rmi.Remote.
* declares java.rmi.RemoteException in its throws clause
* RMI passes a remote stub for a remote object as the remote reference.
* the remote stub implements the same set of remote interfaces that the remote object implements.

RMI server:
* Each interface contains a single method.
* two interfaces : one for remote access, another for starting work
* For an object to be considered serializable, its class must implement the java.io.Serializable marker interface.
* In general, a class that implements a remote interface should at least do the following:
* Declare the remote interfaces being implemented
* Define the constructor for each remote object
* Provide an implementation for each remote method in the remote interfaces
* The rules governing how arguments and return values are passed are as follows:
* Remote objects are essentially passed by reference. A remote object reference is a stub, which is a client-side proxy that implements the complete set of remote interfaces that the remote object implements.
* Local objects are passed by copy, using object serialization. By default, all fields are copied except fields that are marked static or transient. Default serialization behavior can be overridden on a class-by-class basis.
* The main method's first task is to create and install a security manager, which protects access to system resources from untrusted downloaded code running within the Java virtual machine.
* The static `UnicastRemoteObject.exportObject` method exports the supplied remote object so that it can receive invocations of its remote methods from remote clients.
* Before a client can invoke a method on a remote object, it must first obtain a reference to the remote object.
* The system provides a particular type of remote object, the RMI registry, for finding references to other remote objects. The RMI registry is a simple remote object naming service that enables clients to obtain a reference to a remote object by name.


Client Program:
* Like the ComputeEngine server, the client begins by installing a security manager. This step is necessary because the process of receiving the server remote object's stub could require downloading class definitions from the server.
* Note that all serializable classes, whether they implement the Serializable interface directly or indirectly, must declare a private static final field named serialVersionUID to guarantee serialization compatibility between versions.

---

[Socket Programming](http://www.buyya.com/java/Chapter13.pdf)

---

[RMI System Overview](https://docs.oracle.com/javase/7/docs/platform/rmi/spec/rmi-arch.html)

---

[自己实现RMI（一）基本原理](http://blog.csdn.net/semillon/article/details/7916372)

---
[Java RMI（远程方法调用）开发](http://www.cnblogs.com/lvyahui/p/5425507.html)

* 将可以远程调用的对象进行序列化，然后绑定到RMI Server（被调方，运行者）中作为存根（stub）
* RMI Client 会先去下载stub反序列化然后发起client调用，RMI 底层（RMI Interface Layer & Transport Layer）会讲请求参数封装发送到RMI Server
* RMI Server 接收到封装的参数，传递给桩（skeleton），由桩解析参数并且以参数调用对应的存根（）stub方法。
* 存根方法在RMI Server执行完毕之后，返回结果将被RMI底层封装并传输给RMI Client（也就是主调方，调用者）


---

[RMI的实现](http://computerdragon.blog.51cto.com/6235984/1178053)

* 客户端有客户辅助对象(stub)，服务端游服务辅助对象(skeleton)
* 制作远程服务：
1. 制作远程接口: 定义了可以供客户远程调用的方法。stub和实际服务都是实现该接口
2. 制作远程接口的实现：真正的实际工作的服务
3. 利用rmic产生stub
4. 启动RMI：registry，rmiregistry如同一个清单，客户可以从中查到代理的位置
5. 开始启动远程服务：开始启动远程服务，一般服务实现类会去实例化一个服务实例，然后将这个服务注册到RMI registry。

---

[深入剖析 Java RMI 技术实现原理](http://classfoo.com/ccby/article/p1wgbVn)

---

[RMI网络编程开发之二 如何搭建基于JDK1.5的分布式JAVA RMI 程序](http://6221123.blog.51cto.com/6211123/1112619)


---

[远程方法调用（RMI）原理与示例](http://www.cnblogs.com/wxisme/p/5296441.html)

---

[RMI（Remote Method Invocation）原理浅析](http://blog.csdn.net/qb2049_xg/article/details/3278672)


