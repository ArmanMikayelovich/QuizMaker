package com.mikayelovich.quizmaker;

import com.mikayelovich.quizmaker.model.Question;
import com.mikayelovich.quizmaker.util.QuestionParser;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
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
			File file = loadEmployeesWithSpringInternalClass();
			List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
			String fullFileText = String.join("\n", lines);

			List<Question> questionModels = QuestionParser.parseQuestions(fullFileText);
			questionModels.forEach(System.out::println);
			System.exit(0);
		};
	}

	public File loadEmployeesWithSpringInternalClass() throws FileNotFoundException {
		return ResourceUtils.getFile("classpath:questions.qz");
	}

}
