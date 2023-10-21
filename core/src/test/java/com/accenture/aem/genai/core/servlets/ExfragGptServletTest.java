package com.accenture.aem.genai.core.servlets;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import com.accenture.aem.genai.core.services.impl.ChatGptServiceImpl;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.accenture.aem.genai.core.services.ChatGptService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.mockito.MockitoAnnotations;

@ExtendWith(AemContextExtension.class)
class ExfragGptServletTest {

    @InjectMocks
    private ExfragGptServlet exfragGptServlet;

    @Mock
    ChatGptService chatGptService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    void testDoGet() throws ServletException, IOException {
        // Create a mock request and response
        SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
        SlingHttpServletResponse response = mock(SlingHttpServletResponse.class);

        // Create a mock resource
        Resource resource = mock(Resource.class);
        when(request.getResource()).thenReturn(resource);

        // Set up the request parameters
        when(request.getParameter("prompt")).thenReturn("Please rewrite so it so it specifically targets age group 13-17");
        when(request.getParameter("id")).thenReturn("123");

        // Create a StringWriter to capture the response output
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // Create a mock ChatGptService
        ChatGptService chatGptService = mock(ChatGptService.class);
        Map<String, String> exfragOriginalContent = new HashMap<>();
        exfragOriginalContent.put("key1", "value1");
        when(chatGptService.getExfragOriginalContent(Mockito.anyString(), Mockito.any())).thenReturn(exfragOriginalContent);

        exfragGptServlet.doGet(request, response);

        // Verify that the response output is valid JSON
        writer.flush();
        String output = stringWriter.toString();
        JsonObject jsonObject = new Gson().fromJson(output, JsonObject.class);
        assertNotNull(jsonObject);
    }
}