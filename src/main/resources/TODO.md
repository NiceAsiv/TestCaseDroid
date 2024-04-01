# TODO  List

- [ ] 修复callgraph多层调用，未能深入某些类的bug(soot 相关)(测试用例见`test/MultilevelCall/LibraryApplication.java`)无法解决相关
   问题
- [x] 修改函数入口方式，更好适应jar包调用
- [x] 修复CFG和ICFG的遍历方式
- [ ] 增加漏洞路径寻找功能，方式为Worklist求解器,对路径进行逆向减枝，一定要从目标节点的逆向找，如果从入口正向找会出现指数爆炸的问题
- [x] 增加测试用例自动化执行脚本(sh py java)
- [ ] neo4j集成(暂时不处理)