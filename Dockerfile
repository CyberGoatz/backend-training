ARG PROJECT_ARTIFACT_ID=training

############ BUILD STAGE ############
FROM maven:3.8.5-openjdk-17-slim AS build

WORKDIR /app

ARG PROJECT_ARTIFACT_ID
ARG MAVEN_CLI_OPTS=EXTRA-OPTIONS

COPY pom.xml /app/pom.xml
COPY etc/ci_settings.xml /app/etc/ci_settings.xml

COPY training-api /app/training-api
COPY training-opensearch /app/training-opensearch
COPY training-persistence /app/training-persistence
COPY training-service /app/training-service
COPY training-rest /app/training-rest

# Build JAR file
RUN mvn -ntp -q clean install -DskipTests -DskipChecks=true $MAVEN_CLI_OPTS && \
    cp /app/training-rest/target/$PROJECT_ARTIFACT_ID-*.jar /app/$PROJECT_ARTIFACT_ID.jar

############ RUNNABLE STAGE ############
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

ARG PROJECT_ARTIFACT_ID

ENV PROJECT_ARTIFACT_ID=${PROJECT_ARTIFACT_ID}

COPY etc/$PROJECT_ARTIFACT_ID.properties /app/etc/$PROJECT_ARTIFACT_ID.properties
COPY entrypoint.sh /app/entrypoint.sh
COPY --from=build /app/$PROJECT_ARTIFACT_ID.jar ./

RUN apt-get update && \
    # Required to use nc command in the wait for it function, see entrypoint.sh
    apt-get install -y netcat && \
    # Make a file executable
    chmod a+x entrypoint.sh

EXPOSE 8083

ENTRYPOINT ["./entrypoint.sh"]
