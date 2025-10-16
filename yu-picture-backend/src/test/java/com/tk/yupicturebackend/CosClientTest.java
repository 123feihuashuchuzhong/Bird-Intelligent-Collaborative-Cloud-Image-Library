package com.tk.yupicturebackend;

import com.qcloud.cos.COSClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // 指定使用测试配置文件
public class CosClientTest {

    @Autowired
    private COSClient cosClient;

    @Test
    public void testCosClient() {
        // 测试 COS 客户端是否正常工作
        System.out.println(cosClient);
    }
}
