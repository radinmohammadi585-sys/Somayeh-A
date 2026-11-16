FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

RUN find . -name "*.java" -print > sources.txt \
    && mkdir -p out \
    && javac -encoding UTF-8 -d out @sources.txt

ENV PORT=10000

EXPOSE 10000

CMD ["sh", "-c", "java -cp out com.infinity.somayeh.ServerMain"]
