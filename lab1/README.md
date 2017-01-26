#
Lab 1: Remote Method Invocation \(RMI\)

## TODO

* ~~socket programming~~
* java reflection
* serialization
* proxy
* ~~multithreads~~
* ~~RMI source code~~

Skeleton:

1. it listens for incoming connections, accepts them, parses method call requests, and calls the correct methods on the server object.
2. When a method returns, the return value \(or exception\) is sent over the network to the client, and the skeleton closes the connection.

Stub:

1. marshal parameters

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

![workflow](http://csis.pace.edu/~marchese/CS865/Lectures/Liu7/LIU7_files/image022.jpg)

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

* Sockets provide an interface for programming networks at the transport layer.
* Network communication using Sockets is very much similar to performing file I/O.
* Socket-based communication is independent of a programming language used for implementing it.
* After connection, the server needs a new socket so that it can continue to listen to the original socket for connection requests while serving the connected client.
* ServerSocket : listen for client requests

The steps for creating a simple server program are:

1. Open the Server Socket:
```
ServerSocket server = new ServerSocket( PORT );
```
2. Wait for the Client Request:
```
Socket client = server.accept();
```
3. Create I/O streams for communicating to the client:
```
DataInputStream is = new DataInputStream(client.getInputStream());
DataOutputStream os = new DataOutputStream(client.getOutputStream());
```
4. Perform communication with client
```
Receive from client: String line = is.readLine();
Send to client: os.writeBytes(“Hello\n”);
```
5. Close socket:
```
client.close();
```

The steps for creating a simple client program are:

1. Create a Socket Object:
```
Socket client = new Socket(server, port_id);
```
2. Create I/O streams for communicating with the server.
```
is = new DataInputStream(client.getInputStream());
os = new DataOutputStream(client.getOutputStream());
```
3. Perform I/O or communication with the server:
```
Receive data from the server: String line = is.readLine();
Send data to the server: os.writeBytes(“Hello\n”);
```
4. Close the socket when done:
```
client.close();
```

---





[RMI System Overview](https://docs.oracle.com/javase/7/docs/platform/rmi/spec/rmi-arch.html)

When a stub's method is invoked, it does the following:

* initiates a connection with the remote JVM containing the remote object,
* marshals \(writes and transmits\) the parameters to the remote JVM,
* waits for the result of the method invocation,
* unmarshals \(reads\) the return value or exception returned, and
* returns the value to the caller.

The stub hides the serialization of parameters and the network-level communication in order to present a simple invocation mechanism to the caller.

Each remote object may have a corresponding skeleton. The skeleton is responsible for dispatching the call to the actual remote object implementation. When a skeleton receives an incoming method invocation it does the following:

* unmarshals \(reads\) the parameters for the remote method,
* invokes the method on the actual remote object implementation, and
* marshals \(writes and transmits\) the result \(return value or exception\) to the caller.

Garbage Collection of Remote Objects:

* it is desirable to automatically delete those remote objects that are no longer referenced by any client.

Dynamic Class Loading:

* When parameters and return values for a remote method invocation are unmarshalled to become live objects in the receiving JVM, class definitions are required for all of the types of objects in the stream. The unmarshalling process first attempts to resolve classes by name in its local class loading context \(the context class loader of the current thread\). RMI also provides a facility for dynamically loading the class definitions for the actual types of objects passed as parameters and return values for remote method invocations from network locations specified by the transmitting endpoint. This includes the dynamic downloading of remote stub classes corresponding to particular remote object implementation classes \(and used to contain remote references\) as well as any other type that is passed by value in RMI calls, such as the subclass of a declared parameter type, that is not already available in the class loading context of the unmarshalling side.

---

[Java - Multithreading](https://www.tutorialspoint.com/java/java_multithreading.htm)

Create a Thread by Implementing a Runnable Interface:
1. implement a run\(\) method provided by a Runnable interface.
2. instantiate a Thread object
3. call start\(\) method, which executes a call to run\( \) method.

Create a Thread by Extending a Thread Class:
1. override run\( \) method available in Thread class.
2. Once Thread object is created, you can start it by calling start\(\) method, which executes a call to run\( \) method.

Java - Thread Synchronization:

* Each object in Java is associated with a monitor, which a thread can lock or unlock. Only one thread at a time may hold a lock on a monitor.

---

[Concurrency](http://docs.oracle.com/javase/tutorial/essential/concurrency/index.html)

* Thread.sleep causes the current thread to suspend execution for a specified period.
* The join method allows one thread to wait for the completion of another.

Synchronization:

* Synchronized Methods:
* When one thread is executing a synchronized method for an object, all other threads that invoke synchronized methods for the same object block \(suspend execution\) until the first thread is done with the object.
* when a synchronized method exits, it automatically establishes a happens-before relationship with any subsequent invocation of a synchronized method for the same object.
* Intrinsic Locks and Synchronization:
* Every object has an intrinsic lock associated with it.

---

[Multithreaded Servers in Java](http://tutorials.jenkov.com/java-multithreaded-servers/index.htmls)

---

[Java Multithreading](https://www.udemy.com/java-multithreading/learn/v4/content)

---

[Trail: The Reflection API](http://docs.oracle.com/javase/tutorial/reflect/index.html)

* Reflection is used to examine or modify the runtime behavior of applications running in the JVM.

#### Classes

* Every object is either a reference or primitive type.
* For every type of object, the Java virtual machine instantiates an immutable instance of **java.lang.Class** which provides methods to examine the runtime properties of the object including its members and type information.
* **Class** also provides the ability to create new classes and objects. Most importantly, it is the entry point for all of the Reflection APIs.
* how to retrieve Class objects?
* Object.getClass()
* e.g., `Class c = "foo".getClass();`
* this only works for reference types which all inherit from Object.
* The .class Syntax
* when the type is available but there is no instance
* e.g., `Class c = boolean.class;`
* This is also the easiest way to obtain the Class for a primitive type.
* Class.forName()
* when the fully-qualified name of a class is available
* e.g., `Class c = Class.forName("com.duke.MyLocaleServiceProvider");`
* for primitive types, use : Class.getName()
* TYPE Field for Primitive Type Wrappers
* e.g., `Class c = Double.TYPE;`
* Methods that Return Classes
* Examining Class Modifiers and Types
* Discovering Class Members
* get*s()


---

[Java - Serialization](https://www.tutorialspoint.com/java/java_serialization.htm)

* JVM independent
* Classes **ObjectInputStream** and **ObjectOutputStream** are high-level streams that contain the methods for serializing and deserializing an object.
* serializes an Object and sends it to the output stream:
```
public final void writeObject(Object x) throws IOException
```
* retrieves the next Object out of the stream and deserializes it:
```
public final Object readObject() throws IOException, ClassNotFoundException
//The return value is Object, so you will need to cast it to its appropriate data type.
```
* for a class to be serialized successfully, two conditions must be met
* The class must implement the java.io.Serializable interface.
* All of the fields in the class must be serializable. If a field is not serializable, it must be marked **transient**.
* If the class implements java.io.Serializable, then it is serializable; otherwise, it's not.










---

[自己实现RMI（一）基本原理](http://blog.csdn.net/semillon/article/details/7916372)

三个关键的技术要点：
1. 对象的查找或者索引，以及回调方法。
2. 对象的序列化与反序列化。
3. 底层socket通信

* 实现对象的索引: 利用Java中的map数据结构

---

[Java RMI（远程方法调用）开发](http://www.cnblogs.com/lvyahui/p/5425507.html)

* 将可以远程调用的对象进行序列化，然后绑定到RMI Server（被调方，运行者）中作为存根（stub）
* RMI Client 会先去下载stub反序列化然后发起client调用，RMI 底层（RMI Interface Layer & Transport Layer）会讲请求参数封装发送到RMI Server
* RMI Server 接收到封装的参数，传递给桩（skeleton），由桩解析参数并且以参数调用对应的存根（）stub方法。
* 存根方法在RMI Server执行完毕之后，返回结果将被RMI底层封装并传输给RMI Client（也就是主调方，调用者）

---

[RMI的实现](http://computerdragon.blog.51cto.com/6235984/1178053)

* 客户端有客户辅助对象\(stub\)，服务端游服务辅助对象\(skeleton\)
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

---

[RMI Source Code](http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/7-b147/java/rmi/RemoteException.java?av=f)

---


