FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY . ./
RUN ./gradlew dockerSyncBuildContext


FROM eclipse-temurin:17-jre
LABEL maintainer=khakers
WORKDIR /app
COPY --from=builder /app/build/docker/libs libs/
COPY --from=builder /app/build/docker/resources resources/
COPY --from=builder /app/build/docker/classes classes/
ENTRYPOINT ["java", "-cp", "/app/resources:/app/classes:/app/libs/*", "com.github.khakers.modmailviewer.Main"]
EXPOSE 80 443