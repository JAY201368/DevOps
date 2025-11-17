package com.example.tomatomall.controller;

import com.example.tomatomall.service.UploadService;
import com.example.tomatomall.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UploadController {

    @Autowired
    private UploadService uploadService;

    @PostMapping("/image")
    public ResultVO<String> upload(@RequestParam("file") MultipartFile file) {
        return ResultVO.buildSuccess(uploadService.upload(file));
    }

}
