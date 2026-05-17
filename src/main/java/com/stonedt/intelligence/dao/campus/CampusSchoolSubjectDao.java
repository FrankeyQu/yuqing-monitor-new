package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusSchoolSubject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusSchoolSubjectDao {

    int insert(CampusSchoolSubject school);

    int update(CampusSchoolSubject school);

    int logicalDelete(@Param("schoolId") Long schoolId,
                      @Param("updateUserId") Long updateUserId);

    CampusSchoolSubject selectBySchoolId(@Param("schoolId") Long schoolId);

    CampusSchoolSubject selectBySchoolName(@Param("schoolName") String schoolName);

    List<CampusSchoolSubject> list(@Param("keyword") String keyword,
                                   @Param("region") String region,
                                   @Param("educationStage") String educationStage,
                                   @Param("status") Integer status);

    List<CampusSchoolSubject> listActive();
}
