package com.accenture.aem.genai.core.dao.impl;

import com.accenture.aem.genai.core.dao.ChatGptDao;
import com.adobe.granite.crypto.CryptoException;
import com.adobe.granite.crypto.CryptoSupport;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Designate(ocd = ChatGptDaoImpl.ChatGptDaoConfiguration.class)
@Component(service = ChatGptDao.class, immediate = true)
public class ChatGptDaoImpl implements ChatGptDao {
    // Configurable fields
    // encrypted per aem instance based on its hmac, for it to work everywhere we'll need to make this an osgi configuration and configure it per env
    private static final String DEFAULT_SECRET_KEY_ENCRYPTED = "{94ed19bc7bd4a77f3319bcb90f32f0ca3672deaa2897f6c4992c4915c4c4554f670758d7bc9828d9af1dea55c25f0a1635323d79ba72f068c324734bed7af5291da6e05c584e4f4d9b1ed2dae21fef32}";
    //    private static final String API_ENDPOINT = "https://api.openai.com/v1/completions";
    private static final String API_ENDPOINT = "https://api.openai.com/v1/chat/completions";
    //    private static final String API_ENDPOINT = "https://api.openai.com/v1/engines/davinci-codex/completions";
    private static final int MAX_TOKENS = 1000;

    public static final String CHAT_GPT_MODEL = "gpt-3.5-turbo"; // working chat
    //    public static final String CHAT_GPT_MODEL = "text-davinci-003"; // completion
    public static final String ROLE = "role";
    public static final String CONTENT = "content";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Reference
    private CryptoSupport cryptoSupport;

    private String secretKey;


    /**
     * Object class definition for chat gpt dao for osgi configuration
     */
    @ObjectClassDefinition(
            name = "Chat Gpt Dao Configuration",
            description = "Configuration for the Chat Gpt Dao service"
    )
    public @interface ChatGptDaoConfiguration {

        @AttributeDefinition(
                name = "Secret Key",
                description = "The secret key for the Chat Gpt Dao service, should be encrypted via" +
                        "crypto service first",
                type = AttributeType.PASSWORD,
                defaultValue = DEFAULT_SECRET_KEY_ENCRYPTED,
                required = true
        )
        String secretKey() default DEFAULT_SECRET_KEY_ENCRYPTED;
    }

    @Activate
    protected void activate(ChatGptDaoConfiguration config) {
        secretKey = config.secretKey();
        if (cryptoSupport.isProtected(secretKey)) {
            try {
                secretKey = cryptoSupport.unprotect(secretKey);
            } catch (CryptoException e) {
                logger.error("Exception while decrypting Secret Key", e);
            }
        }
    }


    public String generateResponseFromMessagesInString(JsonArray messageJsonArray) {
        JsonObject jsonResponseObject = generateResponseFromMessagesInJson(messageJsonArray);


        // simple json parser - getting first choice's message
        // Extract the generated text from the JSON response
        // You may want to process the JSON response in a more robust manner
        // Here, we're simply returning the entire JSON response as a string
        return jsonResponseObject.getAsJsonArray("choices").get(0).getAsJsonObject()
                .getAsJsonObject("message").getAsJsonPrimitive("content").toString();
    }
    public JsonObject generateResponseFromMessagesInJson(JsonArray messageJsonArray) {

        // HTTP client instance
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            // Create an HTTP POST request with the necessary headers
            HttpPost httpPost = new HttpPost(API_ENDPOINT);
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Authorization", "Bearer " + secretKey);


            // Create the request payload using Gson
            JsonObject payload = new JsonObject();
            payload.addProperty("model", CHAT_GPT_MODEL);
            payload.addProperty("max_tokens", MAX_TOKENS);

            // from parameter
            payload.add("messages", messageJsonArray);

            // Convert the payload to a JSON string
            String jsonPayload = new Gson().toJson(payload);

            logger.info("Sending JSON Payload: " + jsonPayload);

            // Set the JSON payload in the HTTP request
            StringEntity entity = new StringEntity(jsonPayload, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);

            // Execute the request and retrieve the response
            CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            String stringResponse = EntityUtils.toString(responseEntity);

            // Close the response
            response.close();
            JsonObject responseJson = new JsonParser().parse(stringResponse).getAsJsonObject();
            logger.info("Received JSON Payload: " + stringResponse);

            // Extract the generated text from the JSON response
            // You may want to process the JSON response in a more robust manner
            // Here, we're simply returning the entire JSON response as a string
            return responseJson;
        } catch (ClientProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // Ensure that the HTTP client is always closed
            try {
                httpClient.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String generateSimpleResponse(String prompt) {

        // setting main message
        JsonArray messageJsonArray = new JsonArray();
        JsonObject mainMessage = new JsonObject();
        // role has option system, user, or assistant.
        mainMessage.addProperty(ROLE, "user");
        mainMessage.addProperty(CONTENT, prompt);
        messageJsonArray.add(mainMessage);

        // calling shared parent method
        return generateResponseFromMessagesInString(messageJsonArray);
    }
}
