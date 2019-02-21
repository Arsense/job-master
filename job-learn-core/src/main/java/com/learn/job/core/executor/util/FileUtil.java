package com.learn.job.core.executor.util;

import java.io.File;

/**
 * @author tangwei
 * @date 2019/2/17 17:26
 */
public class FileUtil {

    public static boolean deleteKeeply(File root) {
        if (root != null && root.exists()) {
            if (root.isDirectory()) {
                File[] children = root.listFiles();
                if (children != null) {
                    for (File child : children) {
                        deleteKeeply(child);
                    }
                }
            }
            return root.delete();
        }
        return false;
    }
}
