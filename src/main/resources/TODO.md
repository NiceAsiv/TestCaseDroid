# TODO  List

## TODO
- [ ] 美化生成的图，已完成cfg美化
- [ ] 优化各种图的输入函数，加入函数签名，防止重名或者重载函数的冲突
- [ ] 增加对未知源方法漏洞路径的处理，并验证效果
- [ ] 保存分析结果到文件dot和png
- [ ] 增加测试用例执行脚本(指定两个项目的位置,然后复制所需的测试用例到两个路径，并运行两个测试用例，输出比较结果)

## Completed
- [x] 修改函数入口方式，更好适应jar包调用
- [x] 提取类信息(src/main/java/TestCaseDroid/analysis/ClassInfoExtractor.java)
- [x] 修复CFG和ICFG的遍历方式
- [x] icfg cg漏洞路径寻找 getAllApplicationClasses 50%完成,下一步解决多路径和未知源方法问题
- [x] 修复测试用例自动化执行脚本 依赖和参数问题
- [x] A->B->E A->C A->D

## Failed
- [ ] neo4j集成(暂时不处理)
- [ ] 修复callgraph多层调用(soot 相关)(测试用例见`test/MultilevelCall/LibraryApplication.java`)无法解决相关
  问题