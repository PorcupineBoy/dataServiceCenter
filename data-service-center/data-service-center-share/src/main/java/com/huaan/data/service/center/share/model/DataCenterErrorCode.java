package com.huaan.data.service.center.share.model;

import com.alibaba.datax.common.spi.ErrorCode;

/**
 * TODO: 根据现有日志数据分析各类错误，进行细化。
 * 
 * <p>请不要格式化本类代码</p>
 */
public enum DataCenterErrorCode implements ErrorCode {



    ILLEGAL_SQL_ERROR("Framework-00", "DataX引擎安装错误, 请联系您的运维解决 ."),
	ARGUMENT_ERROR("Framework-01", "DataX引擎运行错误，该问题通常是由于内部编程错误引起，请联系DataX开发团队解决 ."),
	RUNTIME_ERROR("Framework-02", "DataX引擎运行过程出错，具体原因请参看DataX运行结束时的错误诊断信息  ."),
	CONFIG_ERROR("Framework-03", "配置错误，该问题通常是由于入参填写错误引起，"),
    CALL_DATAX_SERVICE_FAILED("Framework-18", "请求 DataX Service 出错."),
    CALL_REMOTE_FAILED("Framework-19", "远程调用失败"),
    KILLED_EXIT_VALUE("Framework-143", "Job 收到了 Kill 命令.");

    private final String code;

    private final String description;

    private DataCenterErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return String.format("Code:[%s], Description:[%s]. ", this.code,
                this.description);
    }

    /**
     * 通过 "Framework-143" 来标示 任务是 Killed 状态
     */
    public int toExitValue() {
        if (this == DataCenterErrorCode.KILLED_EXIT_VALUE) {
            return 143;
        } else {
            return 1;
        }
    }

}
