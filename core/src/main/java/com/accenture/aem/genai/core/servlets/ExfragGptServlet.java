/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.accenture.aem.genai.core.servlets;

import com.accenture.aem.genai.core.services.ChatGptService;
import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;

/**
 * Servlet that writes some sample content into the response. It is mounted for
 * all resources of a specific Sling resource type. The
 * {@link SlingSafeMethodsServlet} shall be used for HTTP methods that are
 * idempotent. For write operations use the {@link SlingAllMethodsServlet}.
 */
@Component(service = { Servlet.class })
@SlingServletResourceTypes(
        resourceTypes="cq/experience-fragments/components/xfpage",
        methods=HttpConstants.METHOD_GET,
        selectors = "aipreview",
        extensions="json")
@ServiceDescription("Exfrag AI Servlet")
public class ExfragGptServlet extends SlingSafeMethodsServlet {

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

        String prompt = "Please rewrite so it so it specificailly targets age group 13-17";
        String promptQueryString = req.getParameter("prompt");
        if (StringUtils.isNotBlank(promptQueryString)) {
            prompt = promptQueryString;
        }

        String id = null;
        String idQueryString = req.getParameter("id");
        if (StringUtils.isNotBlank(idQueryString)) {
            id = idQueryString;
        }



        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("Status", "WorkInProgress");

        ResourceResolver resourceResolver = req.getResourceResolver();


        Map<String, String> exfragOriginalContent = chatGptService.getExfragOriginalContent(req.getResource().getPath());
        JsonElement jsonOriginalTree = gson.toJsonTree(exfragOriginalContent);
        responseObject.add("originalValues", jsonOriginalTree);

        Map<String, String> exfragGeneratedContent = chatGptService.useGptConvertStrings(prompt, exfragOriginalContent, id);

        JsonElement jsonGeneratedTree = gson.toJsonTree(exfragGeneratedContent);

        responseObject.add("generatedValues", jsonGeneratedTree);


        // process data so we don't need to process it much at the front end
        JsonArray displayTableData = new JsonArray();

        for (String key : exfragOriginalContent.keySet()) {
            JsonObject row = new JsonObject();
            row.addProperty("path", key);
            row.addProperty("originalValue", exfragOriginalContent.get(key));
            row.addProperty("generatedValue", exfragGeneratedContent.get(key));
            displayTableData.add(row);
        }

        responseObject.add("displayTableData", displayTableData);
        responseObject.addProperty("Status", "Success");
        resp.getWriter().write(responseObject.toString());

    }
}
