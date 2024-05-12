#!/bin/bash

# 检查参数数量是否有效
if [ -z "$1" ] || [ -z "$2" ] || [ -z "$3" ]; then
    echo "请输入补丁版本的项目路径、漏洞版本的项目路径和测试类名"
    echo "示例: /home/user/PatchProject/src/test/java /home/user/VulnProject/src/test/java com.example.TestClass"
    exit 1
fi
PATCH_PROJECT_PATH=$1
VULN_PROJECT_PATH=$2
TEST_CLASS_NAME=$3

# 可选的测试用例参数
TEST_CASE_ARGS=${*:4}

printf "补丁版本的项目路径: %s\n" "$PATCH_PROJECT_PATH"
printf "漏洞版本的项目路径: %s\n" "$VULN_PROJECT_PATH"
printf "测试类名: %s\n" "$TEST_CLASS_NAME"

# 检查项目路径是否有效
if [ ! -d "$PATCH_PROJECT_PATH" ] || [ ! -d "$VULN_PROJECT_PATH" ]; then
    echo "无效的项目路径"
    exit 1
fi

# 检查 Maven 是否已安装
if ! command -v mvn &> /dev/null; then
    echo "Maven 未安装"
    exit 1
fi

# 复制测试用例到漏洞版本的项目
cp -r "$PATCH_PROJECT_PATH/$TEST_CLASS_NAME.java" "$VULN_PROJECT_PATH/$TEST_CLASS_NAME.java"

# 使用 Maven 运行补丁版本的测试用例
echo "运行补丁版本的测试用例..."
PATCH_OUTPUT=$(mvn -f "$PATCH_PROJECT_PATH" test -Dtest="$TEST_CLASS_NAME" -Dexec.args="$TEST_CASE_ARGS")

# 使用 Maven 运行漏洞版本的测试用例
echo "运行漏洞版本的测试用例..."
VULN_OUTPUT=$(mvn -f "$VULN_PROJECT_PATH" test -Dtest="$TEST_CLASS_NAME" -Dexec.args="$TEST_CASE_ARGS")

# 检查输出是否为空
if [ -z "$PATCH_OUTPUT" ] || [ -z "$VULN_OUTPUT" ]; then
    echo "测试用例未找到"
    exit 1
fi

# 比较两个版本的测试结果
echo "补丁版本的测试结果:"
echo "$PATCH_OUTPUT"
echo "漏洞版本的测试结果:"
echo "$VULN_OUTPUT"