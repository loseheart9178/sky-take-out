package com.sky.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayInputStream;
import java.util.UUID;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     * 文件上传
     *
     * @param bytes 文件字节数组
     * @param objectName 文件路径
     * @return 文件访问路径
     */
    public String upload(byte[] bytes, String objectName) {
        String extension = objectName.substring(objectName.lastIndexOf("."));
        String uploadObjectName = UUID.randomUUID().toString() + extension;

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 创建PutObject请求。
            ossClient.putObject(bucketName, uploadObjectName, new ByteArrayInputStream(bytes));
        } catch (OSSException oe) {
            log.error("OSS上传失败，ErrorCode: {}, RequestId: {}, HostId: {}",
                    oe.getErrorCode(), oe.getRequestId(), oe.getHostId(), oe);
            throw oe;
        } catch (ClientException ce) {
            log.error("OSS客户端异常", ce);
            throw ce;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        //文件访问路径规则 https://BucketName.Endpoint/ObjectName
        String[] parts = endpoint.split("//");
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(parts[0])
                .append("//")
                .append(bucketName)
                .append(".")
                .append(parts[1])
                .append("/")
                .append(uploadObjectName);

        String fileUrl = urlBuilder.toString();
        log.info("文件上传成功，访问路径: {}", fileUrl);

        return fileUrl;

    }
}
