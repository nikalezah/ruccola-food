# Stage 1: Build
FROM gradle:jdk25 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN chmod +x gradlew && ./gradlew :server:installDist --no-daemon

# Stage 2: Run
FROM eclipse-temurin:25-jre-noble
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/server/build/install/server /app
WORKDIR /app/bin
ENTRYPOINT ["./server"]