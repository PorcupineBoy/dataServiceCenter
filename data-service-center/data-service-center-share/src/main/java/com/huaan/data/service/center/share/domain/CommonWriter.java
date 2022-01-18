package com.huaan.data.service.center.share.domain;

import com.alibaba.datax.common.plugin.AbstractJobPlugin;
import com.alibaba.datax.common.plugin.AbstractTaskPlugin;
import com.alibaba.datax.common.plugin.RecordReceiver;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.fastjson.JSONObject;

import java.util.List;


public abstract class CommonWriter {
    /**
     * 每个Writer插件必须实现Job内部类
     */
    public abstract static class Job extends AbstractJobPlugin {
        /**
         * 切分任务。<br>
         *
         * @param mandatoryNumber
         *            为了做到Reader、Writer任务数对等，这里要求Writer插件必须按照源端的切分数进行切分。否则框架报错！
         *
         * */
        public abstract List<Configuration> split(int mandatoryNumber);
    }

    /**
     * 每个Writer插件必须实现Task内部类
     */
    public abstract static class Task  {
        public void init() {

        }

        public void destroy() {

        }

        public void prepare() {

        }

        public abstract void startWrite(final List<JSONObject> data);

        public boolean supportFailOver(){return false;}
    }
}
