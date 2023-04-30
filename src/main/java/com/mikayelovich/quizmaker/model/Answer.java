package com.mikayelovich.quizmaker.model;

import com.mikayelovich.quizmaker.util.ConsoleUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@Data
@RequiredArgsConstructor
public class Answer {
	private boolean isRight;

	private final String text;


	@Override
	public String toString() {
		String answerText = "Answer {" +  "text='" + text + '\'' + ", isRight=" + isRight +'}';
		if (isRight) {
			return ConsoleUtils.makeLineColorful(ConsoleUtils.Color.GREEN, answerText);
		}
		return answerText;
	}
}
