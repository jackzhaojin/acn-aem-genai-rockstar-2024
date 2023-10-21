package com.accenture.aem.genai.core.services.impl;
import com.accenture.aem.genai.core.dao.ChatGptDao;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.google.gson.Gson;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.jcr.Session;
import java.util.Map;
import java.util.TreeMap;

public class ChatGptServiceImplTest {

    @InjectMocks
    private ChatGptServiceImpl chatGptService;

    @Mock
    private ChatGptDao chatGptDao;

    @Mock
    private QueryBuilder queryBuilder;

    @Mock
    private Query query; // Mocked Query object

    @Mock
    private Session session;

    @Mock
    private ResourceResolverFactory resourceResolverFactory;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void testGetExfragOriginalContent() throws LoginException {
        // Mock your dependencies as needed
        ResourceResolver resourceResolver = Mockito.mock(ResourceResolver.class);

        // Define the behavior of the mocked dependencies
        // For example, when you call resourceResolverFactory.getAdministrativeResourceResolver(null), return the mock resourceResolver
        Mockito.when(resourceResolverFactory.getAdministrativeResourceResolver(null)).thenReturn(resourceResolver);
        Mockito.when(queryBuilder.createQuery(Mockito.any(), Mockito.any())).thenReturn(query);

        // Call the method you want to test
        Map<String, String> exfragOriginalContent = chatGptService.getExfragOriginalContent("/path/to/exfragVar", resourceResolver);
        Map<String, String> exfragOriginalContentResResolver = chatGptService.getExfragOriginalContent("/path/to/exfragVar", null);

        // Add your assertions to validate the result
        // For example, check if exfragOriginalContent contains expected values
    }

    @Test
    public void testUseGptConvertStrings() {
        // Define the input parameters and expected values for your test case
        String authorPrompt = "Author's prompt";
        Map<String, String> exfragOriginalContent = new TreeMap<>();
        exfragOriginalContent.put("/content/unit-test", "<p>test</p>");
        Gson gson;
        String chatSessionId = "123";
        Mockito.when(chatGptDao.generateResponseFromMessagesInString(Mockito.any())).thenReturn("<p>gen response</p>");

        // Mock your dependencies as needed
        // For example, mock the chatGptDao methods and their expected behavior

        // Call the method you want to test
        Map<String, String> generatedStrings = chatGptService.useGptConvertStrings(authorPrompt, exfragOriginalContent, chatSessionId);
        Map<String, String> generatedStringsNoChatId = chatGptService.useGptConvertStrings(authorPrompt, exfragOriginalContent, null);

        // Add your assertions to validate the result
        // For example, check if generatedStrings contain expected values
    }
}
