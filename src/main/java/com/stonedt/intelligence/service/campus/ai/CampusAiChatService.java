package com.stonedt.intelligence.service.campus.ai;

import java.util.function.Consumer;

public interface CampusAiChatService {

    CampusAiChatResponse chat(CampusAiChatRequest request);

    CampusAiChatResponse chatStreaming(CampusAiChatRequest request, StringBuilder streamOutput);

    CampusAiChatResponse chatStreaming(CampusAiChatRequest request, StringBuilder streamOutput, Consumer<String> chunkConsumer);
}
