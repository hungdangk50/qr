package com.example.qr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.docx4j.XmlUtils;
import org.docx4j.model.datastorage.BindingHandler;
import org.docx4j.model.datastorage.OpenDoPEHandler;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Map;

@Service
public class DocxTemplateService {

    public File generateDocx(String templateName, Map<String, Object> jsonData) throws Exception {
        String templatePath = "src/main/resources/templates/" + templateName + ".docx";

        // Convert Map → JSON → XML
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.valueToTree(jsonData);
        String xml = XmlUtils.marshaltoString(jsonNode, true);
        Document xmlDoc = XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));

        // Load docx template
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new File(templatePath));

        // Preprocess OpenDoPE if needed
        new OpenDoPEHandler(wordMLPackage).preprocess();

        // Apply data
//        BindingHandler.applyBindings(wordMLPackage.getMainDocumentPart(), xmlDoc);

        // Output
        File outputFile = File.createTempFile("contract-", ".docx");
        wordMLPackage.save(outputFile);
        return outputFile;
    }
}

