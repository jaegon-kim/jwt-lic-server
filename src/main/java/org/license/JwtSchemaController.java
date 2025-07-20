package org.license;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/schemas")
public class JwtSchemaController {

    private final JwtSchemaService jwtSchemaService;

    public JwtSchemaController(JwtSchemaService jwtSchemaService) {
        this.jwtSchemaService = jwtSchemaService;
    }

    @PostMapping("/{schemaName}")
    public ResponseEntity<?> saveSchema(@PathVariable String schemaName, @RequestBody String schemaContent) {
        try {
            jwtSchemaService.saveSchema(schemaName, schemaContent);
            return ResponseEntity.ok().body("Schema '" + schemaName + "' saved successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving schema: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> listSchemas() {
        try {
            List<String> schemas = jwtSchemaService.listSchemas();
            return ResponseEntity.ok(schemas);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error listing schemas: " + e.getMessage());
        }
    }

    @GetMapping("/{schemaName}")
    public ResponseEntity<?> getSchema(@PathVariable String schemaName) {
        try {
            String schemaContent = jwtSchemaService.getSchema(schemaName);
            return ResponseEntity.ok().body(schemaContent);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{schemaName}")
    public ResponseEntity<?> deleteSchema(@PathVariable String schemaName) {
        try {
            jwtSchemaService.deleteSchema(schemaName);
            return ResponseEntity.ok().body("Schema '" + schemaName + "' deleted successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/verify-claims")
    public ResponseEntity<ValidationResponse> verifyClaims(@RequestBody JwtClaimsVerificationRequest request) {
        try {
            ValidationResponse response = jwtSchemaService.verifyClaimsWithSchema(request.getSchemaName(), request.getClaims());
            return Respverify-claimsonseEntity.ok(response);
        } catch (java.io.IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ValidationResponse(false, "Schema not found: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ValidationResponse(false, "Error verifying claims: " + e.getMessage()));
        }
    }
}

