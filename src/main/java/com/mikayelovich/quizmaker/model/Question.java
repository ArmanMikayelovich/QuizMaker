package com.mikayelovich.quizmaker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;

@Data
@ToString
@AllArgsConstructor
public class Question {
	private Integer id;
	private String question;
	private List<Answer> answers;
	private boolean isMultipleRightAnswers;


	@Override
	public String toString() {
		return "QuestionModel{" +
				"id=" + id +
				", question='" + question + '\'' +
				", answers=" + answers.stream().map(Answer::toString).collect(Collectors.joining("\n")) +
				'}';
	}

}