package ru.kevdev.PvDeclarationBot.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "label_mockups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LabelMockup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mockup_id")
    private Long id;
    @Column(name = "file_name")
    private String fileName;
}