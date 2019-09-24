package org.casbin.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author fangzhengjin
 * @version V1.0
 * @title: FileUtils
 * @package org.casbin.utils
 * @description:
 * @date 2019/9/24 17:06
 */
@Slf4j
@UtilityClass
public class FileUtils {

    private String removePrefix(String str, String prefix) {
        if (str.startsWith(prefix)) {
            return str.substring(prefix.length());
        }
        return str;
    }

    public File getFile(String filePath) {
        File template = new File(removePrefix(filePath, "classpath:"));
        if (template.exists()) {
            return template;
        } else {
            return null;
        }
    }

    public InputStream getFileAsInputStream(String filePath) {
        File file = getFile(filePath);
        try {
            if (file != null && file.exists()) {
                return new FileInputStream(file);
            }
            Resource resource = new DefaultResourceLoader().getResource(filePath);
            if (resource.exists()) {
                return resource.getInputStream();
            }
            return null;
        } catch (Exception e) {
            logger.error("load file $filePath by inputStream error", e);
            return null;
        }
    }

    public String getFileAsText(String filePath) {
        try (InputStream inputStream = getFileAsInputStream(filePath);) {
            if (inputStream != null) return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}
