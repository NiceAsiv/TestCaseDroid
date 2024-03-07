# Soot

适合参考的文档和教程如下：

[北京大学软件分析技术](https://xiongyingfei.github.io/SA/2022/main.htm)

[南京大学软件分析](https://ranger-nju.gitbook.io/static-program-analysis-book/)

[Tutorials for soot](https://github.com/soot-oss/soot/wiki/Tutorials)

[McGill University](https://www.sable.mcgill.ca/soot/tutorial/index.html)

[198:515 (vt.edu)](https://people.cs.vt.edu/ryder/515/f05/lectures/)

比较好的笔记资料：

[南京大学《软件分析》课程笔记](https://ling-yuchen.github.io/2022/07/14/NJU-Static-Analysis/)

比较好的入门作业或者案例：

[CSCE710  Assignment 1](https://o2lab.github.io/710/p/a1.html)

## 简介

[Soot](https://github.com/soot-oss/soot)是McGill大学的Sable研究小组自1996年开始开发的Java字节码分析工具，它提供了多种字节码分析和变换功能，通过它可以进行过程内和过程间的分析优化，以及程序流图的生成，还能通过图形化的方式输出，让用户对程序有个直观的了解。尤其是做单元测试的时候，可以很方便的通过这个生成控制流图然后进行测试用例的覆盖，显著提高效率。

**Soot是java优化框架，提供4种中间代码来分析和转换字节码。**

soot一共支持4种IR（imtermediate representation），分别是:

- Baf：精简的字节码(bytecode)表示，操作简单，主要用来插桩类

```assembly
iload 1  // load variable x1, and push it on the stack
iload 2  // load variable x2, and push it on the stack
iadd     // pop two values, and push their sum on the stack
istore 1 // pop a value from the stack, and store it in variable x1
```

- Jimple：适用于优化的3-address中间表示,可以用来做各种优化需要的分析，比如类型推测(虚调用优化）、边界检查消除、常量分析以及传播、公共子串分析等等

```assembly
stack1 = x1 // iload 1
stack2 = x2 // iload 2
stack1 = stack1 + stack2 // iadd
x1 = stack1 // istore 1
```

- Shimple：Jimple的SSA(Static Single Assignment)变体,每个“变量”只被赋值一次，而且用前会定义，这可以用来做各种reaching分析，比如一个“变量”作用域，进而分析例如内联inline时需要进行的检查等等
- Grimple：Jimple的聚合版本，不再要求表达式树线性排布（也就是按照三地址码一条一条写下来），因此减少了一些中间变量，同时也引入了`new`这个operator，适用于反编译和代码检查。



> 为什么要使用中间表示？
>
> 如果直接使用Java bytecode
>
> - 😨 太贴近机器器码（为执⾏而设计）
> - 😭 语句句类型大约有200种（⾄至多有256条指令）
> - 😫 基于栈的代码



**Soot提供的输入输出格式**

输入格式：

- java（bytecode and source code up to Java 7）
- android字节码
- Jimple中间表示
- Jasmin，低级中间表示
- soot提供的分析功能

输出格式：

- java 字节码
- android字节码
- Jimple
- Jasmin



**Soot提供的分析功能**

- 调用图构建
- 指针分析
- Def/use chains
- 模板驱动的程序内数据流分析
- 模板驱动的程序间数据流分析，与heros结合
- 别名可以使用基于流、字段和上下文敏感的需求驱动指针分析Boomerang解析
- 结合FlowDroid或IDEal的污染分析