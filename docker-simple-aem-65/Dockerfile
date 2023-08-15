FROM eclipse-temurin:11 AS build
LABEL authors="jack.jin"

# working dir setup
WORKDIR /opt/aem/author

ADD crx-quickstart.tar /opt/aem/author/

WORKDIR /opt/aem/author/crx-quickstart/bin
# Enabling ssh from Azure
COPY entrypoint.sh ./

# Update apt get
# Install openssh server for Azure
# Setting default password
# Adding entrypoint to be executable, entry point also starts ssh server
# Setting timezone to new york
RUN apt-get update \
    && apt-get install -y --no-install-recommends rsync \
    && apt-get install -y --no-install-recommends dialog \
    && apt-get install -y --no-install-recommends openssh-server \
    && echo "root:Docker!" | chpasswd \
    && chmod u+x ./entrypoint.sh \
    && ln -snf /usr/share/zoneinfo/America/New_York /etc/localtime \
    && echo "America/New_York" > /etc/timezone



COPY sshd_config /etc/ssh/

# license file needed for 6.5
COPY license.properties /opt/aem/author/

EXPOSE 4502 2222

# RUN /bin/bash start
#ENTRYPOINT ["tail", "-f", "../logs/error.log"]
#ENTRYPOINT ["top", "-b"]
# FROM scratch
# COPY --from=build / /

# WORKDIR /opt/aem/author/crx-quickstart/bin
ENTRYPOINT [ "./entrypoint.sh" ]