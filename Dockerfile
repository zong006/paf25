# ---------------------------- STAGE 1 ----------------------------
    FROM maven:3.9.9-eclipse-temurin-23 AS compiler

    ARG COMPIILE_DIR=/code_folder
    
    WORKDIR ${COMPIILE_DIR}
    
    COPY movies/pom.xml .
    COPY movies/mvnw .
    COPY movies/mvnw.cmd .
    COPY movies/src src
    COPY movies/.mvn .mvn 
    COPY data data
    
    RUN mvn package -Dmaven.test.skip=true
    
    # ---------------------------- STAGE 1 ----------------------------
    
    # ---------------------------- STAGE 2 ----------------------------
    
    FROM maven:3.9.9-eclipse-temurin-23
    
    ARG DEPLOY_DIR=/app
    
    WORKDIR ${DEPLOY_DIR}
    COPY --from=compiler /code_folder/target/movies-0.0.1-SNAPSHOT.jar movies.jar
    
    
    # ENV SERVER_PORT=3000
    # EXPOSE ${SERVER_PORT}
    
    # HEALTHCHECK --interval=60s --start-period=120s\
    #     CMD curl -s -f http://localhost:${SERVER_PORT}/status || exit 1
    
    ENTRYPOINT java -jar movies.jar
        
    # ---------------------------- STAGE 2 ----------------------------
    
            