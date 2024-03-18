package ru.kevdev.PvDeclarationBot.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChatModel {
	@Id
	@Column(name = "chat_id_telegram")
	private Long chatIdTelegram;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_email")
	private User user;
	private Boolean verified = false;
}