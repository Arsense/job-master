package com.job.learn.man;

import com.job.learn.man.util.I18nUtil;
import org.junit.Test;
/**
 * email util test
 *
 * @author xuxueli 2017-12-22 17:16:23
 */
public class I18nUtilTest extends BaseTest {

    @Test
    public void test(){
            System.out.println(I18nUtil.getString("admin_name"));
//        System.out.println(I18nUtil.getMultString("admin_name", "admin_name_full"));
//        System.out.println(I18nUtil.getMultString());
    }

}
