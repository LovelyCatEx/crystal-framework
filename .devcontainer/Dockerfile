FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

RUN sed -i 's@//ports.ubuntu.com@//mirrors.tuna.tsinghua.edu.cn@g' /etc/apt/sources.list.d/ubuntu.sources 2>/dev/null || \
    sed -i 's@//archive.ubuntu.com@//mirrors.tuna.tsinghua.edu.cn@g' /etc/apt/sources.list

# Install essentials, JDK 17, and Maven (all via apt)
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    git \
    unzip \
    zip \
    ca-certificates \
    openjdk-17-jdk \
    maven \
    && rm -rf /var/lib/apt/lists/*

# Set environment variables
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV MAVEN_HOME=/usr/share/maven
ENV PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH"

# Create tmp directory for Kotlin daemon
RUN mkdir -p /tmp/kotlin-daemon

# Clone VertexLib dependency
RUN git clone https://github.com/LovelyCatEx/VertexLib.git /tmp/VertexLib

# Set workspace
WORKDIR /crystalframework