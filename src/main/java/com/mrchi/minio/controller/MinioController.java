package com.mrchi.minio.controller;

import com.mrchi.minio.common.result.Result;
import com.mrchi.minio.common.result.ResultUtil;
import com.mrchi.minio.config.MinioConfig;
import com.mrchi.minio.service.MinioService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@RequestMapping("/minio")
@RestController
@EnableAutoConfiguration
@Api(tags = "文件上传接口")
public class MinioController {

    @Autowired
    private MinioService minioService;

    @Autowired
    private MinioConfig minioConfig;

    @ApiOperation(value = "使用minio文件上传")
    @PostMapping("/uploadFile")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "MultipartFile", name = "file", value = "上传的文件", required = true),
            @ApiImplicitParam(dataType = "String", name = "bucketName", value = "对象存储桶名称", required = false)
    })
    public Result uploadFile(MultipartFile file, String bucketName) {
        try {
            bucketName = StringUtils.isNotBlank(bucketName) ? bucketName : minioConfig.getBucketName();
            if (!minioService.bucketExists(bucketName)) {
                minioService.makeBucket(bucketName);
            }
            String fileName = file.getOriginalFilename();
            String objectName = new SimpleDateFormat("yyyy/MM/dd/").format(new Date()) + UUID.randomUUID().toString().replaceAll("-", "")
                    + fileName.substring(fileName.lastIndexOf("."));

            InputStream inputStream = file.getInputStream();
            minioService.putObject(bucketName, objectName, inputStream);
            inputStream.close();
            return ResultUtil.success(minioService.getObjectUrl(bucketName, objectName));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.sendErrorMessage("上传失败");
        }
    }
    @ApiOperation(value = "使用minio下载文件")
    @GetMapping("/download")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "String", name = "fileName", value = "下载的文件", required = true),
            @ApiImplicitParam(dataType = "String", name = "bucketName", value = "指定下载桶名称", required = true)
    })
    public Result downloadObject(String fileName, String bucketName, HttpServletResponse response) {

        try{
        if (StringUtils.isBlank(fileName)) {
            return ResultUtil.sendErrorMessage("文件名不合法");
        }
        if (!minioService.bucketExists(bucketName)) {
            return ResultUtil.sendErrorMessage("桶不存在！");
        }

        minioService.downloadFile(bucketName, fileName, "", response);
        return ResultUtil.success(minioService.getObjectUrl(bucketName, fileName));
        }catch (Exception e ){
            return ResultUtil.sendErrorMessage("下载失败");
        }
    }
}