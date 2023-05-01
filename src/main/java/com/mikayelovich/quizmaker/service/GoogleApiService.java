package com.mikayelovich.quizmaker.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.PermissionList;
import com.google.api.services.forms.v1.Forms;
import com.google.api.services.forms.v1.FormsScopes;
import com.google.api.services.forms.v1.model.*;
import com.google.auth.oauth2.GoogleCredentials;
import com.mikayelovich.quizmaker.model.Answer;
import com.mikayelovich.quizmaker.model.QuestionModel;
import com.mikayelovich.quizmaker.util.GoogleChoiceQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.mikayelovich.quizmaker.util.FunctionalUtils.mapAnswerToCorrectAnswer;
import static com.mikayelovich.quizmaker.util.FunctionalUtils.mapAnswerToOption;

@Service
@RequiredArgsConstructor
public class GoogleApiService {

	@Value("${application.google-account-key-json-path}")
	private Resource formCreateScriptId1;

	@Value("${application.make-google-form}")
	private Boolean isCreateGoogleForm;

	@Value("${application.google-form-default-point-for-question}")
	private int defaultPointsForQuestion;

	@Autowired
	private Forms googleFormsService;

	@Autowired
	private Drive googleDriveService;

public Optional<String> makeNewForm(String title, List<QuestionModel> questions){
	try {
		return Optional.of(createNewForm(getAccessToken(), title, questions));
	} catch (Exception e) {
		System.err.println(e);
	}
	return Optional.empty();
}

	private String createNewForm(String token, String title, List<QuestionModel> questionModels) throws IOException {
		Form form = new Form();
		form.setInfo(new Info());
		form.getInfo().setTitle(title);
		QuizSettings quizSettings = new QuizSettings();
		form.getSettings().setQuizSettings(quizSettings);

		List<Item> items = form.getItems();
		addQuestionsToItems(questionModels, items);
		quizSettings.setIsQuiz(true);

		form = googleFormsService.forms().create(form)
				.setAccessToken(token)
				.execute();
		return form.getResponderUri();
	}

	private void addQuestionsToItems(List<QuestionModel> questionModels, List<Item> items) {
		for (QuestionModel questionModel : questionModels) {
			Item item = new Item();
			items.add(item);

			QuestionItem questionItem = new QuestionItem();
			item.setQuestionItem(questionItem);

			Question question = new Question();
			questionItem.setQuestion(question);

			question.setRequired(true);

			ChoiceQuestion choiceQuestion = new ChoiceQuestion();
			question.setChoiceQuestion(choiceQuestion);

			if (questionModel.isMultipleRightAnswers()) {
				question.getChoiceQuestion().setType(GoogleChoiceQuestion.CHECKBOX.toString());
			} else {
				question.getChoiceQuestion().setType(GoogleChoiceQuestion.RADIO.toString());
			}

			List<Option> options = questionModel.getAnswers().stream().map(mapAnswerToOption).toList();
			choiceQuestion.setOptions(options);


			//there is a way to set separate feedbacks depended on right/wrong answer
			addGradingWithCorrectAnswers(questionModel, question);
		}
	}

	private void addGradingWithCorrectAnswers(QuestionModel questionModel, Question question) {
		Grading grading = new Grading();
		question.setGrading(grading);

		grading.setPointValue(defaultPointsForQuestion);
		generageCorrectAnswers(questionModel.getAnswers());

		Feedback feedback = new Feedback();
		feedback.setText(questionModel.getExplanation());
		grading.setGeneralFeedback(feedback);
	}

	private static CorrectAnswers generageCorrectAnswers(List<Answer> answerList) {
		CorrectAnswers correctAnswers = new CorrectAnswers();

		List<CorrectAnswer> correctAnswerList = answerList.stream().filter(Answer::isRight)
				.map(mapAnswerToCorrectAnswer).toList();
		correctAnswers.setAnswers(correctAnswerList);
		return correctAnswers;
	}

	private String getAccessToken() throws IOException {
		GoogleCredentials credential = GoogleCredentials.fromStream(formCreateScriptId1.getInputStream())
				.createScoped(FormsScopes.all());

		return credential.getAccessToken() != null ?
				credential.getAccessToken().getTokenValue() :
				credential.refreshAccessToken().getTokenValue();
	}

//	public void createQuiz(List<QuestionModel> questionModels) throws GeneralSecurityException, IOException {
//		// Set up the HttpRequestFactory
//
//				// Create the Google Form using the createGoogleForm method
//		String formId = createGoogleForm("Your Quiz Title");
//
//		// Add questions to the form
//		for (QuestionModel questionModel : questionModels) {
//			String questionText = questionModel.getQuestion();
//			List<Answer> answers = questionModel.getAnswers();
//			boolean isMultipleRightAnswers = questionModel.isMultipleRightAnswers();
//
//			// Convert the answers to the format required by the Google Forms API
//			List<String> answerTexts = answers.stream().map(Answer::getText).collect(Collectors.toList());
//
//			// Add the question to the form
////			addQuestionToForm(requestFactory, formId, questionText, answerTexts, isMultipleRightAnswers);
//		}
//
//		// Set the form to be a quiz, assign points, and make questions mandatory
////		configureFormAsQuiz(requestFactory, formId, questions);
//	}

	private boolean publishForm(String formId, String token) throws IOException {

		PermissionList list = googleDriveService.permissions().list(formId).setOauthToken(token).execute();

		if (list.getPermissions().stream().filter(it -> it.getRole().equals("reader")).findAny().isEmpty()) {
			Permission body = new Permission();
			body.setRole("reader");
			body.setType("anyone");
			googleDriveService.permissions().create(formId, body).setOauthToken(token).execute();
			return true;
		}

		return false;
	}
}
