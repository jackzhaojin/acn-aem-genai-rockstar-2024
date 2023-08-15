package com.accenture.aem.genai.core.services;

import org.apache.sling.api.resource.ResourceResolver;

import java.util.Map;

public interface ChatGptService {

    /**
     * Get a list of content back from the experience fragment variation
     * @param pathToExfragVar experience fragment variation path
     * @param resourceResolver resource resolver used to search
     * @return
     */
    Map<String, String> getExfragOriginalContent(String pathToExfragVar, ResourceResolver resourceResolver);

    /**
     * Leverages chat gpt to generate prompt from user
     * @param authorPrompt what author typed in the prompt
     * @param exfragOriginalContent exfrag content map
     * @param chatSessionId populate if existing chat session should be resumed
     * @return
     */
    Map<String, String> useGptConvertStrings(String authorPrompt, Map<String, String> exfragOriginalContent, String chatSessionId);
}
