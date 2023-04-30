package com.mikayelovich.quizmaker.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Answer {
	private boolean isCorrect;

	private final String text;


	@Override
	public String toString() {
		return "Answer {" +
				"isCorrect=" + isCorrect +
				", text='" + text + '\'' +
				'}';
	}
}
