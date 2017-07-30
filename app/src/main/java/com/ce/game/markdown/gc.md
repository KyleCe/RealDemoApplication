# 由 _Java GC_ 看 _Android_ 内存泄露

内存泄露，一个老生常谈的话题，本文将从Java GC的角度出发，一探Android内存泄露的究竟，希望能给读者带来些许启发。


*本篇涵盖：*

- _**准备**——基础知识要求及名字解释_
- _**背景**——Android内存泄露的本质与危害_
- _**原因**——为什么会产生泄露_
- _**基础**——Java内存分配与回收_
- _**引申**——JVM与Android虚拟机_
- _**实战**——内存泄露攻防_

## 零、准备
### 0.0 要求：
  阅读本文，需要读者具有一定的JAVA基础与Android基础
### 0.1 名词解释：
* GC——Garbage Collector垃圾收集器
* MAT——Eclipse  Memory Analyzer Tool 内存分析工具
* LeakCanary——第三方内存泄露监测工具
* StrictMode——Android严格模式，调优时可以参考
### 0.2 演示环境：
* Android Studio——3.0 Canary 8
* Eclipse MAT——V1.7.0
* LeakCanary——V1.5.1

## 一、本质与危害
### 1.1 何谓内存泄露
在计算机科学中，内存泄漏指由于疏忽或错误造成程序未能释放已经不再使用的内存。内存泄漏并非指内存在物理上的消失，而是应用程序分配某段内存后，由于设计错误，导致在释放该段内存之前就失去了对该段内存的控制，从而造成了内存的浪费。

在安卓中，内存泄露主要是指应用程序进程在运行过程中有不能释放而不再使用的内存，占用了比实际需要多的空间。

图1.1.1是使用MAT分析手机内存快照得到的OverView结果：

<div style="text-align:center" markdown="1">

![图1.1.1 CMCM某款应用的Debug版内存泄露OverView](http://upload-images.jianshu.io/upload_images/1481332-ca4c6177eb55ae69.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/800#center)

</div>



### 1.2 恶劣影响
内存泄漏会因为减少可用内存的数量从而降低计算机的性能。最终，在最糟糕的情况下，过多的可用内存被分配掉导致全部或部分设备停止正常工作，或者应用程序崩溃。在以下情況，内存泄漏导致较嚴重的后果2：
* 程序运行后置之不理，消耗越来越多的内存（比如服务器上的后台任务，尤其是嵌入式系统中的后台任务，这些任务可能被运行后很多年内都置之不理）；
* 频繁分配新内存；
* 程序能够请求未被释放的内存（比如共享内存）；
* 内存非常有限，比如在嵌入式系统或便携设备中；
* ...

针对安卓，内存泄露轻则导致应用占用内存虚高、增加CPU占用、耗电，重则导致应用程序无法开辟所需大小的内存，引发OOM，触发崩溃，这在内存小的机器上尤为明显（我们平时在测试应用内存占用表现时，可以多使用低端机）。
结合上一节所举例子，由图1.1可见该应用的泄露足有35M之多，这一内存结果还是应用刚启动时的情况，随着用户使用时间加长，泄露只会越来越多，直到用户杀死应用或者应用主动崩溃（如图1.2.1）。

<center></center>

<div style="text-align:center" markdown="1"><img src="http://upload-images.jianshu.io/upload_images/1481332-b460f38cafbd927b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/800" alt="图1.2.1 AndroidStudio 某OutOfMemory 堆栈"></div>
<center></center>
<div style="text-align: center; "><div style="display: inline-block; position: relative; top: 0px; background-color: transparent; padding: 0px 10px;">图1.2.1 AndroidStudio 某OutOfMemory 堆栈</div></div>
<center/>
<center/>
<center/>


## 二、产生缘由

内存泄露诱因有很多，安卓中比较常见的有：

* 静态变量持有引用(集合类、单例造成的内存泄漏)
* 匿名内部类/非静态内部类和异步线程
* Handler 、UI线程的post、AnimatorListener等使用不当
* 资源未关闭(或在finalize中关闭)
* 监听器的使用，在释放对象的同时没有相应删除监听器
* ...

下面针对部分诱因进行说明，具体解决办法此处按下不表。
### 2.1 静态变量导致的泄露
静态集合导致的泄露可以分析为：长生命周期的对象，持有了短生命周期对象的引用，在后者生命周期结束时未释放长周期对象对它的引用，导致对象无法被GC回收。图2.1.1为一示例，即使在循环内有设置集合对象为null，但集合中的对象还是存在，GC并不能回收它（这种在集合中不断创建新对象的写法也是极其臭名昭著的）。


<div style="text-align:center" markdown="1">

![图2.1.1  静态集合的泄露示例](http://upload-images.jianshu.io/upload_images/1481332-03be10fdf312be61.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/800)

</div>



图2.1.2为某APP静态集合泄露的对象汇总，可以看到总大小有11.7M之大。

<div style="text-align:center" markdown="1">

![图2.1.2 某静态泄露的汇总结果](http://upload-images.jianshu.io/upload_images/1481332-5a2e015b3d5de3b3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/800)
</div>



### 2.2 匿名内部类引发的内存泄露
匿名内部类极易引发内存泄露，纵使这样的写法在代码层面会简洁很多，但在涉及到匿名内部类生命周期不依附于外部类时，需要我们谨慎处理，不然就很有可能引发泄露，图2.2.1 为AppsFlayer SDK 4.7.1 Forground.java中某段代码的近似版本（该泄露在SDK v4.7.4中已修复）：

<div style="text-align:center" markdown="1">

![图2.2.1 匿名内部类引发的内存泄露示例](http://upload-images.jianshu.io/upload_images/1481332-d8c4f55edc053536.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/800)
图2.2.1 匿名内部类引发的内存泄露示例
</div>


<div style="text-align:center" markdown="1">

![图2.2.2 匿名内部类引发的内存泄露示例2](http://upload-images.jianshu.io/upload_images/1481332-32baef0d6d1e22c7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/800)
图2.2.2展示了常见的Handler写法可能引发的内存泄露（Android Studio）
</div>

图2.2.2 匿名内部类引发的内存泄露示例2


<div style="text-align:center" markdown="1">

![图2.2.3 匿名内部类引发的内存泄露示例3](http://upload-images.jianshu.io/upload_images/1481332-0270c0fdc04b6d26.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/800)
图2.2.3 匿名内部类引发的内存泄露示例3
</div>

一般在一个质量欠佳的工程中，匿名内部类或异步线程操作导致的内存泄露随处可见。

### 2.3 Handler任务管理不当
Handler、AnimationListener、AnimatorUpdateListener使用不当也极易导致泄露，图2.3.1即为有泄露隐患的示例


<div style="text-align:center" markdown="1">

![图2.3.1 Handler操作潜在泄露示例](http://upload-images.jianshu.io/upload_images/1481332-518f810be8d5eea2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/800)
图2.3.1 Handler操作潜在泄露示例
</div>

### 2.4 资源未及时关闭
Android资源不及时关闭会出现内存泄露的地方有很多，诸如在使用I/O流、Cursor（图2.4.1展示了在APP开启StrictMode时会收到的FileIO未close的异常Throwable）


<div style="text-align:center" markdown="1">

![图2.4.1 closable close未调用](http://upload-images.jianshu.io/upload_images/1481332-cbf5aa5529865f5f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/800)
图2.4.1 closable close未调用
</div>

### 2.5 绑定/解绑、注册/反注册未成对调用
绑定/解绑、注册/反注册同时出现这一点毋庸置疑，但实际工程中发现有开发者对于成对调用的理解不够透彻，会有前后条件不一致的情况，导致内存泄露（如注册时无条件注册，反注册时加入不能100%保证成立的判定条件）

## 三、Java内存分配与垃圾回收策略
### 内存分配
### 内存空间划分

* 方法区
* 堆区
* 虚拟机栈
* 程序计数器 
* 本地方法栈




### 垃圾回收




## 四、JVM与Android虚拟机的异同
### JVM
### 安卓虚拟机
### 两者异同
## 五、如何“防治”
### 攻
### 防


>
参考文献：
	1. https://zh.wikipedia.org/wiki/内存泄漏
	2. https://zh.wikipedia.org/wiki/内存泄漏