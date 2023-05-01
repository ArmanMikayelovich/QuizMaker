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

		//TODO refactor, may lead to stack over flow in large inputs
		Pattern questionPattern = Pattern.compile("(\\d+)\\.\\s*((?:[^\\d]|\\d(?![.]))+?)(?=\\s*\\d+\\.|$)", Pattern.DOTALL);
		Matcher questionMatcher = questionPattern.matcher(input);

		while (questionMatcher.find()) {
			Integer questionId = Integer.parseInt(questionMatcher.group(1));
			String questionText = questionMatcher.group(2).trim();
			boolean isMultipleRightAnswers = hasMultipleRightAnswers(questionText);
			List<Answer> answers = parseAnswers(questionText);
			questionText = questionText.replaceAll("(\\s*[A-Z]+\\.\\s*[^\\n]+(?:\\n[^A-Z]+)*)", "").trim();
			questionModels.add(new QuestionModel(questionId, questionText, answers,isMultipleRightAnswers));
		}

		return questionModels;
	}

	private static List<Answer> parseAnswers(String questionText) {
		List<Answer> answers = new ArrayList<>();
		Pattern answerPattern = Pattern.compile("([A-Z]+\\.\\s*[^\\n]+(?:\\n[^A-Z]+)*)", Pattern.DOTALL);
		Matcher answerMatcher = answerPattern.matcher(questionText);

		while (answerMatcher.find()) {
			answers.add(new Answer(answerMatcher.group(1).trim()));
		}

		return answers;
	}

	private static boolean hasMultipleRightAnswers(String questionText) {
		Pattern multipleRightAnswersPattern = Pattern.compile("(choose\\s+(all\\s+that\\s+apply|(?:two|three|four|five|six|seven|eight|nine)))", Pattern.CASE_INSENSITIVE);
		Matcher multipleRightAnswersMatcher = multipleRightAnswersPattern.matcher(questionText);
		return multipleRightAnswersMatcher.find();
	}
}
