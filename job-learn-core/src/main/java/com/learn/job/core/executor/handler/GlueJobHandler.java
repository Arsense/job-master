package com.learn.job.core.executor.handler;

import com.learn.job.core.executor.AbstractJobHandler;
import com.learn.job.core.executor.domain.Result;

/**
 * @author tangwei
 * @date 2019/2/20 17:19
 */
public class GlueJobHandler extends AbstractJobHandler {

    private long glueUpdatetime;
    private AbstractJobHandler jobHandler;
    public GlueJobHandler(AbstractJobHandler jobHandler, long glueUpdatetime) {
        this.jobHandler = jobHandler;
        this.glueUpdatetime = glueUpdatetime;
    }
    public long getGlueUpdatetime() {
        return glueUpdatetime;
    }


    @Override
    public Result<String> execute(String param) throws Exception {
        return null;
    }
}
