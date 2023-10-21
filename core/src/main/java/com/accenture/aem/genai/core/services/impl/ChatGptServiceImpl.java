package com.accenture.aem.genai.core.services.impl;

import com.accenture.aem.genai.core.dao.ChatGptDao;
import com.accenture.aem.genai.core.services.ChatGptService;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.RandomUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Component(service = ChatGptService.class, immediate = true)
public class ChatGptServiceImpl implements ChatGptService {


    public static final String QUERY_GROUP = "0_group.";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, JsonArray> messageChatLog = new TreeMap<>();

    @Reference
    private QueryBuilder queryBuilder;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    public static final String ROLE = "role";
    public static final String CONTENT = "content";

    @Reference
    ChatGptDao chatGptDao;

    public Map<String, String> getExfragOriginalContent(String pathToExfragVar, ResourceResolver resourceResolver) {

        String[] propertiesToScan = {"text", "jcr:description"};
        return searchProperties(pathToExfragVar, resourceResolver);
    }

    public Map<String, String> useGptConvertStrings(String authorPrompt, Map<String,
            String> exfragOriginalContent, String chatSessionId) {

        if (exfragOriginalContent == null) {
            return null;
        }
        boolean newChat = false;
        if (chatSessionId == null) {
            chatSessionId = String.valueOf(RandomUtils.nextInt());
            newChat = true;
        }

        Map<String, String> generatedStrings = new TreeMap<String, String>();

        for (String exfragContentPath : exfragOriginalContent.keySet()) {

            JsonArray messageJsonArray = new JsonArray();
            // set up messages or get existing one
            if (chatSessionId != null && messageChatLog.get(chatSessionId + exfragContentPath) != null) {
                messageJsonArray = messageChatLog.get(chatSessionId + exfragContentPath);
            }

            JsonObject message;
            // role has option system, user, or assistant.
            if (newChat) {
                message = new JsonObject();
                message.addProperty(ROLE, "system");
                message.addProperty(CONTENT, "You are a helpful assistant for generating personalized content");
                messageJsonArray.add(message);

                message = new JsonObject();
                message.addProperty(ROLE, "user");
                message.addProperty(CONTENT, " \n" +
//                        "Please do not change the content structure between the prompt and the response.\n" +
                getChatGptMessageFromContent(authorPrompt, exfragOriginalContent.get(exfragContentPath)));
            } else {
                message = new JsonObject();
                message.addProperty(ROLE, "user");
                message.addProperty(CONTENT, authorPrompt);
            }
            messageJsonArray.add(message);
/*
            message = new JsonObject();
            // role has option system, user, or assistant.
            message.addProperty(ROLE, "user");
            message.addProperty(CONTENT, getChatGptMessageFromContent(authorPrompt, exfragOriginalContent.get(exfragContentPath)));
            messageJsonArray.add(message);
*/
            String chatGptResponse = chatGptDao.generateResponseFromMessagesInString(messageJsonArray);
            chatGptResponse = sanitizeResponse(chatGptResponse);
            logger.info("Message received from chat after processing: " + chatGptResponse);
            // role has option system, user, or assistant.
            message = new JsonObject();
            message.addProperty(ROLE, "assistant");
            message.addProperty(CONTENT, chatGptResponse);
            messageJsonArray.add(message);
            generatedStrings.put(exfragContentPath, chatGptResponse);
            if (messageChatLog.get(chatSessionId + exfragContentPath) == null) {
                messageChatLog.put(chatSessionId + exfragContentPath, messageJsonArray);
            } else {
                messageChatLog.put(chatSessionId + exfragContentPath, messageJsonArray);
            }
            generatedStrings.put("id", chatSessionId);
        }


        return generatedStrings;
    }

    /**
     * Parsing response before storing in chat history and returning to controller
     * @param chatGptResponse unparsed response
     * @return parsed response
     */
    private String sanitizeResponse(String chatGptResponse) {
        // removing surrounding quotes if they exist
        chatGptResponse = chatGptResponse.replaceAll("^\"|\"$", "");

        // replace \\ with \n
        chatGptResponse = chatGptResponse.replaceAll("\\\\n", "");
        chatGptResponse = chatGptResponse.replaceAll("\\\\r", "");
        return chatGptResponse;
    }

    private static String getChatGptMessageFromContent(String prompt, String content) {

//        return prompt + "In the text below. Please preserve all HTML markup if there are any. " +
//                "Please Do not convert to bullet form unless the original message was in bullet \n" + content;


        StringBuffer sb = new StringBuffer();
        sb.append("I am a content copy writer who needs to write personalized content.")
                .append("Below I will provide my personalization \"Prompt\" and the \"Content to be Personalized\" that needs to be personalized. \n")
                .append("\nIf my \"Content to be Personalized\" in this chat has HTML markup, please respond with the same HTML markup structure. For example, if <p> exist in the prompt, keep the <p> in the response.")
                .append("\nAlso please do not add quotes or any rational around the response, only respond back with personalized content! Also please keep the response around the same length as the Content to be Personalized.")
                .append("\nPrompt: " + prompt + ", please keep the content shorter")
                .append("\nContent to be Personalized: " + content);
        return sb.toString();
    }


    private Map<String, String> searchProperties(String path, ResourceResolver resourceResolver) {
        // Specify the properties you want to search for
        String[] properties = {"text", "jcr:description"};

        Map<String, String> returnMap = new TreeMap<>();

        try {
            // Get the resource resolver using the ResourceResolverFactory if resource resolver is null
            if (resourceResolver == null) {
                Map<String, Object> authInfo = Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, "my-service");
                resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
            }

            // Get the session from the resource resolver
            Session session = resourceResolver.adaptTo(Session.class);

            Map<String, String> params = new TreeMap<String, String>();
            params.put("type", "nt:unstructured");
            params.put("path", path);
            params.put(QUERY_GROUP + "p.or", "true");
            params.put("p.offset", "0");
            params.put("p.limit", "-1");
            addPropertyParam(properties, params);
            Query query = queryBuilder.createQuery(PredicateGroup.create(params),
                    resourceResolver.adaptTo(Session.class));
            SearchResult result = query.getResult();
            // Create the QueryBuilder query using PredicateGroup

            if (result != null) {

                // Process the result
                for (Hit hit : result.getHits()) {
                    String hitPath = hit.getPath();
                    Resource hitResource = resourceResolver.getResource(hitPath);

                    // Access the properties
                    ValueMap propertiesMap = hitResource.adaptTo(ValueMap.class);
                    Map<String, Object> propertyValues = new HashMap<>();

                    for (String property : properties) {
                        Object value = propertiesMap.get(property);
                        if (value != null && value.toString() != "") {
                            // other filters, filtering out actions for now, can be refactored to osgi configured later
                            if (!hitPath.contains("actions")) {
                                returnMap.put(hitPath + "/" + property, value.toString());
                            }
                        }
                    }
                }
            }

            // Close the session and resource resolver
            if (session != null) {
                session.logout();
            }
            resourceResolver.close();


        } catch (Exception e) {
            logger.error("Exception Occurred in searchProperties", e);
            throw new RuntimeException(e);
        }
        return returnMap;
    }

    private void addPropertyParam(String[] properties, Map<String, String> map) {
        /*
            0_group.1_property=jcr:description
            0_group.1_operation=exists
            0_group.2_property=text
            0_group.2_operation=exists
            public static final String QUERY_GROUP = "0_group.";

         */
        for (int i = 0; i < properties.length; i++) {
            int queryNumber = i + 1;
            map.put(QUERY_GROUP  + queryNumber + "_property", properties[i]);
            map.put(QUERY_GROUP  + queryNumber + "_operation", "exists");
        }
    }

}
