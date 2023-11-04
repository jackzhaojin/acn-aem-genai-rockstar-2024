(function (window, document, Granite, $) {
    "use strict";
    // prevent listeners from firing twice
    var init = false;

    var latestResponse = {};
    var currentExfragVar = "";
    var chatId = null;

    function bindGenerateContentButtonToPrompt() {

        function getProomptUrl() {
            var url = document.baseURI;
// Create a new URL object
            var parsedUrl = new URL(url);

// Get the value of the "item" query parameter
            var itemValue = parsedUrl.searchParams.get("item");

// Remove the ".html" extension from the path
            var pathWithoutExtension = parsedUrl.pathname.replace(".html", "");

// Remove unwanted segment from the path
            var finalPath = pathWithoutExtension.replace("/mnt/overlay/wcm/core/content/sites/properties", "");

            currentExfragVar = itemValue;
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
                                    var titleOutputDiv = $('<div class="cpt-title-output"></div>');
                                    $('.cpt-generate-content').after(titleOutputDiv);

                                    // Output the string inside the .cpt-title-output div
                                    var title = response; // Replace with your actual title
                                    $('.cpt-title-output').text('Title: ' + title);
                                }
                            }
                            if (promptValue == 'description') {
                                descriptionPending = false;
                                if ($('.cpt-description-output').length === 0) {
                                    // Create the .cpt-title-output div
                                    var descriptionOutputDiv = $('<div class="cpt-description-output"></div>');
                                    $('.cpt-generate-content').after(descriptionOutputDiv);

                                    // Output the string inside the .cpt-title-output div
                                    var description = response; // Replace with your actual title
                                    $('.cpt-description-output').text('Description: ' + description);
                                }
                            }
                            enableButtonIfBothCallsSucceeded();
                        }

                    },
                    error: function (xhr, status, error) {
                        // Handle the error
                        console.error(error);
                        $(".cpt-generate-content").prop("disabled", false);
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
                $(".cpt-generate-content").text("Processing"); // Change the button text
            }
            // if continue chat is checked, add id
            if ($('.cpt-generate-description input').is(':checked')) {
                descriptionPending = true;
                var requestDataDescription = getPromptValue('description');

                $(".cpt-generate-content").prop("disabled", true); // Disable the button
                $(".cpt-generate-content").text("Processing"); // Change the button text
            }
        });

    }

    function bindCreateExfragButton() {
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

        /**
         * Used to convert name to friendly looking jcr title
         * @param str name of the string
         * @returns {string} title case
         */
        function convertToTitleCase(str) {
            // Split the string into an array of words
            var words = str.split('-');

            // Capitalize the first letter of each word and convert the rest to lowercase
            var titleCaseWords = words.map(function(word) {
                return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase();
            });

            // Join the words back into a string with spaces between them
            var titleCaseStr = titleCaseWords.join(' ');

            return titleCaseStr;
        }

        $(".cpt-create-variation").click(function () {
            // Get the destination name from the jQuery class value
            var destName = $(".gpt-var-name").val();

            // Create the payload object
            var payload = {
                _charset_: "UTF-8",
                cmd: "copyPage",
                srcPath: currentExfragVar,
                destParentPath: stripLastSegment(currentExfragVar),
                before: "",
                destName: destName,
                shallow: false
            };

            // Convert the payload object to x-www-form-urlencoded format
            var formData = $.param(payload);

            // Make the POST request to /bin/wcmcommand
            $.post("/bin/wcmcommand", formData, function (response) {
                // Handle the response
                let newExfragPath = parseHTMLResponse(response);
                if (newExfragPath) {
                    var newExfragPathWithEditor = "/editor.html" + newExfragPath + ".html"
                    $(".cpt-create-variation").after('<p>Success!</p>' +
                        '<p><a target="_blank" href="' + newExfragPathWithEditor+ '">Click here to see new exfrag</a></p>');
                }

                // vars: latestResponse, newExfragPath, currentExfragVar
                // need to override content
                var updatePropertyMap = {
                };

                var formData = '';
                for (var jsonToUpdate in latestResponse) {

                    var propertyKey = latestResponse[jsonToUpdate].path.replace(currentExfragVar, "");
                    // propertyKey = propertyKey.slice(0, propertyKey.lastIndexOf("/")) + "@" + propertyKey.slice(propertyKey.lastIndexOf("/") + 1);
                    let generatedValue = latestResponse[jsonToUpdate].generatedValue;
                    updatePropertyMap[propertyKey] =  generatedValue;
                    if (jsonToUpdate != 0) {
                        formData += '&';
                    }

                    // remove extra quotes from generated value
                    var cleanedValue = generatedValue.replace(/^"|"$/g, '');

                    formData += "." + propertyKey + '=' + cleanedValue;
                }

                // adding in jcr title to match exfrag var name
                var uiFriendlyTitle = convertToTitleCase(destName);
                formData += "&." + '/jcr:content/jcr:title' + '=' + uiFriendlyTitle;
                // var formData = '';

                $.ajax({
                    url: newExfragPath,
                    method: "POST",
                    data: formData,
                    success: function(response) {
                        // Handle the response
                        console.log(response);
                    },
                    error: function(error) {
                        // Handle any errors
                        console.error(error);
                    }
                });

                console.log("POST request success:", response);
            })
                .fail(function (error) {
                    // Handle any errors
                    console.error("POST request failed:", error);
                });
        });
    }

    $(document).on("foundation-contentloaded", function (e) {
        if (init) {
            return;
        }
        init = true;
        bindGenerateContentButtonToPrompt();

        bindCreateExfragButton();

    });
})(window, document, Granite, Granite.$);
