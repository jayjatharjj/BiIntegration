package com.biintegration.controller;

import com.biintegration.service.ClickHouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data-flow")
@CrossOrigin(origins = "http://localhost:8081")
public class DataFlowController {

    @Autowired
    private ClickHouseService clickHouseService;

    @PostMapping("/clickhouse-to-file")
    public ResponseEntity<String> exportClickHouseToFile(@RequestBody Map<String, String> request) {
        try {
            String query = request.get("query");
            List<Map<String, Object>> results = clickHouseService.executeQuery(query);
            // TODO: Implement file export logic
            return ResponseEntity.ok("Data exported successfully");
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body("Error exporting data: " + e.getMessage());
        }
    }

    @PostMapping("/file-to-clickhouse")
    public ResponseEntity<String> importFileToClickHouse(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tableName") String tableName) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line;
            StringBuilder insertQuery = new StringBuilder("INSERT INTO " + tableName + " VALUES ");
            
            while ((line = reader.readLine()) != null) {
                // TODO: Implement file parsing and query building logic
            }
            
            clickHouseService.executeUpdate(insertQuery.toString());
            return ResponseEntity.ok("Data imported successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error importing data: " + e.getMessage());
        }
    }
} 