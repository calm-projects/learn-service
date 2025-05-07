package com.calm.data;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.Path;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        @Override
        public Path[] gets() {
            Path path = get();
            Path[] inputPaths = new Path[3];
            return Stream.of("/word.txt", "/word02.txt", "/word03.txt")
                    .map(item -> new Path(path + item))
                    .collect(Collectors.toList()).toArray(inputPaths);
        }
    },

    FLOW {
        @Override
        public Path get() {
            return path("src/main/java/com/calm/data/flow.txt");
        }
    },

    LOG {
        @Override
        public Path get() {
            return path("src/main/java/com/calm/data/log.txt");
        }
    },

    JOIN {
        @Override
        public Path get() {
            return path("src/main/java/com/calm/data/join");
        }

        @Override
        public Path[] gets() {
            Path path = get();
            Path[] inputPaths = new Path[2];
            return Stream.of("/order.txt", "/pd.txt")
                    .map(item -> new Path(path + item))
                    .collect(Collectors.toList()).toArray(inputPaths);
        }
    },

    OUTPUT {
        @Override
        public Path get() throws IOException {
            Path path = path("src/main/java/com/calm/data/output/");
            FileUtils.deleteDirectory(new File(path.toString()));
            return path;
        }

        @Override
        public Path get(String subPath) throws IOException {
            Path path = path("src/main/java/com/calm/data/output/" + subPath);
            FileUtils.deleteDirectory(new File(path.toString()));
            return path;
        }
    };

    private static final String root = Objects.requireNonNull(Paths.class.getResource("")).getPath();
    private static final String dataRootDir = root.substring(0, root.indexOf("target"));

    public abstract Path get() throws IOException;

    public Path get(String subPath) throws IOException {
        return null;
    }

    public Path[] gets() throws IOException {
        return null;
    }

    Path path(String filename) {
        return new Path(dataRootDir + filename);
    }


    public static void main(String[] args) throws IOException {
        System.out.println(Arrays.toString(Paths.SMALL_FILE.gets()));
        System.out.println(Paths.OUTPUT.get());
    }

}
