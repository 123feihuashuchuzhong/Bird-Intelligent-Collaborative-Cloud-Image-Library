package com.tk.yupicturebackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.tk.yupicturebackend.config.CosClientConfig;
import com.tk.yupicturebackend.exception.BusinessException;
import com.tk.yupicturebackend.exception.ErrorCode;
import com.tk.yupicturebackend.exception.ThrowUtils;
import com.tk.yupicturebackend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 文件管理服务类，负责处理文件上传、校验和临时文件删除等操作
 */
@Service
@Slf4j
public class FileManager {

    // COS客户端配置
    @Resource
    private CosClientConfig cosClientConfig;

    // COS管理服务
    @Resource
    private CosManager cosManager;

    /**
     * 上传图片到COS对象存储
     * @param multipartFile 上传的图片文件
     * @param uploadPathPrefix 上传路径前缀
     * @return UploadPictureResult 上传结果对象
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        // 校验图片格式和大小
        validPicture(multipartFile);

        // 生成唯一文件名：日期_uuid.扩展名
// 生成一个16位的随机字符串作为唯一标识
        String uuid = RandomUtil.randomString(16);
// 获取上传文件的原始文件名
        String originFilename = multipartFile.getOriginalFilename();
// 构造新的上传文件名，格式为：日期_uuid.扩展名
        String uploadFilename = String.format("%s_%s.%s",
                DateUtil.formatDate(new Date()), // 当前日期
                uuid, // 随机唯一标识
                FileUtil.getSuffix(originFilename) // 原始文件扩展名
        );

        // 构造完整上传路径，格式为：/前缀/文件名
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        File file = null;

        try {
            // 创建临时文件
            file = File.createTempFile(uploadPath, null);
            // 将上传文件内容写入临时文件
            multipartFile.transferTo(file);

            // 上传到COS对象存储：调用COS管理器的图片上传方法
            PutObjectResult putObjectResult = cosManager.putPictureObject(
                    uploadPath, // 文件在COS中的存储路径
                    file        // 要上传的本地临时文件
            );
            // 获取图片元信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();

            // 构造返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            // 计算图片宽高比
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();

            // 设置返回结果属性
            uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);

            return uploadPictureResult;
        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 确保删除临时文件
            this.deleteTempFile(file);
        }
    }

    /**
     * 校验图片文件
     * @param multipartFile 待校验的文件
     * @throws BusinessException 如果文件不符合要求
     */
    public void validPicture(MultipartFile multipartFile) {
        // 检查文件是否为空
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");

        // 1. 校验文件大小(不超过2MB)
        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024L;
        ThrowUtils.throwIf(fileSize > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");

        // 2. 校验文件后缀(只允许jpeg,jpg,png,webp)
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "jpg", "png", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
    }

    /**
     * 删除临时文件
     * @param file 要删除的文件
     */
    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        // 尝试删除文件，失败时记录错误日志
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }
    }
}
