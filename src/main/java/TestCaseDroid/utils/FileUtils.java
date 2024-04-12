package TestCaseDroid.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class FileUtils {
    public static String removeIllegalCharacters(String input) {
        // 定义 Windows 不允许的字符的正则表达式
        String illegalCharsRegex = "[<>:\"/\\|?*]";

        // 使用正则表达式替换方法移除不允许的字符
        return input.replaceAll(illegalCharsRegex, "");
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

    public static String classPathParser(String ...classPath){
        if (classPath.length>1){
            log.error("Only one class path is allowed");
            throw new RuntimeException("Only one class path is allowed");
        }else if (classPath.length==1){
            String path = classPath[0];
            if (isAbsolutePath(path)){
                return path;
            }else {
                return getAbsolutePath(path);
            }
        }
        return null;
    }

    //相对路径转绝对路径
    public static String getAbsolutePath(String relativePath){
        return System.getProperty("user.dir") + File.separator + relativePath;
    }

    public static boolean isAbsolutePath(String pathStr) {
        Path path = Paths.get(pathStr);
        return path.isAbsolute();
    }

    public static boolean isPathExist(String path){
        File file = new File(path);
        if (file.exists()){
            return true;
        }else {
            log.error("File or directory does not exist: {}", path);
            return false;
        }
    }
}
