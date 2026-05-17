package com.stonedt.intelligence.controller.campus;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.User;
import com.stonedt.intelligence.entity.campus.CampusEducationBaiduTaskRequest;
import com.stonedt.intelligence.entity.campus.CampusIngestTask;
import com.stonedt.intelligence.entity.campus.CampusSchoolSubject;
import com.stonedt.intelligence.service.campus.CampusAuditLogService;
import com.stonedt.intelligence.service.campus.CampusEducationService;
import com.stonedt.intelligence.util.UserUtil;
import com.stonedt.intelligence.vo.ResultVO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/campus/education")
public class CampusEducationController {

    private final CampusEducationService campusEducationService;
    private final CampusAuditLogService campusAuditLogService;
    private final UserUtil userUtil;

    public CampusEducationController(CampusEducationService campusEducationService,
                                     CampusAuditLogService campusAuditLogService,
                                     UserUtil userUtil) {
        this.campusEducationService = campusEducationService;
        this.campusAuditLogService = campusAuditLogService;
        this.userUtil = userUtil;
    }

    @GetMapping("/school/list")
    public ResultVO<PageInfo<CampusSchoolSubject>> listSchools(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String educationStage,
            @RequestParam(required = false) Integer status) {
        return ResultVO.success(campusEducationService.listSchools(pageNum, pageSize, keyword,
                region, educationStage, status));
    }

    @PostMapping("/school/save")
    public ResultVO<CampusSchoolSubject> saveSchool(@RequestBody CampusSchoolSubject school,
                                                    HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusSchoolSubject saved = campusEducationService.saveSchool(school, user.getUser_id());
            campusAuditLogService.record(request, "教育专题", "保存学校主体", "campus_school_subject",
                    String.valueOf(saved.getSchoolId()), JSON.toJSONString(school), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "教育专题", "保存学校主体", "campus_school_subject",
                    school == null ? null : String.valueOf(school.getSchoolId()), JSON.toJSONString(school), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/school/delete")
    public ResultVO<Void> deleteSchool(@RequestParam Long schoolId, HttpServletRequest request) {
        String params = "schoolId=" + schoolId;
        try {
            User user = userUtil.getuser(request);
            campusEducationService.deleteSchool(schoolId, user.getUser_id());
            campusAuditLogService.record(request, "教育专题", "删除学校主体", "campus_school_subject",
                    String.valueOf(schoolId), params, true, null);
            return ResultVO.success();
        } catch (Exception e) {
            campusAuditLogService.record(request, "教育专题", "删除学校主体", "campus_school_subject",
                    String.valueOf(schoolId), params, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/school/template")
    public ResponseEntity<byte[]> schoolImportTemplate() {
        byte[] content = campusEducationService.schoolImportTemplate().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=campus-school-template.csv")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(content);
    }

    @PostMapping("/school/import")
    public ResultVO<Map<String, Integer>> importSchools(@RequestParam("file") MultipartFile file,
                                                        HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            Map<String, Integer> result = campusEducationService.importSchools(content, user.getUser_id());
            campusAuditLogService.record(request, "教育专题", "导入学校主体", "campus_school_subject",
                    null, file.getOriginalFilename(), true, null);
            return ResultVO.success(result);
        } catch (IOException e) {
            campusAuditLogService.record(request, "教育专题", "导入学校主体", "campus_school_subject",
                    null, file == null ? null : file.getOriginalFilename(), false, e.getMessage());
            return ResultVO.error(400, "读取导入文件失败");
        } catch (Exception e) {
            campusAuditLogService.record(request, "教育专题", "导入学校主体", "campus_school_subject",
                    null, file == null ? null : file.getOriginalFilename(), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/topic/list")
    public ResultVO<List<Map<String, Object>>> listTopics(
            @RequestParam(defaultValue = "education_news") String topicType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime,
            @RequestParam(required = false) Integer limit) {
        return ResultVO.success(campusEducationService.listTopics(topicType, startTime, endTime, limit));
    }

    @GetMapping("/ranking/school-sentiment")
    public ResultVO<List<Map<String, Object>>> schoolSentimentRanking(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime,
            @RequestParam(required = false) Integer limit) {
        return ResultVO.success(campusEducationService.schoolSentimentRanking(keyword, startTime, endTime, limit));
    }

    @PostMapping("/baidu-task/create")
    public ResultVO<CampusIngestTask> createBaiduTask(@RequestBody CampusEducationBaiduTaskRequest taskRequest,
                                                      HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusIngestTask saved = campusEducationService.createBaiduTask(taskRequest, user.getUser_id());
            campusAuditLogService.record(request, "教育专题", "创建百度接入任务", "campus_ingest_task",
                    String.valueOf(saved.getTaskId()), JSON.toJSONString(taskRequest), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "教育专题", "创建百度接入任务", "campus_ingest_task",
                    taskRequest == null ? null : taskRequest.getTaskName(), JSON.toJSONString(taskRequest), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/baidu-task/create-and-run")
    public ResultVO<Map<String, Object>> createAndRunBaiduTask(@RequestBody CampusEducationBaiduTaskRequest taskRequest,
                                                               HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            Map<String, Object> result = campusEducationService.createAndRunBaiduTask(taskRequest, user.getUser_id());
            Object task = result.get("task");
            String objectId = task instanceof CampusIngestTask
                    ? String.valueOf(((CampusIngestTask) task).getTaskId())
                    : (taskRequest == null ? null : taskRequest.getTaskName());
            campusAuditLogService.record(request, "教育专题", "创建并运行百度接入任务", "campus_ingest_task",
                    objectId, JSON.toJSONString(taskRequest), true, null);
            return ResultVO.success(result);
        } catch (Exception e) {
            campusAuditLogService.record(request, "教育专题", "创建并运行百度接入任务", "campus_ingest_task",
                    taskRequest == null ? null : taskRequest.getTaskName(), JSON.toJSONString(taskRequest), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }
}
