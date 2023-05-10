package com.mikayelovich.quizmaker.service;

import com.mikayelovich.quizmaker.model.Answer;
import com.mikayelovich.quizmaker.model.QuestionModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public  class QuestionParser {

	public List<QuestionModel> parseQuestions(String input) {
		List<QuestionModel> questionModels = new ArrayList<>();

		String[] lines = input.split("\n");
		StringBuilder currentQuestionText = new StringBuilder();
		Integer currentQuestionId = null;
		boolean isMultipleRightAnswers = false;

		for (String line : lines) {
			Pattern questionPattern = Pattern.compile("^(\\d+)\\.");
			Matcher questionMatcher = questionPattern.matcher(line);

			if (questionMatcher.find()) {
				if (currentQuestionId != null) {
					List<Answer> answers = parseAnswers(currentQuestionText.toString());
					String questionText = currentQuestionText.toString().replaceAll("(\\s*[A-Z]+\\.\\s*[^\\n]+(?:\\n[^A-Z]+)*)", "").trim();
					isMultipleRightAnswers = hasMultipleRightAnswers(questionText);

					questionModels.add(new QuestionModel(currentQuestionId, questionText, answers, isMultipleRightAnswers));
				}
				currentQuestionId = Integer.parseInt(questionMatcher.group(1));
				currentQuestionText = new StringBuilder(line);
			} else {
				currentQuestionText.append("\n").append(line);
			}
		}

		if (currentQuestionId != null) {
			List<Answer> answers = parseAnswers(currentQuestionText.toString());
			String questionText = currentQuestionText.toString().replaceAll("(\\s*[A-Z]+\\.\\s*[^\\n]+(?:\\n[^A-Z]+)*)", "").trim();
			isMultipleRightAnswers = hasMultipleRightAnswers(questionText);
			questionModels.add(new QuestionModel(currentQuestionId, questionText, answers, isMultipleRightAnswers));
		}

		return questionModels;
	}

	private static List<Answer> parseAnswers(String questionText) {
		List<Answer> answers = new ArrayList<>();
		Pattern answerPattern = Pattern.compile("([A-Z]+\\.\\s*[^\\n]+(?:\\n[^A-Z]+)*)", Pattern.DOTALL);
		Matcher answerMatcher = answerPattern.matcher(questionText);

		while (answerMatcher.find()) {
			String answerText = answerMatcher.group(1).trim().replaceAll("[\\t\\n\\r]+", " ");;
			answers.add(new Answer(answerText));
		}

		return answers;
	}

	private static boolean hasMultipleRightAnswers(String questionText) {
		Pattern multipleRightAnswersPattern = Pattern.compile("(?i)Choose(\\s+(all\\s+that\\s+apply|(?:two|three|four|five|six|seven|eight|nine)))?");
		Matcher multipleRightAnswersMatcher = multipleRightAnswersPattern.matcher(questionText);
		return multipleRightAnswersMatcher.find();
	}


}

