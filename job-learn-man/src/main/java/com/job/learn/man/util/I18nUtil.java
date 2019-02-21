package com.job.learn.man.util;

import com.job.learn.man.config.TaskAdminConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

/**
 * 国际化的东北  就是支持多种语言切换
 *
 * @author tangwei
 * @date 2019/2/18 19:39
 */
public class I18nUtil {
    private static Logger logger = LoggerFactory.getLogger(I18nUtil.class);


    private static Properties propery = null;

    public static Properties loadI18nProp() {
        if (propery != null) {
            return propery;
        }
        try {
            String i18n = TaskAdminConfig.getAdminConfig().getI18n();

            i18n = StringUtils.isNotBlank(i18n)?("_"+i18n):i18n;
            String i18nFile = MessageFormat.format("i18n/message{0}.properties", i18n);

            Resource resource = new ClassPathResource(i18nFile);
            EncodedResource encodedResource = new EncodedResource(resource,"UTF-8");
            propery = PropertiesLoaderUtils.loadProperties(encodedResource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return propery;
    }
    /**
     * get val of i18n key
     *
     * @param key
     * @return
     */
    public static String getString(String key) {
        return loadI18nProp().getProperty(key);
    }

}
