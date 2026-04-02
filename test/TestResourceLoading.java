package test;
import java.io.File;
import java.net.URL;
import java.net.URLDecoder;

public class TestResourceLoading {
    public static void ggg(){
        System.out.println("=== 详细调试信息 ===");
        
        // 1. 打印当前工作目录
        System.out.println("当前工作目录: " + new File(".").getAbsolutePath());
        
        // 2. 检查不同路径
        String[] paths = {
            "DATA/SZSC.xlsx",        // 相对路径
            "/DATA/SZSC.xlsx",       // 绝对路径
            "SZSC.xlsx",             // 直接在类路径根目录
            "./DATA/SZSC.xlsx"       // 当前目录
        };
        
        for (String path : paths) {
            testPath(path);
        }
        
        // 3. 检查类加载器的类路径
        System.out.println("\n=== 类路径信息 ===");
        String classpath = System.getProperty("java.class.path");
        System.out.println("java.class.path: " + classpath);
        
        // 4. 手动检查文件是否存在
        System.out.println("\n=== 文件系统检查 ===");
        File dataFolder = new File("DATA");
        System.out.println("DATA文件夹是否存在: " + dataFolder.exists());
        if (dataFolder.exists()) {
            System.out.println("DATA绝对路径: " + dataFolder.getAbsolutePath());
            File[] files = dataFolder.listFiles();
            if (files != null) {
                System.out.println("DATA文件夹内容:");
                for (File f : files) {
                    System.out.println("  " + f.getName() + " (" + f.length() + " bytes)");
                }
            }
        }
        
        // 5. 检查src文件夹是否包含DATA
        System.out.println("\n=== 检查src/DATA ===");
        File srcData = new File("src/DATA");
        if (srcData.exists()) {
            System.out.println("警告: Excel文件在src/DATA中，应该在项目根目录的DATA文件夹");
        }
    }
    
    private static void testPath(String path) {
        System.out.println("\n测试路径: " + path);
        
        ClassLoader cl = TestResourceLoading.class.getClassLoader();
        URL url = cl.getResource(path);
        System.out.println("  URL: " + (url != null ? url.toString() : "null"));
        
        if (url != null) {
            try {
                String decodedPath = URLDecoder.decode(url.getPath(), "UTF-8");
                System.out.println("  解码路径: " + decodedPath);
                
                File file = new File(decodedPath);
                if (file.exists()) {
                    System.out.println("  文件存在，大小: " + file.length() + " bytes");
                } else {
                    // 可能是jar内的路径
                    System.out.println("  注意: 可能是JAR内路径");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // 测试InputStream
        try {
            java.io.InputStream is = cl.getResourceAsStream(path);
            System.out.println("  InputStream: " + (is != null ? "成功获取" : "null"));
            if (is != null) is.close();
        } catch (Exception e) {
            System.out.println("  异常: " + e.getMessage());
        }
    }
}
