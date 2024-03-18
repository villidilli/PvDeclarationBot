package ru.kevdev.PvDeclarationBot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {
	@Column(name = "user_name")
	private String name;
	@Id
	@Column(name = "user_email")
	@Email
	private String email;
	@Column(name = "partner_name")
	private String partnerName;
}