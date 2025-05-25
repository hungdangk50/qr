package com.example.qr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    @Autowired
    private DocxTemplateService docxTemplateService;

    @PostMapping("/generate")
    public ResponseEntity<Resource> generateContract(
            @RequestParam String templateName,
            @RequestBody Map<String, Object> jsonData
    ) throws Exception {

        File result = docxTemplateService.generateDocx(templateName, jsonData);

        InputStreamResource resource = new InputStreamResource(new FileInputStream(result));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + result.getName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
