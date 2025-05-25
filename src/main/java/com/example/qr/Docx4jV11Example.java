package com.example.qr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;

import java.io.File;
import java.util.*;

public class Docx4jV11Example {

    public static void main(String[] args) throws Exception {
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new File("template-docx4j-verified.docx"));

        // Load JSON input
        String json = "{"
                + "  \"contract\": { \"number\": \"HD-2025-ABC\", \"date\": \"2025-06-01\" },"
                + "  \"customer\": { \"name\": \"Nguyễn Văn A\", \"address\": \"123 Lê Lợi, Hà Nội\" },"
                + "  \"orderList\": ["
                + "    { \"productName\": \"Máy in\", \"price\": \"3,000,000\" },"
                + "    { \"productName\": \"Giấy A4\", \"price\": \"500,000\" }"
                + "  ]"
                + "}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        // 1. Replace các field đơn {{...}}
        Map<String, String> flatReplacements = new HashMap<>();
        flatReplacements.put("{{contract.number}}", root.at("/contract/number").asText());
        flatReplacements.put("{{contract.date}}", root.at("/contract/date").asText());
        flatReplacements.put("{{customer.name}}", root.at("/customer/name").asText());
        flatReplacements.put("{{customer.address}}", root.at("/customer/address").asText());

        new TraversalUtil(wordMLPackage.getMainDocumentPart().getJaxbElement(), new TraversalUtil.CallbackImpl() {
            @Override
            public List<Object> apply(Object o) {
                if (o instanceof Text) {
                    Text text = (Text) o;
                    String value = text.getValue();
                    for (Map.Entry<String, String> entry : flatReplacements.entrySet()) {
                        if (value.contains(entry.getKey())) {
                            text.setValue(value.replace(entry.getKey(), entry.getValue()));
                        }
                    }
                }
                return null;
            }
        });

        // 2. Lặp bảng orderList[*]
        JsonNode orderList = root.path("orderList");
        List<Object> tables = getAllElementsOfType(wordMLPackage.getMainDocumentPart(), Tbl.class);
        for (Object tblObj : tables) {
            Tbl tbl = (Tbl) tblObj;
            List<Object> rows = getAllElementsOfType(tbl, Tr.class);
            for (Object rowObj : rows) {
                Tr row = (Tr) rowObj;
                String rowText = getText(row);
                if (rowText.contains("orderList[*]")) {
                    List<Tr> newRows = new ArrayList<>();
                    for (JsonNode item : orderList) {
                        Tr newRow = XmlUtils.deepCopy(row);
                        replaceRowText(newRow, Map.of(
                                "{{orderList[*].productName}}", item.path("productName").asText(),
                                "{{orderList[*].price}}", item.path("price").asText()
                        ));
                        newRows.add(newRow);
                    }
                    tbl.getContent().remove(row);
                    tbl.getContent().addAll(newRows);
                    break;
                }
            }
        }

        wordMLPackage.save(new File("output-filled.docx"));
        System.out.println("✅ Done: output-filled.docx");
    }

    static void replaceRowText(Tr row, Map<String, String> replacements) {
        List<Object> texts = getAllElementsOfType(row, Text.class);
        for (Object obj : texts) {
            Text t = (Text) obj;
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                if (t.getValue().contains(entry.getKey())) {
                    t.setValue(t.getValue().replace(entry.getKey(), entry.getValue()));
                }
            }
        }
    }

    static List<Object> getAllElementsOfType(Object obj, Class<?> type) {
        List<Object> result = new ArrayList<>();
        new TraversalUtil(obj, new TraversalUtil.CallbackImpl() {
            @Override
            public List<Object> apply(Object o) {
                if (type.isAssignableFrom(o.getClass())) {
                    result.add(o);
                }
                return null;
            }
        });
        return result;
    }

    static String getText(Object obj) {
        StringBuilder sb = new StringBuilder();
        List<Object> texts = getAllElementsOfType(obj, Text.class);
        for (Object o : texts) {
            sb.append(((Text) o).getValue());
        }
        return sb.toString();
    }
}

