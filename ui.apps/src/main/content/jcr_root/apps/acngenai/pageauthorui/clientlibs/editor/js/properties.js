(function (window, document, Granite, $) {
    "use strict";
    // prevent listeners from firing twice
    var init = false;

    var latestResponse = {};
    var currentPagePath = "";
    var chatId = null;

    function bindGenerateContentButtonToPrompt() {

        function getProomptUrl() {
            var url = document.baseURI;
// Create a new URL object
            var parsedUrl = new URL(url);

// Get the value of the "item" query parameter
            var itemValue = parsedUrl.searchParams.get("item");

// Remove the ".html" extension from the path
            let pathWithoutExtension = parsedUrl.pathname.replace(".html", "");

// Remove unwanted segment from the path
            var finalPath = pathWithoutExtension.replace("/mnt/overlay/wcm/core/content/sites/properties", "");

            currentPagePath = itemValue;
// Combine the path, item value, and desired suffix
            finalPath += itemValue + "/jcr:content.aipreview.json";

            return finalPath;

        }

        function generateTableFromJSON(data) {
            // Get the table container element
            var $tableContainer = $(".cpt-chat-table");

            // Clear the table container
            $tableContainer.empty();

            // Create the table element
            var $table = $("<table>");

            // Create the table header
            var $thead = $("<thead>");
            var $headerRow = $("<tr>");
            var headers = ["Value", "Generated Value"];

            // Add the headers to the header row
            headers.forEach(function (headerText) {
                var $th = $("<th>").text(headerText);
                $headerRow.append($th);
            });

            // Append the header row to the table header
            $thead.append($headerRow);

            // Create the table body
            var $tbody = $("<tbody>");

            // Iterate over the JSON data and create table rows
            data.forEach(function (item) {
                var $row = $("<tr>");

                // Create the cells for each row based on the column mapping
                var $pathCell = $("<td>").text(item.path);
                $row.append($pathCell);

                var $originalValueCell = $("<td>").text(item.originalValue);
                $row.append($originalValueCell);

                var $generatedValueCell = $("<td>").text(item.generatedValue);
                $row.append($generatedValueCell);

                // Append the row to the table body
                $tbody.append($row);
            });

            // Append the table header and body to the table
            $table.append($thead);
            $table.append($tbody);

            // Append the table to the table container
            $tableContainer.append($table);
        }


        $(".cpt-generate-content").on("click", function () {

            let titlePending = false;
            let descriptionPending = false;



            // Function to enable the button when both calls are successful
            function enableButtonIfBothCallsSucceeded() {
                if (!titlePending && !descriptionPending) {
                    $(".cpt-generate-content").prop("disabled", false);
                    $(".cpt-save-content").prop("disabled", false);
                    $(".cpt-generate-content").text("Generate Content (previous generation successful");
                }
            }
            function getPromptValue(promptValue) {
                var requestData = {
                    prompt: promptValue
                };
                $.ajax({
                    url: getProomptUrl(),
                    method: "GET",
                    data: requestData,
                    success: function (response) {
                        // Handle the API response
                        console.log(response);
                        if (response) {
                            if (promptValue == 'title') {
                                titlePending = false;
                                if ($('.cpt-title-output').length === 0) {
                                    // Create the .cpt-title-output div
                                    var titleOutputTr = $('<tr class="cpt-title-output _coral-Table-row" is="coral-table-row"></tr>');
                                    $('.cpt-chat-table tbody tr').first().after(titleOutputTr);

                                }
                                // Output the string inside the .cpt-title-output div
                                var title = response; // Replace with your actual title
                                $('.cpt-title-output').html('<td is="coral-table-cell" class="_coral-Table-cell">Title</td><td is="coral-table-cell" class="_coral-Table-cell">' + title + '</td>');

                            }
                            if (promptValue == 'description') {
                                descriptionPending = false;
                                if ($('.cpt-description-output').length === 0) {
                                    // Create the .cpt-description-output div
                                    var descriptionOutputTr = $('<tr class="cpt-description-output _coral-Table-row" is="coral-table-row"></tr>');
                                    $('.cpt-chat-table tbody tr').first().after(descriptionOutputTr);

                                }
                                // Output the string inside the .cpt-description-output div
                                var description = response; // Replace with your actual description
                                $('.cpt-description-output').html('<td is="coral-table-cell" class="_coral-Table-cell">Description</td><td is="coral-table-cell" class="_coral-Table-cell">' + description + '</td>');
                            }

                            $("td:contains('There is no item.')").closest("tr").remove();
                            enableButtonIfBothCallsSucceeded();
                        }

                    },
                    error: function (xhr, status, error) {
                        // Handle the error
                        console.error(error);
                        $(".cpt-generate-content").prop("disabled", false);
                        $(".cpt-save-content").prop("disabled", false);
                        $(".cpt-generate-content").text("Generate Content (previous generation failed");
                    }
                });
                return requestData;
            }

            // if continue chat is checked, add id
            if ($('.cpt-generate-title input').is(':checked')) {
                titlePending = true;
                var requestDataTitle = getPromptValue('title');

                $(".cpt-generate-content").prop("disabled", true); // Disable the button
                $(".cpt-save-content").prop("disabled", true); // Disable the button
                $(".cpt-generate-content").text("Processing"); // Change the button text
            }
            // if continue chat is checked, add id
            if ($('.cpt-generate-description input').is(':checked')) {
                descriptionPending = true;
                var requestDataDescription = getPromptValue('description');

                $(".cpt-generate-content").prop("disabled", true); // Disable the button
                $(".cpt-save-content").prop("disabled", true); // Disable the button
                $(".cpt-generate-content").text("Processing"); // Change the button text
            }
        });

    }

    function saveCheckedContent() {
        function stripLastSegment(path) {
            var lastIndex = path.lastIndexOf("/");
            if (lastIndex !== -1) {
                return path.substring(0, lastIndex);
            }
            return path;
        }

        function parseHTMLResponse(response) {
            // Create a temporary jQuery element
            var $tempElement = $('<div></div>');

            // Set the HTML content of the temporary element
            $tempElement.html(response);

            // Find the desired div by its ID
            var $messageDiv = $tempElement.find('#Message');

            // Check if the div element exists
            if ($messageDiv.length > 0) {
                // Get the text content of the div
                var message = $messageDiv.text().trim();

                // Return the message
                return message;
            }

            // Return null if the div is not found
            return null;
        }


        $(".cpt-save-content").click(function () {

            var updatePropertyMap = {
            };

            if ($('.cpt-generate-title input').is(':checked')) {
                updatePropertyMap['jcr:title'] = $('.cpt-title-output td:last-child').text();
            }
            if ($('.cpt-generate-description input').is(':checked')) {
                updatePropertyMap['jcr:description'] = $('.cpt-description-output td:last-child').text();
            }

            let formData = '';
            let count = 0;
            for (let jsonToUpdate in updatePropertyMap) {

                let propertyKey = jsonToUpdate;
                // propertyKey = propertyKey.slice(0, propertyKey.lastIndexOf("/")) + "@" + propertyKey.slice(propertyKey.lastIndexOf("/") + 1);
                let generatedValue = updatePropertyMap[jsonToUpdate];
                if (count !== 0) {
                    formData += '&';
                }

                // remove extra quotes from generated value
                let cleanedValue = generatedValue.replace(/^"|"$/g, '');

                formData += propertyKey + '=' + cleanedValue;
                count ++;
            }

            // adding in jcr title to match exfrag var name
            // var uiFriendlyTitle = convertToTitleCase(destName);
            // formData += "&." + '/jcr:content/jcr:title' + '=' + uiFriendlyTitle;
            // var formData = '';

            $.ajax({
                url: currentPagePath + "/jcr:content",
                method: "POST",
                data: formData,
                success: function(response) {
                    // Handle the response
                    $('.cpt-save-content').text("Save Checked Content (Previous save successful)");
                    console.log(response);
                },
                error: function(error) {
                    // Handle any errors
                    $('.cpt-save-content').text("Save Checked Content (Previous save failed)");
                    console.error(error);
                }
            });
        });
    }

    $(document).on("foundation-contentloaded", function (e) {
        if (init) {
            return;
        }
        init = true;
        bindGenerateContentButtonToPrompt();

        saveCheckedContent();

    });
})(window, document, Granite, Granite.$);
