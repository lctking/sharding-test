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
     * 单个数据库下分表数-查询键
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
            //根据region地区，取到分表节点信息（形如{0..3}，表示当前地区有t_user_0到3。）
            //tableShardingNodes中存放的是[0,3]
            List<Integer> tableShardingNodes = getTableShardingNodes(actualRegion.toLowerCase());

            if(CollUtil.isEmpty(tableShardingNodes)){
                //如果根据当前region找不到数据表，则选择查询other地区的数据表（也意味着准备放入other地区对应的数据库表中
                tableShardingNodes = getTableShardingNodes("other");
            }
            if(CollUtil.isNotEmpty(tableShardingNodes)){
                //取到分表节点信息（形如{0..3}，表示当前地区有t_user_0到3。）中的初始下标，这里就是0
                int tableStartIdx = tableShardingNodes.get(0);
                //终点下标，这里就是3
                int tableEndIdx = tableShardingNodes.get(1);
                //3-0+1=4，这里表示asia区一共有四个表
                int tableRangeSize = tableEndIdx - tableStartIdx + 1;
                //取到用户名信息
                Comparable<?> usernameCompare = columnNameAndShardingValuesMap.get(usernameStr).stream().findFirst().get();
                // 根据username来判定取到地区数据库范围中的哪一个
                // hash(username)的值再对表范围取余，再加上起始下标（即表示从t_user_0-3这四个表中选择一个）
                // 再除以单库中表的数量（这里是2），表示根据表下标来判定数据库下标（例如表下标为t_user_4,4/2=2，表示这个表在ds_2中）；
                dbSuffix = String.valueOf((hashShardingValue(usernameCompare) % tableRangeSize + tableStartIdx) / this.tableShardingCount);
                //固定写法，表示取到的数据库为 ds_x
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
                //注意matcher.group要从1开始取，否则报错
                int tableStartIdx = Integer.parseInt(matcher.group(1));
                int tableEndIdx = Integer.parseInt(matcher.group(2));
                return Lists.newArrayList(tableStartIdx, tableEndIdx);
            }
        //这里要忽略异常，因为可能该地区名有误返回null，但是不妨碍下一步使用other地区
        //如果直接抛异常的话程序将直接中断而不是使用other地区来保底
        }catch (Throwable ignored){}
        return null;
    }

    /**
     * 根据props和查询字段来获取到单个数据库下分表数
     */
    private int getTableShardingCount(Properties props) {
        return Optional.ofNullable(props.getProperty(TABLE_SHARDING_COUNT)).map(Integer::parseInt).orElseThrow(() -> new ShardingAlgorithmInitializationException(getType(), "table-sharding-count cannot be null"));
    }

    /**
     * 取hash值
     */
    private long hashShardingValue(final Comparable<?> shardingValue) {
        return Math.abs((long) shardingValue.hashCode());
    }
}
