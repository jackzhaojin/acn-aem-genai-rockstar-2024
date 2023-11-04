package com.accenture.aem.genai.core.servlets;

import com.accenture.aem.genai.core.services.ChatGptService;
import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;

@Component(service = { Servlet.class })
@SlingServletResourceTypes(
        resourceTypes="wcm/foundation/components/basicpage/v1/basicpage",
        methods= HttpConstants.METHOD_GET,
        selectors = "aipreview",
        extensions="json")
@ServiceDescription("Page AI Servlet")
public class PageRefinementServlet extends SlingSafeMethodsServlet {


    private static final long serialVersionUID = 1L;

    @Reference
    private ChatGptService chatGptService;

    @Override
    protected void doGet(final SlingHttpServletRequest req,
                         final SlingHttpServletResponse resp) throws ServletException, IOException {
        final Resource resource = req.getResource();
        resp.setContentType("application/json");
        JsonParser parser = new JsonParser();
        Gson gson = new Gson();

        String prompt = "Please summerize SEO Content for this page";
        String promptQueryString = req.getParameter("prompt");
        if (StringUtils.isNotBlank(promptQueryString)) {
            prompt = promptQueryString;
        }


        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("Status", "WorkInProgress");

        ResourceResolver resourceResolver = req.getResourceResolver();


        String exfragOriginalContent = chatGptService.getPageSummary(prompt, req.getResource().getPath(), req.getResourceResolver(), req);
        JsonElement jsonOriginalTree = gson.toJsonTree(exfragOriginalContent);

        resp.getWriter().write(jsonOriginalTree.toString());

    }
}
