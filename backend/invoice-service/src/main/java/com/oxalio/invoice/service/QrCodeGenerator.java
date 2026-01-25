package com.oxalio.invoice.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * Service de génération de QR codes conforme aux exigences de certification FNE/RNE.
 */
@Slf4j
@Service
public class QrCodeGenerator {

    /**
     * Génère un QR code et le retourne sous forme de chaîne Base64 pour l'inclusion dans le JSON.
     * Cette méthode est appelée par InvoiceService pour la certification[cite: 22, 87].
     * * @param content URL de vérification FNE (token) [cite: 118, 224]
     * @param width Largeur du QR Code
     * @param height Hauteur du QR Code
     * @return String encodée en Base64
     */
    public String generateQRCodeBase64(String content, int width, int height) {
        try {
            byte[] imageBytes = generateQrCode(content, width, height);
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la conversion Base64 du QR Code: {}", e.getMessage());
            throw new RuntimeException("Échec de la génération Base64 pour le sticker DGI", e);
        }
    }

    /**
     * Génère un QR code au format PNG (tableau d'octets).
     * * @param content Contenu (Token/URL de vérification) fourni par la plateforme FNE 
     * @param width Largeur en pixels
     * @param height Hauteur en pixels
     * @return QR code en bytes PNG
     */
    public byte[] generateQrCode(String content, int width, int height) {
        try {
            log.debug("Generating QR code for: {}", content);
            
            // Créer le QR code avec ZXing selon le format normalisé
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);
            
            // Convertir en BufferedImage pour le traitement graphique
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            
            // Conversion en flux PNG
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            
            log.info("✅ QR code generated: {} bytes", imageBytes.length);
            
            return imageBytes;
            
        } catch (Exception e) {
            log.error("❌ Error generating QR code: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
}