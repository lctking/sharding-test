package com.lctking.shardingtest.algorithm;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;
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
        Map<String, Collection<Comparable<?>>> columnNameAndShardingValuesMap = shardingValues.getColumnNameAndShardingValuesMap();
        Collection<String> result = new ArrayList<>(availableTargetNames.size());
        if(CollUtil.isNotEmpty(columnNameAndShardingValuesMap)){
            String usernameStr = "username";
            String regionStr = "region";
            Comparable<?> regionCompare = columnNameAndShardingValuesMap.get(regionStr).stream().findFirst().get();
            String actualRegion = regionCompare.toString();
            // 获取到表范围and该区域的表的起始下标
            List<Integer> tableShardingNodes = getTableShardingNodes(actualRegion);
            if(CollUtil.isEmpty(tableShardingNodes)){
                tableShardingNodes = getTableShardingNodes("other");
            }
            int tableStartIdx = tableShardingNodes.get(0);
            int tableEndIdx = tableShardingNodes.get(1);
            int tableRangeSize = tableEndIdx - tableStartIdx + 1;

            // 获取到username
            Comparable<?> usernameCompare = columnNameAndShardingValuesMap.get(usernameStr).stream().findFirst().get();
            String tbSuffix = String.valueOf(hashShardingValue(usernameCompare) % tableRangeSize + tableStartIdx);
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
     * 如asia-database-sharding-count: 2 表示亚洲地区有两个数据库
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
        }catch (Throwable ignored){}
        return null;
        //throw new ShardingAlgorithmInitializationException(getType(), "region-sharding-nodes cannot be null");
    }

    private long hashShardingValue(final Comparable<?> shardingValue) {
        return Math.abs((long) shardingValue.hashCode());
    }
}
