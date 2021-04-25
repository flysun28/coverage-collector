package com.oppo.test.coverage.backend.util.http;

import esa.commons.io.IOUtils;
import esa.commons.netty.core.Buffer;
import esa.httpclient.core.Handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 对于内容较小的文件，可通过直接将文件内容写入请求body中或者直接从响应body中读取
 * 当文件内容过大，直接读取或者写入有OOM风险时的大文件上传和下载功能需采用此handler
 * @author 80264236
 * @date 2021/4/25 9:54
 */
public class FileHandler extends Handler {

    private static final String PATH = "xxxx";

    private RandomAccessFile file;

    @Override
    public void onStart() {
        String fileName = response().headers().get("fileName");
        try {
            file = new RandomAccessFile(new File(PATH, fileName), "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onData(Buffer content) {
        if (file != null) {
            byte[] data = new byte[content.readableBytes()];
            content.readBytes(data);
            try {
                file.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            throw new IllegalStateException("file is null");
        }
    }

    @Override
    public void onEnd() {
        IOUtils.closeQuietly(file);
    }

    @Override
    public void onError(Throwable cause) {
        IOUtils.closeQuietly(file);
    }
}
