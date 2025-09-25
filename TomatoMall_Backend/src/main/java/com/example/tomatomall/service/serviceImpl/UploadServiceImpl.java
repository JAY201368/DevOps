package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.exception.TomatoMallException;
import com.example.tomatomall.service.UploadService;
import com.example.tomatomall.util.OssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class UploadServiceImpl implements UploadService {

    @Autowired
    OssUtil ossUtil;

    @Override
    public String upload(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                log.error("上传失败：文件为空");
                throw TomatoMallException.fileUploadFail();
            }
            log.info("开始上传文件：{}", file.getOriginalFilename());
            String result = ossUtil.upload(file.getOriginalFilename(), file.getInputStream());
            log.info("文件上传成功：{}", result);
            return result;
        } catch (Exception e) {
            log.error("文件上传失败：", e);
            throw TomatoMallException.fileUploadFail();
        }
    }
}