import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author chris
 * @create 2022/2/2
 */
public class CheckFiles {
    private static final Logger LOG = Logger.getLogger(CheckFiles.class);
    private static final String COPY_PREFIX = "copy_";

    public static void main(String[] args) {
        // 读取配置文件，文件名称不能叫config
        ResourceBundle resource = ResourceBundle.getBundle("conf");

        String sourcePath = resource.getString("source_path");
        String targetPath = resource.getString("target_path");
        LOG.info("source path is " + sourcePath);
        LOG.info("target path is " + targetPath);

        long start = System.currentTimeMillis();

        // FileUtils.listFiles()会遍历子目录
        List<File> files = (List<File>) FileUtils.listFiles(new File(sourcePath), null, true);
        long endList = System.currentTimeMillis();
        LOG.info("List " + files.size() + " files spend time : " + (endList - start));

        /*【推荐】集合初始化时，指定集合初始值大小。
        说明：HashMap 使用 HashMap(int initialCapacity) 初始化，如果暂时无法确定集合大小，那么指定默认值（16）即可。
        正例：initialCapacity = (需要存储的元素个数 / 负载因子) + 1。注意负载因子（即 loader factor）默认为 0.75，
        如果暂时无法确定初始值大小，请设置为 16（即默认值）。
        反例： HashMap 需要放置 1024 个元素，由于没有设置容量初始大小，随着元素增加而被迫不断扩容，
        resize()方法总共会调用 8 次，反复重建哈希表和数据迁移。当放置的集合元素个数达千万级时会影响程序性能。*/
        Map<Long, File> allFiles = new HashMap<>(1 << 15);
        Map<String, File> sameFiles = new ConcurrentHashMap<>(1 << 10);

        for (File file : files) {
            if (allFiles.containsKey(file.length())) {
                // 将两个文件都放入sameFiles以便稍后下载
                sameFiles.put(file.getAbsolutePath(), file);
                sameFiles.put(allFiles.get(file.length()).getAbsolutePath(), allFiles.get(file.length()));
            } else {
                allFiles.put(file.length(), file);
            }
        }

        long endMap = System.currentTimeMillis();
        LOG.info("Map " + allFiles.size() + " files and " + sameFiles.size() + " same files spend time : " + (endMap - endList));

        int cpu = Runtime.getRuntime().availableProcessors();
        LOG.info("CPU : " + cpu);

        ExecutorService executor = Executors.newFixedThreadPool(cpu);

        for (Map.Entry<String, File> entry : sameFiles.entrySet()) {
            Runnable runnable = () -> {
                File sourceFile = entry.getValue();
                try {
                    File targetFile = new File(targetPath, sourceFile.getName());
                    if (targetFile.exists()) {
                        targetFile = new File(targetPath, COPY_PREFIX + UUID.randomUUID() + "_" + sourceFile.getName());
                        FileUtils.moveFile(sourceFile, targetFile);
                    } else {
                        File targetPathFile = new File(targetPath);
                        FileUtils.moveFileToDirectory(sourceFile, targetPathFile, true);
                    }
                } catch (IOException e) {
                    LOG.error(entry.getKey() + e.getMessage(), e);
                }
            };
            executor.execute(runnable);
        }
        executor.shutdown();
        try {
            boolean awaitTermination = executor.awaitTermination(30, TimeUnit.MINUTES);
            long endMove = System.currentTimeMillis();
            LOG.info("ExecutorService awaitTermination: " + awaitTermination + ", Move files spend time : " + (endMove - endMap));
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
