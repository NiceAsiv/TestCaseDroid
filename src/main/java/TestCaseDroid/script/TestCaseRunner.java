package TestCaseDroid.script;


import lombok.Setter;

import java.util.List;

/**
 * 调用maven test命令执行指定的测试用例
 * 参数：测试用例的类名
 *  mvn test -Dtest=AccountImageServiceImplTest
 *  并从测试结果中提取测试用例的执行结果
 */
@Setter
public class TestCaseRunner {
    private  String projectPath;
    private  String testClassName;

    TestCaseRunner(String projectPath, String testClassName) {
        this.projectPath = projectPath;
        this.testClassName = testClassName;
    }








}
