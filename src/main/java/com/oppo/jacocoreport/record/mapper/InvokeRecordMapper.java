package com.oppo.jacocoreport.record.mapper;

import com.oppo.jacocoreport.record.entity.InvokeRecord;
import com.oppo.jacocoreport.record.entity.InvokeRecordExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface InvokeRecordMapper {
    long countByExample(InvokeRecordExample example);

    int deleteByExample(InvokeRecordExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(InvokeRecord record);

    int insertSelective(InvokeRecord record);

    List<InvokeRecord> selectByExample(InvokeRecordExample example);

    InvokeRecord selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") InvokeRecord record, @Param("example") InvokeRecordExample example);

    int updateByExample(@Param("record") InvokeRecord record, @Param("example") InvokeRecordExample example);

    int updateByPrimaryKeySelective(InvokeRecord record);

    int updateByPrimaryKey(InvokeRecord record);
}