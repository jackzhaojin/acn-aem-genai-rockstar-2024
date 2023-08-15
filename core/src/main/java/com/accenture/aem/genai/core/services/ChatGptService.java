package com.accenture.aem.genai.core.services;

import java.util.Map;

public interface ChatGptService {

    public Map<String, String> getExfragOriginalContent(String pathToExfragVar);
    Map<String, String> useGptConvertStrings(String authorPrompt, Map<String, String> exfragOriginalContent, String chatSessionId);
}
