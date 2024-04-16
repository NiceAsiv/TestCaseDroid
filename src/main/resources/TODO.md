# TODO  List

## 正在完成
- [ ] 美化生成的图，已完成cfg美化 
- [x] 修复测试用例自动化执行脚本 依赖和参数问题
- [x] A->B->E A->C A->D

## 已完成
- [x] 修改函数入口方式，更好适应jar包调用
- [x] 提取类信息(src/main/java/TestCaseDroid/analysis/ClassInfoExtractor.java)
- [x] 修复CFG和ICFG的遍历方式
- [x] icfg cg漏洞路径寻找 getAllApplicationClasses 50%完成,下一步解决多路径和未知源方法问题

## 未能完成
- [ ] neo4j集成(暂时不处理)
- [ ] 修复callgraph多层调用(soot 相关)(测试用例见`test/MultilevelCall/LibraryApplication.java`)无法解决相关
  问题