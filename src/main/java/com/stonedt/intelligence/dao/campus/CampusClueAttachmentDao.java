package com.stonedt.intelligence.dao.campus;

import com.stonedt.intelligence.entity.campus.CampusClueAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CampusClueAttachmentDao {

    int insert(CampusClueAttachment attachment);

    int logicalDelete(@Param("attachmentId") Long attachmentId, @Param("clueId") Long clueId);

    List<CampusClueAttachment> listByClueId(@Param("clueId") Long clueId);
}
