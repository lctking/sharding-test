package com.lctking.shardingtest.algorithm;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;
import org.apache.shardingsphere.sharding.exception.algorithm.sharding.ShardingAlgorithmClassImplementationException;
import org.apache.shardingsphere.sharding.exception.algorithm.sharding.ShardingAlgorithmInitializationException;
import cn.hutool.core.collection.CollUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserTableComplexAlgorithm implements ComplexKeysShardingAlgorithm {

    @Getter
    private Properties props;

    private int shardingCount;

    private int tableShardingCount;

    private static final String SHARDING_TABLE_NODES_SUFFIX = "-sharding-table-nodes";

    private static final String SHARDING_COUNT = "sharding-count";

    private static final String TABLE_SHARDING_COUNT = "table-sharding-count";
    @Override
    public Collection<String> doSharding(Collection availableTargetNames, ComplexKeysShardingValue shardingValues) {
        //获取到分片涉及的字段，这里就是username和region与它们各自的值形成的键值对map
        Map<String, Collection<Comparable<?>>> columnNameAndShardingValuesMap = shardingValues.getColumnNameAndShardingValuesMap();
        //固定写法，arraylist大小为可用目标名的大小
        Collection<String> result = new ArrayList<>(availableTargetNames.size());
        if(CollUtil.isNotEmpty(columnNameAndShardingValuesMap)){
            String usernameStr = "username";
            String regionStr = "region";
            Comparable<?> regionCompare = columnNameAndShardingValuesMap.get(regionStr).stream().findFirst().get();
            String actualRegion = regionCompare.toString();
            // 获取到表范围and该区域的表的起始下标
            //根据region地区，取到分表节点信息（形如{0..3}，表示当前地区有t_user_0到3。）
            //tableShardingNodes中存放的是[0,3]
            List<Integer> tableShardingNodes = getTableShardingNodes(actualRegion);
            if(CollUtil.isEmpty(tableShardingNodes)){
                //如果根据当前region找不到数据表，则选择查询other地区的数据表（也意味着准备放入other地区对应的数据库表中
                tableShardingNodes = getTableShardingNodes("other");
            }
            if(CollUtil.isEmpty(tableShardingNodes)){
                // 该地区和other地区的分表节点配置都找不到
                throw new NullPointerException(String.format("The configuration of sharding-table-nodes in the %s region and other region cannot be found",actualRegion));
            }
            //取到分表节点信息（形如{0..3}，表示当前地区有t_user_0到3。）中的初始下标，这里就是0
            int tableStartIdx = tableShardingNodes.get(0);
            //终点下标，这里就是3
            int tableEndIdx = tableShardingNodes.get(1);
            //3-0+1=4，这里表示asia区一共有四个表
            int tableRangeSize = tableEndIdx - tableStartIdx + 1;
            // 获取到username
            Comparable<?> usernameCompare = columnNameAndShardingValuesMap.get(usernameStr).stream().findFirst().get();
            // hash(username)的值再对表范围取余，再加上起始下标（即表示从t_user_0-3这四个表中选择一个）
            String tbSuffix = String.valueOf(hashShardingValue(usernameCompare) % tableRangeSize + tableStartIdx);
            //固定写法，逻辑表名拼接_tbSuffix
            result.add(shardingValues.getLogicTableName()+"_"+tbSuffix);
        }
        return result;
    }

    @Override
    public void init(Properties props) {
        this.props = props;
        this.shardingCount = getShardingCount(props);
        this.tableShardingCount = getTableShardingCount(props);
    }

    private int getShardingCount(Properties props) {
        return Optional.ofNullable(props.getProperty(SHARDING_COUNT)).map(Integer::parseInt).orElseThrow(() -> new ShardingAlgorithmInitializationException(getType(), "sharding-count cannot be null"));
    }

    private int getTableShardingCount(Properties props) {
        return Optional.ofNullable(props.getProperty(TABLE_SHARDING_COUNT)).map(Integer::parseInt).orElseThrow(() -> new ShardingAlgorithmInitializationException(getType(), "table-sharding-count cannot be null"));
    }

    /**
     * 根据地区名获取到shardingSphere配置文件中的各地表范围
     * 如 asia-sharding-table-nodes: "{0..3}"表示亚洲地区的数据表范围是t_user_0-3
     */
    private List<Integer> getTableShardingNodes(String region) {
        String actualDataBaseShardingCountSuffix = region + SHARDING_TABLE_NODES_SUFFIX;
        String s = this.props.getProperty(actualDataBaseShardingCountSuffix);

        try{
            // 正则表达式匹配
            String regex = "\\{(\\d+)\\.\\.(\\d+)\\}";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(s);

            if (matcher.matches()) {
                //下标从1开始！
                int tableStartIdx = Integer.parseInt(matcher.group(1));
                int tableEndIdx = Integer.parseInt(matcher.group(2));
                return Lists.newArrayList(tableStartIdx, tableEndIdx);
            }
        //这里要忽略异常，因为可能该地区名有误返回null，但是不妨碍下一步使用other地区
        //如果直接抛异常的话程序将直接中断而不是使用other地区来保底
        }catch (Throwable ignored){}
        return null;
        //throw new ShardingAlgorithmInitializationException(getType(), "region-sharding-nodes cannot be null");
    }

    private long hashShardingValue(final Comparable<?> shardingValue) {
        return Math.abs((long) shardingValue.hashCode());
    }
}
