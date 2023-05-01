package com.mikayelovich.quizmaker.service;

import com.mikayelovich.quizmaker.model.Answer;
import com.mikayelovich.quizmaker.model.QuestionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReviewParser {

	@Value("${application.keep-right-answers-in-explanation}")
	private Boolean keepRightAnswers;
	public void mergeReview(String reviewText, List<QuestionModel> questionModels) {
		mergeReviewWithAnswers(reviewText, questionModels, keepRightAnswers);
	}

	private  void mergeReviewWithAnswers(String reviewText, List<QuestionModel> questionModels, Boolean keepRightAnswers) {
		Pattern reviewPattern = Pattern.compile("(\\d+)\\.\\s*([A-Z,\\s]+)\\.\\s*([^\\d]+(?:\\n(?=\\s+)[^\\n]+)*)", Pattern.DOTALL);
		Matcher reviewMatcher = reviewPattern.matcher(reviewText);

		while (reviewMatcher.find()) {
			Integer questionId = Integer.parseInt(reviewMatcher.group(1));
			String correctAnswersText = reviewMatcher.group(2);
			String explanation = reviewMatcher.group(3).trim();

			for (QuestionModel questionModel : questionModels) {
				if (questionModel.getId().equals(questionId)) {
					if (Boolean.TRUE.equals(keepRightAnswers)) {
						explanation = "Correct answers: " + correctAnswersText + "\n\n" + explanation;
					}
					questionModel.setExplanation(explanation);
					updateRightAnswers(questionModel.getAnswers(), correctAnswersText);
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
