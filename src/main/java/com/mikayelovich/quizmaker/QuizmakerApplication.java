package com.mikayelovich.quizmaker;

import com.mikayelovich.quizmaker.model.QuestionModel;
import com.mikayelovich.quizmaker.model.QuestionWithAnswersRaw;
import com.mikayelovich.quizmaker.service.GoogleApiService;
import com.mikayelovich.quizmaker.service.QuestionParser;
import com.mikayelovich.quizmaker.service.ReviewParser;
import com.mikayelovich.quizmaker.util.ConsoleUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootApplication
@RequiredArgsConstructor
public class QuizmakerApplication {

	private final QuestionParser questionParser;
	private final ReviewParser reviewParser;

	private final GoogleApiService googleApiService;

	public static void main(String[] args) {
		SpringApplication.run(QuizmakerApplication.class, args);
	}


	@Bean
	public CommandLineRunner run() {
		return (String... args) -> {
			Resource[] resources = IOUtils.collectQuestionResources();
			IOUtils.verifyIsAnswersExists(resources);

			HashMap<String, List<QuestionModel>> topicNameWithQuestions = new HashMap<>();

			List<QuestionWithAnswersRaw> questionWithAnswersRawList = IOUtils.generateQuestionsWithAnswersRaw(resources);
			for (QuestionWithAnswersRaw questionWithAnswersRaw : questionWithAnswersRawList) {
				String topicName = questionWithAnswersRaw.getFileName();
				System.out.println(ConsoleUtils.makeLineColorful(ConsoleUtils.Color.YELLOW, topicName));
				System.out.println(ConsoleUtils.makeLineColorful(ConsoleUtils.Color.YELLOW, topicName));
				System.out.println(ConsoleUtils.makeLineColorful(ConsoleUtils.Color.YELLOW, topicName));
				List<QuestionModel> questionModelModels = questionParser.parseQuestions(questionWithAnswersRaw.getQuestions());
				reviewParser.mergeReview(questionWithAnswersRaw.getAnswers(), questionModelModels);
				questionModelModels.forEach(q -> System.out.println("\n" + q));
				System.out.println("\n\n\n");

				topicNameWithQuestions.put(topicName, questionModelModels);
			}


			System.out.println("printed, now publishing");
			System.out.println("printed, now publishing");
			System.out.println("printed, now publishing");
			System.out.println("printed, now publishing");
			System.out.println("printed, now publishing");

			for (Map.Entry<String, List<QuestionModel>> topicWithQuestions : topicNameWithQuestions.entrySet()) {
				Optional<String> responderUrlOptional = googleApiService.makeNewForm(topicWithQuestions.getKey(), topicWithQuestions.getValue());
				responderUrlOptional.ifPresent(url -> System.out.println(ConsoleUtils.makeLineColorful(ConsoleUtils.Color.GREEN, url)));
			}

			System.exit(0);
		};
	}


}
