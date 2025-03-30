
package com.example.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/qr")
@Slf4j
public class QrCodeController {

    @GetMapping(produces = MediaType.IMAGE_PNG_VALUE)
    public Mono<ResponseEntity<byte[]>> generateQRCode(@RequestParam String text,
                                                       @RequestParam(defaultValue = "300") int size) {
       log.info("Start Process API");
        return Mono.fromCallable(() -> {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix matrix = new MultiFormatWriter().encode(
                    text, BarcodeFormat.QR_CODE, size, size, hints
            );

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", stream);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(stream.toByteArray());
        });
    }
}
