package com.mikayelovich.quizmaker;

import com.mikayelovich.quizmaker.model.QuestionModel;
import com.mikayelovich.quizmaker.model.QuestionWithAnswersTuple;
import com.mikayelovich.quizmaker.util.QuestionParser;
import com.mikayelovich.quizmaker.util.ReviewParser;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

@SpringBootApplication
public class QuizmakerApplication {


	public static void main(String[] args) {
		SpringApplication.run(QuizmakerApplication.class, args);
	}


	@Bean
	public CommandLineRunner run() {
		return (String... args) -> {
			QuestionWithAnswersTuple questionWithAnswersTuple = readQuestionsWithAnswers();

			List<QuestionModel> questionModels = QuestionParser.parseQuestions(questionWithAnswersTuple.getQuestions());
			ReviewParser.mergeReview(questionWithAnswersTuple.getAnswers(),questionModels);
			questionModels.forEach(q -> System.out.println("\n" + q));
			System.exit(0);
		};
	}

	private QuestionWithAnswersTuple readQuestionsWithAnswers() throws IOException {
		String questions= loadFromFile("WelcomeToJava");
		String answers= loadFromFile("WelcomeToJavaAnswers");
		return new QuestionWithAnswersTuple(questions, answers);
	}


	public String loadFromFile(String fileNameWithoutExtension) throws IOException {
		File file = ResourceUtils.getFile("classpath:" + fileNameWithoutExtension + ".qz");
		List<String> questionLines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
		return String.join("\n", questionLines);
	}


}
