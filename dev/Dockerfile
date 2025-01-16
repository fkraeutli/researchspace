FROM ubuntu:22.04
RUN mkdir workdir
WORKDIR /workdir

RUN apt update
RUN apt -y install curl wget unzip dirmngr apt-transport-https lsb-release ca-certificates openjdk-21-jdk python2

# Install Gradle
RUN wget https://services.gradle.org/distributions/gradle-8.11.1-bin.zip -P /tmp
RUN unzip -d /opt/gradle /tmp/gradle-8.11.1-bin.zip
RUN ln -s /opt/gradle/gradle-8.11.1 /opt/gradle/latest

# Install Node.js v22x
RUN curl -sL https://deb.nodesource.com/setup_22.x | bash -
RUN apt install -y nodejs

# Set environment variables
ENV GRADLE_HOME=/opt/gradle/latest
ENV PATH=${GRADLE_HOME}/bin:${PATH}
ENV GRADLE_USER_HOME=/opt/gradle

CMD ["tail", "-f", "/dev/null"]