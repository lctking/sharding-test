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

public class UserDataBaseComplexAlgorithm implements ComplexKeysShardingAlgorithm {

    /**
     * 存放shardingsphere-config配置文件中rules[0].shardingAlgorithms.user_database_complex_mod.props的配置信息
     * 用来传递一些必要的配置，例如分片数量信息、每个地区下的分表数量及下标信息。自由度很高。
     */
    @Getter
    private Properties props;

    /**
     * 存放props中的分表数量信息
     */
    private int tableShardingCount;

    /**
     * 地区分片信息-查询键后缀。与地区字符串拼接，到props中查到形如”{0..3}“的分表节点信息
     */
    private static final String SHARDING_TABLE_NODES_SUFFIX = "-sharding-table-nodes";

    /**
     * 分表总数-查询键
     */
    private static final String TABLE_SHARDING_COUNT = "table-sharding-count";
    @Override
    public Collection<String> doSharding(Collection availableTargetNames, ComplexKeysShardingValue shardingValues) {
        //获取到分片涉及的字段，这里就是username和region与它们各自的值形成的键值对map
        Map<String, Collection<Comparable<?>>> columnNameAndShardingValuesMap = shardingValues.getColumnNameAndShardingValuesMap();
        //固定写法，arraylist大小为可用目标名的大小
        Collection<String> result = new ArrayList<>(availableTargetNames.size());
        if(CollUtil.isNotEmpty(columnNameAndShardingValuesMap)){
            String regionStr = "region";
            String usernameStr = "username";
            // 取出region键对应的值
            Comparable<?> regionCompare = columnNameAndShardingValuesMap.get(regionStr).stream().findFirst().get();
            String dbSuffix;
            String actualRegion = regionCompare.toString();

            List<Integer> tableShardingNodes = getTableShardingNodes(actualRegion.toLowerCase());

            if(CollUtil.isEmpty(tableShardingNodes)){
                //如果根据当前region找不到数据表，则选择放入other地区的数据表中
                //databaseShardingNodes = getDatabaseShardingNodes("other");
                tableShardingNodes = getTableShardingNodes("other");
            }
            if(CollUtil.isNotEmpty(tableShardingNodes)){
                int tableStartIdx = tableShardingNodes.get(0);
                int tableEndIdx = tableShardingNodes.get(1);
                int tableRangeSize = tableEndIdx - tableStartIdx + 1;
                Comparable<?> usernameCompare = columnNameAndShardingValuesMap.get(usernameStr).stream().findFirst().get();
                // 根据username来判定取到地区数据库范围中的哪一个
                dbSuffix = String.valueOf((hashShardingValue(usernameCompare) % tableRangeSize + tableStartIdx) / this.tableShardingCount);
                result.add("ds_" + dbSuffix);
            }
        }
        return result;
    }

    @Override
    public void init(Properties props) {
        this.props = props;
        this.tableShardingCount = getTableShardingCount(props);
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
                int tableStartIdx = Integer.parseInt(matcher.group(1));
                int tableEndIdx = Integer.parseInt(matcher.group(2));
                return Lists.newArrayList(tableStartIdx, tableEndIdx);
            }
        }catch (Throwable ignored){}
        return null;
        //throw new ShardingAlgorithmInitializationException(getType(), "region-sharding-nodes cannot be null");
    }

    private int getTableShardingCount(Properties props) {
        return Optional.ofNullable(props.getProperty(TABLE_SHARDING_COUNT)).map(Integer::parseInt).orElseThrow(() -> new ShardingAlgorithmInitializationException(getType(), "table-sharding-count cannot be null"));
    }

    private long hashShardingValue(final Comparable<?> shardingValue) {
        return Math.abs((long) shardingValue.hashCode());
    }
}
