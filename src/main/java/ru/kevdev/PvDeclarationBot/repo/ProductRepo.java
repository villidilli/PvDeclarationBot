package ru.kevdev.PvDeclarationBot.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kevdev.PvDeclarationBot.model.Product;

import java.util.Optional;

public interface ProductRepo extends JpaRepository<Product, Long> {
    @Override
    Optional<Product> findById(Long id);
}