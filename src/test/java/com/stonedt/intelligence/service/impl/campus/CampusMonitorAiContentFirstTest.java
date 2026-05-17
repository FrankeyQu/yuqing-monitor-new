package com.stonedt.intelligence.service.impl.campus;

import com.stonedt.intelligence.entity.campus.CampusMonitorResult;
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
    public void buildAiAnalysisTextUsesContentBeforeTitle() throws Exception {
        CampusMonitorServiceImpl service = newService();
        CampusMonitorResult result = new CampusMonitorResult();
        result.setTitle("新疆大学出现投诉");
        result.setContent("新疆大学相关部门回应，网传投诉已核实为误会，学生服务已恢复正常，后续将持续沟通。");

        Object analysisText = invokeBuildAiAnalysisText(service, result);

        Assert.assertEquals("content", getField(analysisText, "analysisBasis"));
        Assert.assertEquals(Boolean.TRUE, getField(analysisText, "contentUsable"));
        Assert.assertTrue(String.valueOf(getField(analysisText, "primaryText")).contains("已恢复正常"));
        Assert.assertEquals("新疆大学出现投诉", getField(analysisText, "secondaryTitle"));
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
        Method method = CampusMonitorServiceImpl.class
                .getDeclaredMethod("buildAiAnalysisText", CampusMonitorResult.class,
                        Class.forName("com.stonedt.intelligence.entity.campus.CampusIngestRecord"));
        method.setAccessible(true);
        return method.invoke(service, result, null);
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
