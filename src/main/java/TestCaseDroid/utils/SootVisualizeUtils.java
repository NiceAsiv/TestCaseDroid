package TestCaseDroid.utils;

public class SootVisualizeUtils {
    public enum TextColor {
        // 定义枚举常量及其对应的 ANSI 转义序列
        RESET("\u001B[0m"),
        BLACK("\u001B[30m"),
        RED("\u001B[31m"),
        GREEN("\u001B[32m"),
        YELLOW("\u001B[33m"),
        BLUE("\u001B[34m"),
        MAGENTA("\u001B[35m"),
        CYAN("\u001B[36m"),
        WHITE("\u001B[37m");

        // ANSI 转义序列
        private final String code;

        // 构造方法
        TextColor(String code) {
            this.code = code;
        }

        // 获取 ANSI 转义序列
        public String getCode() {
            return code;
        }
    }
}
