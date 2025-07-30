package org.license;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testJwtSigningWorkflow() throws Exception {
        // 1. Generate Certificate
        mockMvc.perform(post("/certificates/generate")
                        .param("commonName", "test-cert")
                        .param("validityDays", "365"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commonName").value("test-cert"));

        // 2. List Certificates
        mockMvc.perform(get("/certificates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].commonName").value("test-cert"));

        // 3. Sign JWT
        String claims = "{\"sub\":\"1234567890\",\"name\":\"John Doe\"}";
        String requestBody = String.format("{\"commonName\":\"test-cert\",\"claims\":%s}", claims);
        mockMvc.perform(post("/jwt/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        // 4. Verify History
        mockMvc.perform(get("/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].success").value(true));

        // 5. Delete Certificate
        mockMvc.perform(delete("/certificates/test-cert"))
                .andExpect(status().isOk());
    }

    @Test
    void testSchemaLifecycle() throws Exception {
        String schema = "{\"type\":\"object\",\"properties\":{\"sub\":{\"type\":\"string\"}},\"required\":[\"sub\"]}";

        // 1. Create Schema
        mockMvc.perform(post("/schemas/test-schema")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(schema))
                .andExpect(status().isOk());

        // 2. Get Schema
        mockMvc.perform(get("/schemas/test-schema"))
                .andExpect(status().isOk())
                .andExpect(content().json(schema));

        // 3. Delete Schema
        mockMvc.perform(delete("/schemas/test-schema"))
                .andExpect(status().isOk());
    }
}