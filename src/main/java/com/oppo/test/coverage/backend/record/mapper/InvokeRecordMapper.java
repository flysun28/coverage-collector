package com.oppo.test.coverage.backend.record.mapper;

import com.oppo.test.coverage.backend.record.entity.InvokeRecord;
import com.oppo.test.coverage.backend.record.entity.InvokeRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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