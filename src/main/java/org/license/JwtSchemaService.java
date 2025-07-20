package org.license;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class JwtSchemaService {

    private static final Logger log = LoggerFactory.getLogger(JwtSchemaService.class);

    private final String schemaDirectory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtSchemaService(@Value("${jwt.schema.directory}") String schemaDirectory) {
        this.schemaDirectory = schemaDirectory;
        // Ensure the schema directory exists
        try {
            Files.createDirectories(Paths.get(schemaDirectory));
        } catch (IOException e) {
            log.error("Failed to create schema directory: {}", schemaDirectory, e);
            // Depending on the application's robustness requirements,
            // you might want to throw a runtime exception here or handle it differently.
        }
    }

    public void saveSchema(String schemaName, String schemaContent) throws IOException {
        Path schemaPath = Paths.get(schemaDirectory, schemaName + ".json");
        Files.writeString(schemaPath, schemaContent);
        log.info("Schema '{}' saved to {}", schemaName, schemaPath);
    }

    public String getSchema(String schemaName) throws IOException {
        Path schemaPath = Paths.get(schemaDirectory, schemaName + ".json");
        if (!Files.exists(schemaPath)) {
            throw new IOException("Schema '" + schemaName + "' not found.");
        }
        return Files.readString(schemaPath);
    }

    public List<String> listSchemas() throws IOException {
        try (Stream<Path> walk = Files.walk(Paths.get(schemaDirectory))) {
            return walk
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.endsWith(".json"))
                    .map(name -> name.substring(0, name.length() - ".json".length()))
                    .collect(Collectors.toList());
        }
    }

    public void deleteSchema(String schemaName) throws IOException {
        Path schemaPath = Paths.get(schemaDirectory, schemaName + ".json");
        if (!Files.exists(schemaPath)) {
            throw new IOException("Schema '" + schemaName + "' not found.");
        }
        Files.delete(schemaPath);
        log.info("Schema '{}' deleted from {}", schemaName, schemaPath);
    }

    public ValidationResponse verifyClaimsWithSchema(String schemaName, Map<String, Object> claims) throws IOException {
        String schemaContent = getSchema(schemaName);
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        JsonSchema jsonSchema = factory.getSchema(schemaContent);

        JsonNode jsonClaims = objectMapper.valueToTree(claims);
        Set<ValidationMessage> validationMessages = jsonSchema.validate(jsonClaims);

        if (validationMessages.isEmpty()) {
            return new ValidationResponse(true, "Claims successfully validated against schema '" + schemaName + "'.");
        } else {
            List<String> errors = validationMessages.stream()
                           .map(ValidationMessage::getMessage)
                           .collect(Collectors.toList());
            return new ValidationResponse(false, "Claims validation failed against schema '" + schemaName + "'.", errors);
        }
    }
}

