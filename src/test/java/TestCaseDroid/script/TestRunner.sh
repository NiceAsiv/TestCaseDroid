#!/bin/bash

#输入提示
echo "请输入项目路径和测试类名："
# shellcheck disable=SC2028
echo "eg:bash TestRunner.sh E:\\Tutorial\\taitest\\ test.CFGTestTest"
read -r PROJECT_PATH TEST_CLASS_NAME
#输出输入的路径和类名
echo "项目路径：$PROJECT_PATH"
echo "测试类名：$TEST_CLASS_NAME"


# Run the test case using Maven
OUTPUT=$(mvn -f "$PROJECT_PATH" test -Dtest="$TEST_CLASS_NAME")


# Parse the test results
TOTAL_TESTS=$(echo "$OUTPUT" | grep "Tests run:" | awk '{print $3}' | cut -d ',' -f1)
FAILED_TESTS=$(echo "$OUTPUT" | grep "Tests run:" | awk '{print $6}' | cut -d ',' -f1)
ERROR_TESTS=$(echo "$OUTPUT" | grep "Tests run:" | awk '{print $8}' | cut -d ',' -f1)
SKIPPED_TESTS=$(echo "$OUTPUT" | grep "Tests run:" | awk '{print $10}' | cut -d ',' -f1)

# Print the test results
echo "Total tests: $TOTAL_TESTS"
echo "Failed tests: $FAILED_TESTS"
echo "Error tests: $ERROR_TESTS"
echo "Skipped tests: $SKIPPED_TESTS"