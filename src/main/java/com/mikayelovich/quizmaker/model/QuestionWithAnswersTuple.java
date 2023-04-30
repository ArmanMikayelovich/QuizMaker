package com.mikayelovich.quizmaker.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuestionWithAnswersTuple {
	private String questions;
	private String answers;

}
