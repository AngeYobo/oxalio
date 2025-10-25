package com.oxalio.invoice.repository;

import com.oxalio.invoice.model.Sticker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StickerRepository extends JpaRepository<Sticker, Long> {
    Optional<Sticker> findFirstByStatusOrderByIdAsc(Sticker.StickerStatus status);
}
