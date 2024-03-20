package ru.kevdev.PvDeclarationBot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import ru.kevdev.PvDeclarationBot.utils.Constant;

import java.time.LocalDateTime;

@Entity
@Table(name = "declarations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Declaration {
    @Id
    @Column(name = "number_declaration")
    private String declarationNum;
    @Column(name = "start_date")
    @DateTimeFormat(pattern = Constant.dateTimeFormat)
    private LocalDateTime startDate;
    @Column(name = "due_date")
    @DateTimeFormat(pattern = Constant.dateTimeFormat)
    private LocalDateTime dueDate;
    @Column(name = "path")
    private String pathToFile;
}