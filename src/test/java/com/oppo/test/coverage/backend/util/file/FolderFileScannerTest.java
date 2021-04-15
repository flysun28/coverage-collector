package com.oppo.test.coverage.backend.util.file;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author 80264236
 * @date 2021/4/7 20:11
 */
@RunWith(SpringRunner.class)
@SpringBootTest
class FolderFileScannerTest {

    @Test
    void delAllFile() {
        FolderFileScanner.delAllFile("F:\\业务场景\\play\\_1");
    }
}