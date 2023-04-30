package com.mikayelovich.quizmaker.util;

import com.mikayelovich.quizmaker.model.Answer;
import com.mikayelovich.quizmaker.model.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class QuestionParser {
	private static final Pattern QUESTION_PATTERN =Pattern.compile("(\\d+)(?:\\.|\\))\\s*([^\\d]+?)(?=\\s*\\d+(?:\\.|\\))|$)", Pattern.DOTALL);
						 Pattern questionPattern = Pattern.compile("(\\d+)\\.\\s*([^\\d]+?)(?=\\s*\\d+\\.|$)", Pattern.DOTALL);

	private static final Pattern ANSWER_PATTERN = Pattern.compile("([A-Z]+\\.\\s*[^\\n]+(?:\\n[^A-Z]+)*)", Pattern.DOTALL);


	public static List<Question> parseQuestions(String input) {
		List<Question> questions = new ArrayList<>();
		Pattern questionPattern = Pattern.compile("(\\d+)\\.\\s*((?:[^\\d]|\\d(?![.]))+?)(?=\\s*\\d+\\.|$)", Pattern.DOTALL);
		Matcher questionMatcher = questionPattern.matcher(input);

		while (questionMatcher.find()) {
			Integer questionId = Integer.parseInt(questionMatcher.group(1));
			String questionText = questionMatcher.group(2).trim();
			boolean isMultipleRightAnswers = hasMultipleRightAnswers(questionText);
			List<Answer> answers = parseAnswers(questionText);
			questionText = questionText.replaceAll("(\\s*[A-Z]+\\.\\s*[^\\n]+(?:\\n[^A-Z]+)*)", "").trim();
			questions.add(new Question(questionId, questionText, answers,isMultipleRightAnswers));
		}

		return questions;
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
		Pattern multipleRightAnswersPattern = Pattern.compile("(choose\\s+(all\\s+that\\s+apply|\\d+))", Pattern.CASE_INSENSITIVE);
		Matcher multipleRightAnswersMatcher = multipleRightAnswersPattern.matcher(questionText);
		return multipleRightAnswersMatcher.find();
	}
}

