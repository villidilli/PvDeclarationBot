package ru.kevdev.PvDeclarationBot.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.kevdev.PvDeclarationBot.model.Product;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {
    Optional<Product> findById(Long id);

    List<Product> findByIndustrialSiteAndBarcode(String indSite, String barcode);

    @Query("SELECT p.industrialSite " +
            "FROM Product as p " +
            "WHERE p.barcode = :barcode " +
            "GROUP BY p.industrialSite")
    List<String> getIndustrialSitesByProductBarcode(String barcode);
}