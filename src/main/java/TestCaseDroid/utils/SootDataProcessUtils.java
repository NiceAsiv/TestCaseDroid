package TestCaseDroid.utils;

public class SootDataProcessUtils {
    public static String removeIllegalCharacters(String input) {
        // 定义 Windows 不允许的字符的正则表达式
        String illegalCharsRegex = "[<>:\"/\\|?*]";

        // 使用正则表达式替换方法移除不允许的字符
        String result = input.replaceAll(illegalCharsRegex, "");
        return result;
    }
}
