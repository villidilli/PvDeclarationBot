package ru.kevdev.PvDeclarationBot.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "product_declaration",
                joinColumns = @JoinColumn(name = "product_erp_id"),
                inverseJoinColumns = @JoinColumn(name = "num_declaration"))
    private List<Declaration> declarations;
    @Column(name = "barcode")
    private String barcode;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinTable(name = "product_label_mockup",
                joinColumns = @JoinColumn(name = "product_erp_id"),
                inverseJoinColumns = @JoinColumn(name = "mockup_id"))
    private LabelMockup labelMockup;
}