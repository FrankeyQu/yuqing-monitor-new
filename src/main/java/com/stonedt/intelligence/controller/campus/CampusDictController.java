package com.stonedt.intelligence.controller.campus;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.User;
import com.stonedt.intelligence.entity.campus.CampusDictItem;
import com.stonedt.intelligence.entity.campus.CampusDictType;
import com.stonedt.intelligence.service.campus.CampusAuditLogService;
import com.stonedt.intelligence.service.campus.CampusDictService;
import com.stonedt.intelligence.util.UserUtil;
import com.stonedt.intelligence.vo.ResultVO;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/campus/dict")
public class CampusDictController {

    private final CampusDictService campusDictService;
    private final CampusAuditLogService campusAuditLogService;
    private final UserUtil userUtil;

    public CampusDictController(CampusDictService campusDictService,
                                CampusAuditLogService campusAuditLogService,
                                UserUtil userUtil) {
        this.campusDictService = campusDictService;
        this.campusAuditLogService = campusAuditLogService;
        this.userUtil = userUtil;
    }

    @GetMapping("/type/list")
    public ResultVO<PageInfo<CampusDictType>> listTypes(@RequestParam(defaultValue = "1") Integer pageNum,
                                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                                        @RequestParam(required = false) String keyword,
                                                        @RequestParam(required = false) Integer status) {
        return ResultVO.success(campusDictService.listTypes(pageNum, pageSize, keyword, status));
    }

    @PostMapping("/type/save")
    public ResultVO<CampusDictType> saveType(@RequestBody CampusDictType dictType, HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusDictType saved = campusDictService.saveType(dictType, user.getUser_id());
            campusAuditLogService.record(request, "数据字典", "保存类型", "campus_dict_type",
                    saved.getDictType(), JSON.toJSONString(dictType), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "数据字典", "保存类型", "campus_dict_type",
                    dictType == null ? null : dictType.getDictType(), JSON.toJSONString(dictType), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/type/delete")
    public ResultVO<Void> deleteType(@RequestParam String dictType, HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            campusDictService.deleteType(dictType, user.getUser_id());
            campusAuditLogService.record(request, "数据字典", "删除类型", "campus_dict_type",
                    dictType, "dictType=" + dictType, true, null);
            return ResultVO.success();
        } catch (Exception e) {
            campusAuditLogService.record(request, "数据字典", "删除类型", "campus_dict_type",
                    dictType, "dictType=" + dictType, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/item/list")
    public ResultVO<PageInfo<CampusDictItem>> listItems(@RequestParam(defaultValue = "1") Integer pageNum,
                                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                                        @RequestParam(required = false) String dictType,
                                                        @RequestParam(required = false) String keyword,
                                                        @RequestParam(required = false) Integer status) {
        return ResultVO.success(campusDictService.listItems(pageNum, pageSize, dictType, keyword, status));
    }

    @GetMapping("/item/enabled")
    public ResultVO<List<CampusDictItem>> enabledItems(@RequestParam String dictType) {
        return ResultVO.success(campusDictService.enabledItems(dictType));
    }

    @PostMapping("/item/save")
    public ResultVO<CampusDictItem> saveItem(@RequestBody CampusDictItem dictItem, HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusDictItem saved = campusDictService.saveItem(dictItem, user.getUser_id());
            campusAuditLogService.record(request, "数据字典", "保存字典项", "campus_dict_item",
                    saved.getDictType() + ":" + saved.getItemCode(), JSON.toJSONString(dictItem), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "数据字典", "保存字典项", "campus_dict_item",
                    dictItem == null ? null : dictItem.getDictType() + ":" + dictItem.getItemCode(),
                    JSON.toJSONString(dictItem), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/item/delete")
    public ResultVO<Void> deleteItem(@RequestParam String dictType,
                                     @RequestParam String itemCode,
                                     HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            campusDictService.deleteItem(dictType, itemCode, user.getUser_id());
            campusAuditLogService.record(request, "数据字典", "删除字典项", "campus_dict_item",
                    dictType + ":" + itemCode, "dictType=" + dictType + "&itemCode=" + itemCode, true, null);
            return ResultVO.success();
        } catch (Exception e) {
            campusAuditLogService.record(request, "数据字典", "删除字典项", "campus_dict_item",
                    dictType + ":" + itemCode, "dictType=" + dictType + "&itemCode=" + itemCode, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }
}
