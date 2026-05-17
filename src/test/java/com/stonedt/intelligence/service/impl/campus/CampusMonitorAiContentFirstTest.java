package com.stonedt.intelligence.service.impl.campus;

import com.stonedt.intelligence.entity.campus.CampusMonitorResult;
import com.stonedt.intelligence.entity.campus.CampusMonitorTask;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CampusMonitorAiContentFirstTest {

    @Test
    public void buildAiAnalysisTextUsesContentWhenContentMatchesBetter() throws Exception {
        CampusMonitorServiceImpl service = newService();
        CampusMonitorResult result = new CampusMonitorResult();
        result.setTitle("新疆大学出现投诉");
        result.setContent("新疆大学相关部门回应，网传投诉已核实为误会，学生服务已恢复正常，后续将持续沟通。");
        result.setMatchedSubjects("新疆大学");

        Object analysisText = invokeBuildAiAnalysisText(service, result);

        Assert.assertEquals("content", getField(analysisText, "analysisBasis"));
        Assert.assertEquals(Boolean.TRUE, getField(analysisText, "contentUsable"));
        Assert.assertTrue(String.valueOf(getField(analysisText, "primaryText")).contains("已恢复正常"));
        Assert.assertEquals("新疆大学出现投诉", getField(analysisText, "secondaryTitle"));
    }

    @Test
    public void buildAiAnalysisTextUsesContentWhenSignalsTie() throws Exception {
        CampusMonitorServiceImpl service = newService();
        CampusMonitorResult result = new CampusMonitorResult();
        result.setTitle("新疆大学食堂服务调整");
        result.setContent("新疆大学食堂服务调整后，学生反馈整体就餐秩序平稳。");
        result.setMatchedSubjects("新疆大学");
        result.setMatchedKeywords("食堂");

        Object analysisText = invokeBuildAiAnalysisText(service, result);

        Assert.assertEquals("content", getField(analysisText, "analysisBasis"));
        Assert.assertTrue(String.valueOf(getField(analysisText, "selectionReason")).contains("正文优先"));
    }

    @Test
    public void buildAiAnalysisTextUsesTitleWhenTitleMatchesBetterThanReplyContent() throws Exception {
        CampusMonitorServiceImpl service = newService();
        CampusMonitorResult result = new CampusMonitorResult();
        result.setTitle("新疆大学食堂食品安全投诉");
        result.setContent("确实应该重视，已经转给同学们看了，希望后面能有更明确的处理说明。");
        result.setMatchedSubjects("新疆大学");
        result.setMatchedKeywords("食堂");
        result.setMatchedNegativeWords("投诉");
        CampusMonitorTask task = new CampusMonitorTask();
        task.setMonitorSubject("新疆大学");
        task.setKeywords("食堂,食品安全");
        task.setNegativeWords("投诉");

        Object analysisText = invokeBuildAiAnalysisText(service, result, task);

        Assert.assertEquals("title", getField(analysisText, "analysisBasis"));
        Assert.assertEquals("新疆大学食堂食品安全投诉", getField(analysisText, "primaryText"));
        Assert.assertTrue(String.valueOf(getField(analysisText, "selectionReason")).contains("标题包含更多"));
    }

    @Test
    public void buildAiAnalysisTextFallsBackToTitleWhenContentRepeatsTitle() throws Exception {
        CampusMonitorServiceImpl service = newService();
        CampusMonitorResult result = new CampusMonitorResult();
        result.setTitle("新疆大学出现投诉");
        result.setContent("新疆大学出现投诉");

        Object analysisText = invokeBuildAiAnalysisText(service, result);

        Assert.assertEquals("title", getField(analysisText, "analysisBasis"));
        Assert.assertEquals(Boolean.FALSE, getField(analysisText, "contentUsable"));
        Assert.assertEquals("新疆大学出现投诉", getField(analysisText, "primaryText"));
    }

    @Test
    public void titleOnlyNegativeSentimentDoesNotEscalateRiskWithoutAiRiskDecision() throws Exception {
        CampusMonitorServiceImpl service = newService();
        CampusMonitorResult result = new CampusMonitorResult();
        result.setRiskLevel("normal");

        Object analysis = newAiResultAnalysis("title", null);
        String riskLevel = invokeResolveAiAnalysisRiskLevel(service, result, analysis, "negative");

        Assert.assertEquals("normal", riskLevel);
    }

    @Test
    public void contentBasedNegativeSentimentStillEscalatesRisk() throws Exception {
        CampusMonitorServiceImpl service = newService();
        CampusMonitorResult result = new CampusMonitorResult();
        result.setRiskLevel("normal");

        Object analysis = newAiResultAnalysis("content", null);
        String riskLevel = invokeResolveAiAnalysisRiskLevel(service, result, analysis, "negative");

        Assert.assertEquals("concern", riskLevel);
    }

    private CampusMonitorServiceImpl newService() {
        PlatformTransactionManager transactionManager = new PlatformTransactionManager() {
            @Override
            public TransactionStatus getTransaction(TransactionDefinition definition) {
                return new SimpleTransactionStatus();
            }

            @Override
            public void commit(TransactionStatus status) {
            }

            @Override
            public void rollback(TransactionStatus status) {
            }
        };
        return new CampusMonitorServiceImpl(null, null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null, transactionManager);
    }

    private Object invokeBuildAiAnalysisText(CampusMonitorServiceImpl service,
                                             CampusMonitorResult result) throws Exception {
        return invokeBuildAiAnalysisText(service, result, null);
    }

    private Object invokeBuildAiAnalysisText(CampusMonitorServiceImpl service,
                                             CampusMonitorResult result,
                                             CampusMonitorTask task) throws Exception {
        Method method = CampusMonitorServiceImpl.class
                .getDeclaredMethod("buildAiAnalysisText", CampusMonitorResult.class,
                        Class.forName("com.stonedt.intelligence.entity.campus.CampusIngestRecord"),
                        CampusMonitorTask.class);
        method.setAccessible(true);
        return method.invoke(service, result, null, task);
    }

    private String invokeResolveAiAnalysisRiskLevel(CampusMonitorServiceImpl service,
                                                    CampusMonitorResult result,
                                                    Object analysis,
                                                    String sentiment) throws Exception {
        Method method = CampusMonitorServiceImpl.class
                .getDeclaredMethod("resolveAiAnalysisRiskLevel", CampusMonitorResult.class,
                        Class.forName("com.stonedt.intelligence.entity.campus.CampusIngestRecord"),
                        analysis.getClass(), String.class);
        method.setAccessible(true);
        return (String) method.invoke(service, result, null, analysis, sentiment);
    }

    private Object newAiResultAnalysis(String analysisBasis, String riskLevel) throws Exception {
        Class<?> clazz = Class.forName("com.stonedt.intelligence.service.impl.campus.CampusMonitorServiceImpl$AiResultAnalysis");
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object analysis = constructor.newInstance();
        setField(analysis, "analysisBasis", analysisBasis);
        setField(analysis, "riskLevel", riskLevel);
        return analysis;
    }

    private Object getField(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
