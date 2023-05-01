package com.mikayelovich.quizmaker;

import com.mikayelovich.quizmaker.model.QuestionModel;
import com.mikayelovich.quizmaker.model.QuestionWithAnswersRaw;
import com.mikayelovich.quizmaker.service.QuestionParser;
import com.mikayelovich.quizmaker.service.ReviewParser;
import com.mikayelovich.quizmaker.util.IOUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

@SpringBootApplication
@RequiredArgsConstructor
public class QuizmakerApplication {

	private final QuestionParser questionParser;
	private final ReviewParser reviewParser;

	public static void main(String[] args) {
		SpringApplication.run(QuizmakerApplication.class, args);
	}


	@Bean
	public CommandLineRunner run() {
		return (String... args) -> {
			Resource[] resources = IOUtils.collectQuestionResources();
			IOUtils.verifyIsAnswersExists(resources);

			List<QuestionWithAnswersRaw> questionWithAnswersRawList = IOUtils.generateQuestionsWithAnswersRaw(resources);
			for (QuestionWithAnswersRaw questionWithAnswersRaw : questionWithAnswersRawList) {

				List<QuestionModel> questionModelModels = questionParser.parseQuestions(questionWithAnswersRaw.getQuestions());
				reviewParser.mergeReview(questionWithAnswersRaw.getAnswers(), questionModelModels);
				questionModelModels.forEach(q -> System.out.println("\n" + q));
				System.out.println("\n\n\n");
			}
			System.exit(0);
		};
	}

	private QuestionWithAnswersRaw readQuestionsWithAnswers() throws IOException {
		String questions = loadFromFile("WelcomeToJava");
		String answers = loadFromFile("WelcomeToJavaAnswers");
		return new QuestionWithAnswersRaw(questions, answers);
	}


	public String loadFromFile(String fileNameWithoutExtension) throws IOException {
		File file = ResourceUtils.getFile("classpath:" + fileNameWithoutExtension + ".qz");
		List<String> questionLines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
		return String.join("\n", questionLines);
	}


}
