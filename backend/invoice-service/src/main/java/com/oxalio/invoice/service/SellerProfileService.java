package com.oxalio.invoice.service;

import com.oxalio.invoice.entity.SellerProfileEntity;
import com.oxalio.invoice.repository.SellerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerProfileService {

    private final SellerProfileRepository sellerProfileRepository;

    @Value("${oxalio.invoice.logo-base-dir:./logos}")
    private String logoBaseDir;

    /**
     * Upload / mise à jour du logo vendeur.
     * - Sauvegarde le fichier sur disque
     * - Met à jour le SellerProfileEntity (chemin)
     */
    @Transactional
    public SellerProfileEntity uploadLogo(String taxId, MultipartFile file) throws IOException {
        if (taxId == null || taxId.isBlank()) {
            throw new IllegalArgumentException("taxId vendeur obligatoire pour l'upload du logo");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Fichier logo vide");
        }

        // Crée le dossier si besoin
        Path baseDir = Paths.get(logoBaseDir).toAbsolutePath().normalize();
        Files.createDirectories(baseDir);

        // Nom de fichier simple : IFU.png ou IFU.ext
        String originalName = file.getOriginalFilename();
        String extension = ".png";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf('.'));
        }
        String fileName = taxId + extension;

        Path target = baseDir.resolve(fileName);

        // Copie / remplace si déjà existant
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // Cherche profil existant ou en crée un nouveau
        SellerProfileEntity profile = sellerProfileRepository
                .findByTaxId(taxId)
                .orElseGet(() -> {
                    SellerProfileEntity p = new SellerProfileEntity();
                    p.setTaxId(taxId);
                    return p;
                });

        profile.setLogoPath(target.toString());
        // ⚠️ plus de setLogoMimeType ici car le champ n’existe pas dans l’entité

        SellerProfileEntity saved = sellerProfileRepository.save(profile);
        log.info("Logo vendeur sauvegardé pour {} -> {}", taxId, target);

        return saved;
    }

    /**
     * Récupère le logo en binaire.
     * - Retourne null si aucun logo ou fichier introuvable.
     * - Ne jette pas d'exception pour ne pas casser la génération de PDF.
     */
    public byte[] getLogoBytes(String taxId) {
        try {
            if (taxId == null || taxId.isBlank()) {
                return null;
            }

            return sellerProfileRepository.findByTaxId(taxId)
                    .map(SellerProfileEntity::getLogoPath)
                    .filter(p -> p != null && !p.isBlank())
                    .map(Paths::get)
                    .filter(Files::exists)
                    .map(path -> {
                        try {
                            return Files.readAllBytes(path);
                        } catch (IOException e) {
                            log.warn("Impossible de lire le fichier logo {} : {}", path, e.getMessage());
                            return null;
                        }
                    })
                    .orElse(null);

        } catch (Exception e) {
            log.warn("Erreur lors du chargement du logo pour {} : {}", taxId, e.getMessage());
            return null;
        }
    }

    @Transactional
    public void deleteLogo(String taxId) throws Exception {
        SellerProfileEntity profile = sellerProfileRepository.findByTaxId(taxId)
                .orElseThrow(() -> new RuntimeException("Seller profile not found: " + taxId));

        if (profile.getLogoPath() != null) {
            Path p = Paths.get(profile.getLogoPath());
            try {
                Files.deleteIfExists(p);
            } catch (Exception e) {
                log.warn("Impossible de supprimer le fichier logo {}", p);
            }
        }

        profile.setLogoPath(null);
        sellerProfileRepository.save(profile);
    }
}
