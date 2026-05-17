package com.stonedt.intelligence.service.campus.ai;

public interface CampusAiChatService {

    CampusAiChatResponse chat(CampusAiChatRequest request);

    CampusAiChatResponse chatStreaming(CampusAiChatRequest request, StringBuilder streamOutput);
}
