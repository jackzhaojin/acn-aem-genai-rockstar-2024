# Note that this docker container assumes crx-quickstart is mounted to /opt/aem/author/crx-quickstart

FROM eclipse-temurin:11.0.19_7-jdk
LABEL authors="jack.jin"

# working dir setup

# WORKDIR /opt/aem/author/crx-quickstart/bin
WORKDIR /opt/aem/author

# feel free to update this to the latest sdk
COPY aem-author.jar ./

# Update apt get
# Install openssh server for Azure
# Setting default password
# Adding entrypoint to be executable, entry point also starts ssh server
# Setting timezone to new york
RUN apt-get update \
    && apt-get install -y --no-install-recommends dialog \
    && apt-get install -y --no-install-recommends openssh-server \
    && echo "root:Docker!" | chpasswd \
    && ln -snf /usr/share/zoneinfo/America/New_York /etc/localtime \
    && echo "America/New_York" > /etc/timezone


COPY sshd_config /etc/ssh/

COPY entrypoint.sh ./

RUN chmod u+x ./entrypoint.sh

EXPOSE 4502 2222 5005

ENTRYPOINT [ "./entrypoint.sh" ]