package com.oppo.test.coverage.backend.record.mapper;

import com.oppo.test.coverage.backend.record.entity.TestRecord;
import com.oppo.test.coverage.backend.record.entity.TestRecordExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestRecordMapper {
    long countByExample(TestRecordExample example);

    int deleteByExample(TestRecordExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(TestRecord record);

    int insertSelective(TestRecord record);

    List<TestRecord> selectByExample(TestRecordExample example);

    TestRecord selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") TestRecord record, @Param("example") TestRecordExample example);

    int updateByExample(@Param("record") TestRecord record, @Param("example") TestRecordExample example);

    int updateByPrimaryKeySelective(TestRecord record);

    int updateByPrimaryKey(TestRecord record);
}