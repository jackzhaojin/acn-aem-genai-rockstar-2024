#!/bin/bash

# Define the directories and files
BASE_DIR="/opt/aem/author"
JAR_FILE="$BASE_DIR/aem-author.jar"

java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar "$JAR_FILE" -nointeractive