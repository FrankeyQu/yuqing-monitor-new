package com.stonedt.intelligence.controller.campus;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.stonedt.intelligence.entity.User;
import com.stonedt.intelligence.entity.campus.CampusAiCallLog;
import com.stonedt.intelligence.entity.campus.CampusAiFeatureBinding;
import com.stonedt.intelligence.entity.campus.CampusAiModel;
import com.stonedt.intelligence.entity.campus.CampusAiPromptTemplate;
import com.stonedt.intelligence.entity.campus.CampusAiProvider;
import com.stonedt.intelligence.service.campus.CampusAuditLogService;
import com.stonedt.intelligence.service.campus.ai.CampusAiService;
import com.stonedt.intelligence.util.UserUtil;
import com.stonedt.intelligence.vo.ResultVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/campus/ai")
public class CampusAiController {

    private final CampusAiService campusAiService;
    private final CampusAuditLogService campusAuditLogService;
    private final UserUtil userUtil;

    public CampusAiController(CampusAiService campusAiService,
                              CampusAuditLogService campusAuditLogService,
                              UserUtil userUtil) {
        this.campusAiService = campusAiService;
        this.campusAuditLogService = campusAuditLogService;
        this.userUtil = userUtil;
    }

    @GetMapping("/overview")
    public ResultVO<Map<String, Object>> overview() {
        return ResultVO.success(campusAiService.overview());
    }

    @GetMapping("/provider/list")
    public ResultVO<PageInfo<CampusAiProvider>> listProviders(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String providerType,
            @RequestParam(required = false) Integer enabled) {
        return ResultVO.success(campusAiService.listProviders(pageNum, pageSize, keyword, providerType, enabled));
    }

    @PostMapping("/provider/save")
    public ResultVO<CampusAiProvider> saveProvider(@RequestBody CampusAiProvider provider,
                                                   HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusAiProvider saved = campusAiService.saveProvider(provider, user.getUser_id());
            campusAuditLogService.record(request, "AI能力管理", "保存供应商", "campus_ai_provider",
                    saved.getProviderCode(), JSON.toJSONString(provider), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "AI能力管理", "保存供应商", "campus_ai_provider",
                    provider == null ? null : provider.getProviderCode(), JSON.toJSONString(provider), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/provider/delete")
    public ResultVO<Void> deleteProvider(@RequestParam String providerCode,
                                         HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            campusAiService.deleteProvider(providerCode, user.getUser_id());
            campusAuditLogService.record(request, "AI能力管理", "删除供应商", "campus_ai_provider",
                    providerCode, "providerCode=" + providerCode, true, null);
            return ResultVO.success();
        } catch (Exception e) {
            campusAuditLogService.record(request, "AI能力管理", "删除供应商", "campus_ai_provider",
                    providerCode, "providerCode=" + providerCode, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/provider/test")
    public ResultVO<Map<String, Object>> testProvider(@RequestParam String providerCode,
                                                      HttpServletRequest request) {
        try {
            Map<String, Object> result = campusAiService.testProvider(providerCode);
            campusAuditLogService.record(request, "AI能力管理", "测试供应商配置", "campus_ai_provider",
                    providerCode, "providerCode=" + providerCode, true, null);
            return ResultVO.success(result);
        } catch (Exception e) {
            campusAuditLogService.record(request, "AI能力管理", "测试供应商配置", "campus_ai_provider",
                    providerCode, "providerCode=" + providerCode, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/model/list")
    public ResultVO<PageInfo<CampusAiModel>> listModels(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String providerCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer enabled) {
        return ResultVO.success(campusAiService.listModels(pageNum, pageSize, providerCode, keyword, enabled));
    }

    @PostMapping("/model/save")
    public ResultVO<CampusAiModel> saveModel(@RequestBody CampusAiModel model,
                                             HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusAiModel saved = campusAiService.saveModel(model, user.getUser_id());
            campusAuditLogService.record(request, "AI能力管理", "保存模型", "campus_ai_model",
                    saved.getProviderCode() + ":" + saved.getModelCode(), JSON.toJSONString(model), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "AI能力管理", "保存模型", "campus_ai_model",
                    model == null ? null : model.getProviderCode() + ":" + model.getModelCode(),
                    JSON.toJSONString(model), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/model/delete")
    public ResultVO<Void> deleteModel(@RequestParam String providerCode,
                                      @RequestParam String modelCode,
                                      HttpServletRequest request) {
        String objectId = providerCode + ":" + modelCode;
        try {
            User user = userUtil.getuser(request);
            campusAiService.deleteModel(providerCode, modelCode, user.getUser_id());
            campusAuditLogService.record(request, "AI能力管理", "删除模型", "campus_ai_model",
                    objectId, "providerCode=" + providerCode + "&modelCode=" + modelCode, true, null);
            return ResultVO.success();
        } catch (Exception e) {
            campusAuditLogService.record(request, "AI能力管理", "删除模型", "campus_ai_model",
                    objectId, "providerCode=" + providerCode + "&modelCode=" + modelCode, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/feature/list")
    public ResultVO<PageInfo<CampusAiFeatureBinding>> listFeatureBindings(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String featureType,
            @RequestParam(required = false) Integer enabled) {
        return ResultVO.success(campusAiService.listFeatureBindings(pageNum, pageSize, keyword, featureType, enabled));
    }

    @PostMapping("/feature/save")
    public ResultVO<CampusAiFeatureBinding> saveFeatureBinding(@RequestBody CampusAiFeatureBinding binding,
                                                               HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusAiFeatureBinding saved = campusAiService.saveFeatureBinding(binding, user.getUser_id());
            campusAuditLogService.record(request, "AI能力管理", "保存功能绑定", "campus_ai_feature_binding",
                    saved.getFeatureCode(), JSON.toJSONString(binding), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "AI能力管理", "保存功能绑定", "campus_ai_feature_binding",
                    binding == null ? null : binding.getFeatureCode(), JSON.toJSONString(binding), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/prompt/list")
    public ResultVO<PageInfo<CampusAiPromptTemplate>> listPromptTemplates(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String featureCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer enabled) {
        return ResultVO.success(campusAiService.listPromptTemplates(pageNum, pageSize, featureCode, keyword, enabled));
    }

    @PostMapping("/prompt/save")
    public ResultVO<CampusAiPromptTemplate> savePromptTemplate(@RequestBody CampusAiPromptTemplate promptTemplate,
                                                               HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            CampusAiPromptTemplate saved = campusAiService.savePromptTemplate(promptTemplate, user.getUser_id());
            campusAuditLogService.record(request, "AI能力管理", "保存提示词", "campus_ai_prompt_template",
                    String.valueOf(saved.getTemplateId()), JSON.toJSONString(promptTemplate), true, null);
            return ResultVO.success(saved);
        } catch (Exception e) {
            campusAuditLogService.record(request, "AI能力管理", "保存提示词", "campus_ai_prompt_template",
                    promptTemplate == null ? null : String.valueOf(promptTemplate.getTemplateId()),
                    JSON.toJSONString(promptTemplate), false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @PostMapping("/prompt/delete")
    public ResultVO<Void> deletePromptTemplate(@RequestParam Long templateId,
                                               HttpServletRequest request) {
        try {
            User user = userUtil.getuser(request);
            campusAiService.deletePromptTemplate(templateId, user.getUser_id());
            campusAuditLogService.record(request, "AI能力管理", "删除提示词", "campus_ai_prompt_template",
                    String.valueOf(templateId), "templateId=" + templateId, true, null);
            return ResultVO.success();
        } catch (Exception e) {
            campusAuditLogService.record(request, "AI能力管理", "删除提示词", "campus_ai_prompt_template",
                    String.valueOf(templateId), "templateId=" + templateId, false, e.getMessage());
            return ResultVO.error(400, e.getMessage());
        }
    }

    @GetMapping("/call-log/list")
    public ResultVO<PageInfo<CampusAiCallLog>> listCallLogs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String featureCode,
            @RequestParam(required = false) String providerCode,
            @RequestParam(required = false) String callStatus) {
        return ResultVO.success(campusAiService.listCallLogs(pageNum, pageSize, featureCode, providerCode, callStatus));
    }
}
