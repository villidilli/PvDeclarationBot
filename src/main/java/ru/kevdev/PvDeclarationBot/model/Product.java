package ru.kevdev.PvDeclarationBot.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Product {
    @Id
    @Column(name = "erp_id")
    private Long erpId;
    @Column(name = "product_name")
    private String productName;
    @Column(name = "industrial_site")
    private String industrialSite;
    @Column(name = "product_group1")
    private String group1;
    @Column(name = "product_group2")
    private String group2;
    @Column(name = "product_group3")
    private String group3;
    @ManyToOne(fetch = FetchType.EAGER) //TODO
    @JoinColumn(name = "declaration")
    private Declaration declaration;
}