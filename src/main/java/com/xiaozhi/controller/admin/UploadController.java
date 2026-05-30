package com.xiaozhi.controller.admin;

import com.xiaozhi.common.Result;
import com.xiaozhi.util.AliOssUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/admin/upload")
@RequiredArgsConstructor
public class UploadController {

    private final AliOssUtil aliOssUtil;

    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail("请选择图片");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || !isImage(originalName)) {
            return Result.fail("仅支持 jpg/png/gif/webp 格式");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.fail("图片大小不能超过5MB");
        }

        String ext = originalName.substring(originalName.lastIndexOf("."));
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String objectKey = "images/" + datePath + "/" + UUID.randomUUID().toString().replace("-", "") + ext;

        try {
            String url = aliOssUtil.upload(file.getBytes(), objectKey);
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            return Result.ok(data);
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return Result.fail("图片上传失败");
        }
    }

    private boolean isImage(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".png") || lower.endsWith(".gif")
                || lower.endsWith(".webp");
    }
}
