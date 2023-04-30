package com.mikayelovich.quizmaker.util;

import com.mikayelovich.quizmaker.model.Answer;
import com.mikayelovich.quizmaker.model.QuestionModel;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReviewParser {
	public static void mergeReview(String reviewText, List<QuestionModel> questions) {
		Pattern reviewPattern = Pattern.compile("(\\d+)\\.\\s*([A-Z,\\s]+)\\.\\s*([^\\n]+(?:\\n(?=\\s+)[^\\n]+)*)", Pattern.DOTALL);
		Matcher reviewMatcher = reviewPattern.matcher(reviewText);

		while (reviewMatcher.find()) {
			Integer questionId = Integer.parseInt(reviewMatcher.group(1));
			String correctAnswersText = reviewMatcher.group(2);
			String explanation = reviewMatcher.group(3).trim();

			for (QuestionModel question : questions) {
				if (question.getId().equals(questionId)) {
					question.setExplanation(explanation);
					updateRightAnswers(question.getAnswers(), correctAnswersText);
					break;
				}
			}
		}
	}

	private static void updateRightAnswers(List<Answer> answers, String correctAnswersText) {
		String[] correctAnswers = correctAnswersText.split(",\\s*");

		for (Answer answer : answers) {
			for (String correctAnswer : correctAnswers) {
				if (answer.getText().startsWith(correctAnswer)) {
					answer.setRight(true);
					break;
				}
			}
		}
	}
}
