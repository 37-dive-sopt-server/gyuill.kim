package org.sopt.domain.article.domain.entity;

import lombok.Getter;

@Getter
public enum Tag {
	CS("Computer Science"),
	DB("Database"),
	SPRING("Spring Framework"),
	ETC("Etcetera");

	private final String description;

	Tag(String description) {
		this.description = description;
	}
}
