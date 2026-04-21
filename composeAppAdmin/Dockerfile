# Stage 1: Build
FROM gradle:jdk25 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN sed -i "s|http://localhost:8080|https://food-f1g8.onrender.com|g" shared/src/webMain/kotlin/kz/interna/food/Host.web.kt
ENV GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx4g -Dorg.gradle.workers.max=1 -Dorg.gradle.parallel=false"
RUN chmod +x gradlew && ./gradlew :web:admin:jsBrowserDevelopmentDistribution --no-daemon --no-configuration-cache \
    -Pkotlin.incremental=false

# Stage 2: Serve
FROM nginx:stable-alpine
COPY --from=build /home/gradle/src/web/admin/build/dist/js/developmentExecutable /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]