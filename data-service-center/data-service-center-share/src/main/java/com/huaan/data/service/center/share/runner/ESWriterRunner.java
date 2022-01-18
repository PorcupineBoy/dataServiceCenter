package com.huaan.data.service.center.share.runner;

import com.alibaba.fastjson.JSONObject;
import com.huaan.data.service.center.share.domain.CommonESWriter;
import com.huaan.data.service.center.share.domain.CommonWriter;
import com.huaan.data.service.center.share.domain.ESinfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by jingxing on 14-9-1.
 * <p/>
 * 单个slice的writer执行调用
 */
public class ESWriterRunner implements Runnable {

    private static final Logger LOG = LoggerFactory
            .getLogger(ESWriterRunner.class);
    private List<JSONObject> writeData;
    private volatile ESinfo esinfo;
    public ESWriterRunner(List<JSONObject> jsonObjectList, ESinfo esinfo) {
        setWriteData(jsonObjectList);
        setEsinfo(esinfo);
    }

    public void setWriteData(List<JSONObject> writeData) {
        this.writeData = writeData;
    }
    public void setEsinfo(ESinfo esinfo) {
        this.esinfo = esinfo;
    }
    @Override
    public void run() {
        //Validate.isTrue(this.recordReceiver != null);

        CommonWriter.Task taskWriter = new CommonESWriter.Task(esinfo);
        //统计waitReadTime，并且在finally end
        //PerfRecord channelWaitRead = new PerfRecord(getTaskGroupId(), getTaskId(), PerfRecord.PHASE.WAIT_READ_TIME);
        try {
            // channelWaitRead.start();
            LOG.debug("task writer starts to do init ...");
            taskWriter.init();
            taskWriter.prepare();
            taskWriter.startWrite(writeData);

            LOG.debug("task writer starts to write ...");

            //super.markSuccess();
        } catch (Throwable e) {
            LOG.error("Writer Runner Received Exceptions:", e);
            //super.markFail(e);
        } finally {
            LOG.debug("task writer starts to do destroy ...");
        }
    }

}
