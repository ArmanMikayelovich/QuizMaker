package com.mikayelovich.quizmaker.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;

@Data
@ToString
@RequiredArgsConstructor
public class QuestionModel {
	private final Integer id;
	private final String question;
	private final List<Answer> answers;
	private final boolean isMultipleRightAnswers;

	private String explanation;


	@Override
	public String toString() {
		return "Question {" +
				"id=" + id +
				", question='" + question + '\'' +
				", answers=\n" + answers.stream().map(Answer::toString).collect(Collectors.joining("\n")) +
				'}';
	}

}