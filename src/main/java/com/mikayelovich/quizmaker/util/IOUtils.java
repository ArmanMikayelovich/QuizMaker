package com.mikayelovich.quizmaker.util;

import com.mikayelovich.quizmaker.model.QuestionWithAnswersRaw;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ResourceUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class IOUtils {
	private static final String FILE_EXTENSION_POSTFIX_FOR_ANSWERS = "a";

	private IOUtils() {

	}

	public static Resource[] collectQuestionResources() throws IOException {
		String directoryPath = getExecutableLocation();

		// Read all files from a directory using the JAR location
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		return resolver.getResources("file:" + directoryPath + "*.qz");
	}

	public static void verifyIsAnswersExists(Resource[] questionResources) throws IOException {
		for (Resource resource : questionResources) {
			String questionsFileName = resource.getFilename();
			String answersFileName = generateAnswersFilePath(resource);
			if (!new File(answersFileName).exists()) {
				throw new IllegalArgumentException("Answers not found for Questions:" + questionsFileName);
			}
			System.out.println("question File name: " + questionsFileName);
			System.out.println("answers File name: " + answersFileName);
		}
	}


	public static List<QuestionWithAnswersRaw> generateQuestionsWithAnswersRaw(Resource[] questionResources) throws IOException {
		List<QuestionWithAnswersRaw> questionWithAnswersRawList = new ArrayList<>();
		for (Resource questionResource : questionResources) {
			String questionsPath = generateQuestionFilePath(questionResource);
			String answersPath = generateAnswersFilePath(questionResource);

			String rawQuestions = readAllLinesAsString(questionsPath);
			String rawAnswers = readAllLinesAsString(answersPath);
			QuestionWithAnswersRaw questionWithAnswersRaw = new QuestionWithAnswersRaw(rawQuestions, rawAnswers);
			questionWithAnswersRawList.add(questionWithAnswersRaw);
		}
		return questionWithAnswersRawList;
	}

	private static String readAllLinesAsString(String filePath) {
		StringBuilder stringBuilder = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(System.lineSeparator());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return stringBuilder.toString();
	}

	private static String generateAnswersFilePath(Resource questionResource) {
		return generateQuestionFilePath(questionResource) + FILE_EXTENSION_POSTFIX_FOR_ANSWERS;
	}

	private static String generateQuestionFilePath(Resource questionResource) {
		try {
			return questionResource.getFile().getPath();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static String getExecutableLocation() throws IOException {
		Resource resource = new ClassPathResource("");
		return ResourceUtils.getFile(resource.getURL()).getPath() + '/';
	}

}
