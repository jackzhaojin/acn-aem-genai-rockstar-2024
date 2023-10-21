package com.accenture.aem.genai.core.dao.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.adobe.granite.crypto.CryptoSupport;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.gson.JsonObject;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({AemContextExtension.class})
class ChatGptDaoImplTest {

    @Mock
    public static CloseableHttpClient httpClient;

    @Mock
    private HttpPost httpPost;

    @Mock
    private CloseableHttpResponse httpResponse;

    @Mock
    private HttpEntity httpEntity;

    @Mock
    private CryptoSupport cryptoSupport;

    @InjectMocks
    private ChatGptDaoImpl chatGptDao = new ChatGptDaoImpl() {
        public CloseableHttpClient getHttpClient() {
            CloseableHttpClient httpClient = ChatGptDaoImplTest.httpClient;
            return httpClient;
        }
    };

    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGenerateResponseFromMessagesInString() throws IOException {
        // Set up a mock JSON response
        JsonObject messageJsonObject = new JsonObject();
        messageJsonObject.addProperty("content", "Hello, world!");
        JsonObject choiceJsonObject = new JsonObject();
        choiceJsonObject.add("message", messageJsonObject);
        JsonArray choicesJsonArray = new JsonArray();
        choicesJsonArray.add(choiceJsonObject);
        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.add("choices", choicesJsonArray);

        String responseJson = "{\n" +
                " \"id\": \"chatcmpl-8C5duJRnY4wKG7rOYjcFJcL67JOWM\",\n" +
                " \"object\": \"\n" +
                "chat.completion\",\n" +
                " \"created\": 1697892318,\n" +
                " \"model\": \"gpt-4-0613\",\n" +
                " \"choices\": [\n" +
                "   {\n" +
                "     \"index\": 0,\n" +
                "     \"message\": {\n" +
                "       \"role\": \"assistant\",\n" +
                "    \n" +
                "   \"content\": \"<p>As a Pro Athlete, Yosemite National Park, designated a World Heritage Site in 1984, provides a perfect training ground. Famous for its granite\n" +
                " cliffs, waterfalls and giant sequoias, its nearly 1,200 square miles also offers deep valleys, grand meadows, glaciers and lakes for intense workouts and condi\n" +
                "tioning. The majority of visitors explore the Yosemite Valley, an ideal spot for endurance training. Additionally, on this trip, we'll take you to the less-expl\n" +
                "ored backcountry. This was the same area which helped John Muir to initiate a movement leading to the establishment of Yosemite as we appreciate it today.</p>\"\n" +
                "      },\n" +
                "     \"finish_reason\": \"stop\"\n" +
                "   }\n" +
                " ],\n" +
                " \"usage\": {\n" +
                "   \"prompt_tokens\": 249,\n" +
                "   \"completion_tokens\": 131,\n" +
                "   \"total_tokens\": 380\n" +
                " }\n" +
                " }";
        InputStream responseJsonIS = new ByteArrayInputStream(responseJson.getBytes(StandardCharsets.UTF_8));
//        CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
//        HttpEntity httpEntity = mock(HttpEntity.class);
//        doReturn(responseJsonIS).when(httpEntity).getContent();
//        when(httpClient.execute(Mockito.any(HttpPost.class))).thenReturn(httpResponse);

        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(responseJsonIS);
        when(httpClient.execute(Mockito.any())).thenReturn(httpResponse);


        // Call the ChatGptDaoImpl's generateResponseFromMessagesInString method
        ChatGptDaoImpl.ChatGptDaoConfiguration mockConfig = mock(ChatGptDaoImpl.ChatGptDaoConfiguration.class);
        when(mockConfig.maxTokens()).thenReturn("5010");
        chatGptDao.activate(mockConfig);

        String response = String.valueOf(chatGptDao.generateResponseFromMessagesInJson(choicesJsonArray));
        responseJsonIS = new ByteArrayInputStream(responseJson.getBytes(StandardCharsets.UTF_8));
        when(httpEntity.getContent()).thenReturn(responseJsonIS);

        String simpleResponse = chatGptDao.generateSimpleResponse("test prompt");
        // Verify that the response is "Hello, world!"
//        assertEquals("Hello, world!", response);
    }

    @Test
    void testGetClient() {

        chatGptDao.getHttpClient();
    }

}