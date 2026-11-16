FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

RUN mkdir -p out && javac -d out *.java

EXPOSE 8103

CMD ["java", "-cp", "out", "com.infinity.somayeh.ServerMain"]
