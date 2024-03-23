package TestCaseDroid.utils;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SootDataProcessUtils {
    public static String removeIllegalCharacters(String input) {
        // 定义 Windows 不允许的字符的正则表达式
        String illegalCharsRegex = "[<>:\"/\\|?*]";

        // 使用正则表达式替换方法移除不允许的字符
        String result = input.replaceAll(illegalCharsRegex, "");
        return result;
    }

    public static void folderExistenceTest(String folderPath){
        File folder = new File(folderPath.substring(0, folderPath.lastIndexOf("/")));
        // 正则匹配，获取sootOutput/之后的部分，直到下一个/出现
        String pattern = "\\/sootOutput\\/(.*?)\\/";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(folderPath);
        String folderName = null;
        if (m.find()) {
            folderName = m.group(1);
        }
        // 如果文件夹存在，则不做处理，如果不存在则创建
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                System.out.println(String.format("Creating %s output folder：", folderName) + folder.getAbsolutePath());
            } else {
                System.err.println(String.format("Unable to create %s output folder：", folderName) + folder.getAbsolutePath());
            }
        } else {
            System.out.println(String.format("%s output folder exist in：",folderName) + folder.getAbsolutePath());
        }
    }
}
