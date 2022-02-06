import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.*;

/**
 * @author chris
 */
public class TestClass {
    private static final Logger LOG = Logger.getLogger(TestClass.class);
    private static final int LOOP = 10_000;

    public static void main(String[] args) {
        int i = 0;
        long start = System.currentTimeMillis();
        for (int j = 0; j < LOOP; j++) {
            for (int k = LOOP - 1; k > j; k--) {
                i++;
                //LOG.info(j + " -- " + k);
            }
        }
        long end = System.currentTimeMillis();
        LOG.info("Loop " + i + " spend time : " + (end - start));
    }


    /**
     * 遍历移除List元素
     */
    @Test
    public void listRemove() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add(Integer.toString(i));
        }

        /*阿里巴巴Java开发手册中原话:14.【强制】不要在 foreach 循环里进行元素的 remove/add 操作。
        remove 元素请使用 Iterator方式，如果并发操作，需要对 Iterator 对象加锁。
        正例：*/
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String item = iterator.next();
            if (item.contains("1")) {
                iterator.remove();
            }
        }

        // 遍历
        list.forEach(LOG::info);
    }

    @Test
    public void streamTest() {
        System.out.println(1 << 14);
    }

    @Test
    public void renameTest() {
        File srcFile = new File("D:\\picture\\others\\IMG_20200420_151123.jpg");
        File targetFile = new File("D:\\picture\\temp", srcFile.getName());
        boolean rename = srcFile.renameTo(targetFile);
        System.out.println(rename);
    }

    @Test
    public void listFileTest() {
        System.out.println("File.listFiles()");
        // File.listFiles()不会遍历子目录
        File[] fileArray = (new File("D:/picture/Fun/run")).listFiles();
        Arrays.stream(fileArray).forEach(System.out::println);

        System.out.println("FileUtils.listFiles()");
        List<File> files = (List<File>) FileUtils.listFiles(new File("D:/picture/Fun/run"), null, true);
        files.forEach(System.out::println);
    }

    @Test
    public void sortTest() {
        /*synchronized (sameBeanList) {
            //根据属性给对象排序
            Collections.sort(sameBeanList, (u1, u2) -> u2.getLength() - u1.getLength());

            // LOG.info(Thread.currentThread().getName() + "Here are " + Start.sameBeanList.size() + " same files in main");

            for (SameBean sameBean : sameBeanList) {
                LOG.info("Here are same files,file size is \r\n" +
                        (sameBean.getLength() * 1000L / 1048576L) + " KB\r\n" +
                        sameBean.getName1() + "\r\n" +
                        sameBean.getName2() + "\r\n");
            }
        }*/
    }


}
