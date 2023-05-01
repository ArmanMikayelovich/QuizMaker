package com.mikayelovich.quizmaker.util;

import com.google.api.services.forms.v1.model.CorrectAnswer;
import com.google.api.services.forms.v1.model.Option;
import com.mikayelovich.quizmaker.model.Answer;

import java.util.function.Function;

public final class FunctionalUtils {
	private FunctionalUtils() {

	}

	public static final Function<Answer, Option> mapAnswerToOption = answer -> {
		Option option = new Option();
		option.setValue(answer.getText());
		return option;
	};

	public static final Function<Answer, CorrectAnswer> mapAnswerToCorrectAnswer = answer -> {
		CorrectAnswer option = new CorrectAnswer();
		option.setValue(answer.getText());
		return option;
	};
}
