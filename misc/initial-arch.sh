# Run this in the parent project folder by entering the following -
# misc/initial-arch.sh

mvn -B org.apache.maven.plugins:maven-archetype-plugin:3.2.1:generate \
 -D archetypeGroupId=com.adobe.aem \
 -D archetypeArtifactId=aem-project-archetype \
 -D archetypeVersion=43\
 -D appTitle="Accenture AEM Gen AI Accelerator" \
 -D appId="acngenai" \
 -D artifactId="acngenai" \
 -D version="1.0-SNAPSHOT" \
 -D aemVersion=cloud \
 -D groupId="com.accenture.aem.genai"

mv acngenai/* .