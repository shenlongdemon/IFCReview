package com.sl.commonutil.zip;

/**
 * Created by shenlong on 16/10/2016.
 */

public class Constants {
    public enum COMPRESS_TYPE {
        NONE(1, "none"),
        LZSTRING(2, "lz-string"),
        GZIP(3, "gzip");
        private int value;
        private String name;
        private COMPRESS_TYPE(int value, String name) {
            this.value = value;
            this.name = name;
        }
    };
}
