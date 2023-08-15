package com.accenture.aem.genai.core.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Interface for the ChatGPT DAO (Data Access Object) that provides methods to interact with the ChatGPT API.
 */
public interface ChatGptDao {
    /**
     * Generates a response from the ChatGPT API based on the given prompt.
     *
     * @param prompt the input prompt for generating the response
     * @return the generated response as a string
     */
    String generateSimpleResponse(String prompt);

    JsonObject generateResponseFromMessagesInJson(JsonArray messageJsonArray);

    String generateResponseFromMessagesInString(JsonArray messageJsonArray);
}
