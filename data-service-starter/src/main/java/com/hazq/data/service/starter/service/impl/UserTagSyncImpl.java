package com.hazq.data.service.starter.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.expression.ExpressionUtil;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hazq.data.service.starter.model.CustomerInfo;
import com.hazq.data.service.starter.model.TagModel;
import com.hazq.data.service.starter.properties.EsInfoConfiguration;
import com.huaan.data.service.center.constan.ConstantSql;
import com.huaan.data.service.center.share.config.DefaultDBinfo;
import com.huaan.data.service.center.share.domain.DBinfo;
import com.huaan.data.service.center.share.domain.ESinfo;
import com.huaan.data.service.center.share.runner.ESWriterRunner;
import com.huaan.data.service.center.share.taskgroup.TaskGroupContainer;
import com.huaan.data.service.center.vendor.service.CommonDBService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.huaan.data.service.center.constan.ConstantSql.DEFAULT_USER_SOURCE_TABLE;
import static com.huaan.data.service.center.constan.ConstantSql.QUERY_SQL_TEMPLATE;

@Slf4j
@Service
public class UserTagSyncImpl implements UserTagSync {
    private static DBinfo dBinfo;

    static {
        dBinfo = DefaultDBinfo.buildDefaultDBinfo();
    }

    @Autowired
    private CommonDBService dbService;

    /**
     * @param left      表达式左边的时间
     * @param right     表达式右边的时间
     * @param operation 表达式 ： <= 、>=  =
     * @return
     */
    private static boolean compareDate(String left, String right, String operation) {
        Date leftDate = DateUtil.parseDateTime(left);
        Date rightDate = DateUtil.parseDateTime(right);
        switch (operation) {
            case ">=":
                return leftDate.getTime() >= rightDate.getTime();
            case "<=":
                return leftDate.getTime() <= rightDate.getTime();
            case "=":
                return leftDate.getTime() == rightDate.getTime();
            default:
                throw new RuntimeException("不符合表达式");
        }
    }

    @Override
    public void autoSync() {
        synchronized (UserTagSyncImpl.class) {

            /**
             * 第一步：获取全量标签。
             */
            List<TagModel> tagsInfo = getTagsInfo();
            /**
             * 第一步：获取全量标签的全部指标。
             */
            List<JSONObject> targetInfo = getTargetInfo();
            /**
             * 第一步：获取全量用户的全部id并进行切割。
             */
            List<CustomerInfo> userAllList = getUserIdList();
            List<List<CustomerInfo>> partition = Lists.partition(userAllList, 10000);


            List<String> columns = new ArrayList<>();
            /**
             *  process start
             */
            tagsInfo.forEach(info -> {
                Configuration configuration = Configuration.from(info);
                String tagOperationRule = configuration.getString("tagOperationRule");
                JSONArray objects = JSON.parseArray(tagOperationRule);
                objects.forEach(o -> {
                    JSONObject jsonObject = BeanUtil.toBean(o, JSONObject.class);
                    Object targetIds = jsonObject.get("targetIds");
                    columns.addAll((Collection<? extends String>) targetIds);
                });
                //
            });
            /**
             * 第二步 过滤不在标签内的指标
             */
            targetInfo = targetInfo.stream().filter(target ->
                    columns.contains(target.getString("id"))).collect(Collectors.toList());
            /**
             * 第三步 获取全量用户ID
             *  进行切割
             */
            List<JSONObject> process = new ArrayList<>();
            try {
                for (List<CustomerInfo> users : partition) {
                    /**
                     * 用户的实际字段数据
                     */
                    List<JSONObject> userObject = queryUserRealInfo(users, targetInfo);

                    /**
                     * 第四步： 执行匹配规则。
                     */
                    process.addAll(process(tagsInfo, userObject, targetInfo));
                    //todo 存入ES
                    if (process.size() >= 1000) {
                        ESWriterRunner runner = new ESWriterRunner(process,EsInfoConfiguration.getEsinfoInstance(null));
                        //runner.run();
                        TaskGroupContainer.pool.execute(runner);
                        process.clear();
                    }
                }
                if (!process.isEmpty()) {
                    ESWriterRunner runner = new ESWriterRunner(process,EsInfoConfiguration.getEsinfoInstance(null));
                    TaskGroupContainer.pool.execute(runner);
                }
            } catch (Exception ioException){
                log.error(ioException.getMessage());
            } finally {
               // TaskGroupContainer.pool.shutdown();
            }
        }
    }

    /**
     * 获取用户实际信息
     *
     * @param usersInfo    需要匹配的用户标签 | 过滤后需要入库的用户信息
     * @param tagsInfo     需要匹配的用户标签
     * @param targetModels 需要匹配的指标规则
     * @return
     */
    public List<JSONObject> process(List<TagModel> tagsInfo, List<JSONObject> usersInfo, List<JSONObject> targetModels) {
        List<JSONObject> customerInfos = new ArrayList<>();
        usersInfo.forEach(user -> {
            /**
             * 每个用户 对 N个标签进行匹配
             */
            List<String> tags = new ArrayList<>();
            tagsInfo.forEach(info -> {
                Configuration configuration = Configuration.from(info);
                String tagOperationRule = configuration.getString("tagOperationRule");
                JSONArray rules = JSON.parseArray(tagOperationRule);
                //指标集合内匹配需要均为true 。
                AtomicReference<Boolean> allFlag = new AtomicReference<>(Boolean.FALSE);
                rules.forEach(o -> {
                    JSONObject jsonObject = BeanUtil.toBean(o, JSONObject.class);
                    List<JSONArray> targetIds = Collections.singletonList(JSONArray.parseArray(jsonObject.getString("targetIds")));
                    /**
                     * 一个标签内有几组指标，关系为或
                     */
                    for (JSONArray targetId : targetIds) {
                        AtomicReference<Boolean> thisFlag = new AtomicReference<>(Boolean.FALSE);
                        /**
                         * 一组内指标，关系为且
                         */
                        for (Object id : targetId) {
                            Optional<JSONObject> optional = targetModels.stream().filter(ids -> id.equals(ids.get("id").toString())).findFirst();
                            if (optional.isPresent()) {
                                JSONObject thisTarget = optional.get();
                                /**
                                 * 当前用户的第一个标签的第一个指标的判定
                                 * 用户当前的info ，取对应的指标字段 的值。 再进行 逻辑判断对比。
                                 */
                                try {
                                    analysisType(user, thisTarget, thisFlag);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (thisFlag.get()) {
                            allFlag.set(thisFlag.get());
                            break;
                        }
                    }
                });
                if (allFlag.get()) {
                    tags.add(info.getAttrCode());
                }
            });
            //结束后 观察flag 标志。 若为true 则说明匹配成功。
            if (CollectionUtil.isNotEmpty(tags)) {
                JSONObject customerInfo = new JSONObject();
                customerInfo.put("clientId", user.getString("clientId"));
                //将Listtags 转成 逗号分割的 string 字符串
                String tagStr = StringUtils.strip(tags.toString(), "[]").replace(" ", "");
                HashMap<String, String> hashMap = Maps.newHashMap();
                hashMap.put(tagStr, "1");
                customerInfo.put("tags", hashMap);
                customerInfos.add(customerInfo);
            }
        });
        return customerInfos;
    }

    /**
     * @param userInfo   指当前用户实际值集合
     * @param thisTarget 当前指标
     * @param flag       当前指标组的判定
     * @return
     */
    public void analysisType(JSONObject userInfo, JSONObject thisTarget, AtomicReference<Boolean> flag) throws ParseException {
        String type = thisTarget.getString("attrType").toLowerCase(Locale.ROOT);
        String attrCode = thisTarget.getString("attrCode");
        String targetOperationRule = thisTarget.getString("targetOperationRule");
        String userValue = userInfo.getString(attrCode);
        if (StringUtils.isEmpty(userValue)) {
            flag.set(false);
            return;
        }
        HashMap<String, Object> map = new HashMap<>();
        String code = targetOperationRule.replace("attrCode", "");
        switch (type) {
            case "number":
            case "int":
            case "double":
            case "long":
            case "float":
                map.put("attrCode", Double.parseDouble(userValue));
                Object eval = ExpressionUtil.eval(targetOperationRule, map);
                flag.set((Boolean) eval);
                break;
            case "string":
                if (targetOperationRule.contains("!=")) {
                    String rule = code.replace("!=", "").trim();
                    flag.set(!userValue.equals(rule));
                } else {
                    String rule = code.replace("=", "").trim();
                    flag.set(userValue.equals(rule));
                }
                break;
            case "date":
            case "datetime":
            case "time":
                if (code.contains(">=")) {
                    String replace = code.replace(">=", "");
                    boolean compareDate = compareDate(userValue, replace, ">=");
                    flag.set(compareDate);

                } else if (code.contains("<=")) {
                    String replace = code.replace("<=", "");
                    boolean compareDate = compareDate(userValue, replace, "<=");
                    flag.set(compareDate);

                } else if (code.contains("=")) {
                    String replace = code.replace("=", "");
                    boolean compareDate = compareDate(userValue, replace, "=");
                    flag.set(compareDate);
                }
                break;
            default:
                throw new RuntimeException("匹配规则不匹配");
        }
    }

    /**
     * 查询用户具体的信息  done
     * 获取用户实际信息
     *
     * @param users
     * @param targetModelList
     * @return
     */
    public List<JSONObject> queryUserRealInfo(List<CustomerInfo> users, List<JSONObject> targetModelList) {
        Assert.notEmpty(users, "users 集合不能为空");
        Assert.notEmpty(targetModelList, "targetModelList 集合不能为空");
        List<String> columns = new ArrayList<>(targetModelList.size());
        targetModelList.forEach(target -> columns.add(target.getString("attrCode")));
        String columnsStr = StringUtils.strip(columns.toString(), "[]").replace(" ", "");
        StringBuilder whereIn = new StringBuilder("  C.client_id in ( ");
        users.forEach(user -> whereIn.append("'").append(user.getClientId()).append("'").append(","));
        whereIn.deleteCharAt(whereIn.length() - 1);
        whereIn.append(" )");
        String sql = QUERY_SQL_TEMPLATE.replaceFirst("%s", columnsStr + ",C.client_id")
                .replaceFirst("%s", DEFAULT_USER_SOURCE_TABLE)
                .replaceFirst("%s", whereIn.toString());
        dBinfo.setQuerySql(sql);
        return dbService.getRecordList(dBinfo);
    }

    /**
     * 获取全部指标信息
     * 1、id 指标ID
     * 2、attrCode  指标字段
     * 3、attrName 指标字段名
     * 4、targetOperationRule 指标规则
     *
     * @return
     */
    public List<JSONObject> getTargetInfo() {

        dBinfo.setQuerySql(ConstantSql.QUERY_SQL_TARGETS);

        return dbService.getRecordList(dBinfo, JSONObject.class);
    }

    /**
     * 获取全部标签信息
     * 1、attrCode : 标签编码
     * 2、attrName: 标签名称
     * 3、tarOperationRule 标签规则
     *
     * @return
     */
    public List<TagModel> getTagsInfo() {

        dBinfo.setQuerySql(ConstantSql.QUERY_SQL_TAG);

        return dbService.getRecordList(dBinfo, TagModel.class);
    }

    /**
     * 获取全部用户ID信息
     *
     * @return
     */
    public List<CustomerInfo> getUserIdList() {
        dBinfo.setQuerySql(ConstantSql.QUERY_SQL_USER_ID_LIST);
        return dbService.getRecordList(dBinfo, CustomerInfo.class);
    }

}
