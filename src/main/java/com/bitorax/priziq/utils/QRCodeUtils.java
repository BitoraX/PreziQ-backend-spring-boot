package com.bitorax.priziq.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QRCodeUtils {

    public String generateQRCode(String qrContent) throws Exception {
        // Config create QR code
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        // Create QR code
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(qrContent, BarcodeFormat.QR_CODE, 200, 200, hints);
        } catch (WriterException e) {
            throw new Exception("Failed to generate QR code", e);
        }

        // Change BitMatrix to image and encode to Base64
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        String base64Image = Base64.getEncoder().encodeToString(outputStream.toByteArray());

        // Return URL shape Data URL (Front-end directly show)
        return "data:image/png;base64," + base64Image;
    }
}
