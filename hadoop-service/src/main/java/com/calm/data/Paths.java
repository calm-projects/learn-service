package com.calm.data;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.Path;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * 家里电脑与公司电脑路径不同，懒得修改绝对路径，prod不应该使用此类，仅仅用于测试。
 */
public enum Paths {
    WORD {
        @Override
        public Path get() {
            return path("src/main/java/com/calm/data/word.txt");
        }
    },

    SMALL_FILE {
        @Override
        public Path get() {
            return path("src/main/java/com/calm/data/smallFile");
        }
    },

    FLOW {
        @Override
        public Path get() {
            return path("src/main/java/com/calm/data/flow.txt");
        }
    },

    OUTPUT {
        @Override
        public Path get() throws IOException {
            Path path = path("src/main/java/com/calm/data/output");
            FileUtils.deleteDirectory(new File(path.toString()));
            return path;
        }
    };

    public abstract Path get() throws IOException;

    Path path(String filename) {
        String path = Objects.requireNonNull(Paths.class.getResource("")).getPath();
        path = path.substring(0, path.indexOf("target")) + filename;
        return new Path(path);
    }

    public static void main(String[] args) throws IOException {
        System.out.println(Paths.WORD.get());
        System.out.println(Paths.OUTPUT.get());
    }
}
