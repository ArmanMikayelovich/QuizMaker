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
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.mikayelovich.quizmaker.util.FunctionalUtils.mapAnswerToCorrectAnswer;
import static com.mikayelovich.quizmaker.util.FunctionalUtils.mapAnswerToOption;

@Service
@RequiredArgsConstructor
public class GoogleApiService {

	@Value("${application.google-account-key-json-path}")
	private String jsonKeyFullPath;

	@Value("${application.make-google-form}")
	private Boolean isCreateGoogleForm;

	@Value("${application.google-form-default-point-for-question}")
	private int defaultPointsForQuestion;

	@Autowired
	private Forms googleFormsService;

	@Autowired
	private Drive googleDriveService;

	public Optional<String> makeNewForm(String title, List<QuestionModel> questions) {
		try {
			String accessToken = getAccessToken();
			Form form = createNewForm(accessToken, title, questions);
			transformToQuiz(form.getFormId(), accessToken);
			batchUpdateAddQuestions(questions, form.getFormId(), accessToken);
			publishForm(form.getFormId(), accessToken);
			return Optional.of(form.getResponderUri());
		} catch (Exception e) {
			System.err.println(e);
		}
		return Optional.empty();
	}

	private Form createNewForm(String token, String title, List<QuestionModel> questionModels) throws IOException {
		Form form = new Form();
		form.setInfo(new Info());
		form.getInfo().setTitle(title);
//		FormSettings formSettings = new FormSettings();
//		form.setSettings(formSettings);
//		QuizSettings quizSettings = new QuizSettings();
//		formSettings.setQuizSettings(quizSettings);
//		form.setItems(new ArrayList<>());
//		addQuestionsToItems(questionModels, form.getItems());
//		quizSettings.setIsQuiz(true);

		form = googleFormsService.forms().create(form)
				.setAccessToken(token)
				.execute();
		return form;
	}
	private void transformToQuiz(String formId, String token) throws IOException {
		BatchUpdateFormRequest batchRequest = new BatchUpdateFormRequest();
		Request request = new Request();
		request.setUpdateSettings(new UpdateSettingsRequest());
		request.getUpdateSettings().setSettings(new FormSettings());
		request.getUpdateSettings().getSettings().setQuizSettings(new QuizSettings());
		request.getUpdateSettings().getSettings().getQuizSettings().setIsQuiz(true);
		request.getUpdateSettings().setUpdateMask("quizSettings.isQuiz");
		batchRequest.setRequests(Collections.singletonList(request));
		googleFormsService.forms().batchUpdate(formId, batchRequest)
				.setAccessToken(token).execute();
	}

	public void batchUpdateAddQuestions(List<QuestionModel> questionModels, String formId, String accessToken) {
		BatchUpdateFormRequest batchRequest = new BatchUpdateFormRequest();
		ArrayList<Request> requestList = new ArrayList<>();
		batchRequest.setRequests(requestList);

		List<Item> items = mapQuestionsToItemsForCreateRequest(questionModels);
		for (int i = 0; i < items.size(); i++) {
			Item item = items.get(i);
			Request request = new Request();
			requestList.add(request);
			CreateItemRequest createItemRequest = new CreateItemRequest();
			request.setCreateItem(createItemRequest);
			Location location = new Location();
			createItemRequest.setLocation(location);

			location.setIndex(i);
			createItemRequest.setItem(item);
		}



		try {
			googleFormsService.forms().batchUpdate(formId, batchRequest)
					.setAccessToken(accessToken).execute();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private List<Item> mapQuestionsToItemsForCreateRequest(List<QuestionModel> questionModels) {
		List<Item> items = new ArrayList<>();
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
			question.getChoiceQuestion().setOptions(options);

			//there is a way to set separate feedbacks depended on right/wrong answer
			addGradingWithCorrectAnswersAndFeedback(questionModel, question);
		}
		return items;
	}

	private void addGradingWithCorrectAnswersAndFeedback(QuestionModel questionModel, Question question) {
		Grading grading = new Grading();
		question.setGrading(grading);

		grading.setPointValue(defaultPointsForQuestion);
		CorrectAnswers correctAnswers = generageCorrectAnswers(questionModel.getAnswers());
		grading.setCorrectAnswers(correctAnswers);

		Feedback feedback = new Feedback();
		feedback.setText(questionModel.getExplanation());
		grading.setGeneralFeedback(feedback);
	}


	private String getAccessToken() throws IOException {

		try (FileInputStream fileInputStream = new FileInputStream(jsonKeyFullPath);) {
			GoogleCredentials credential = GoogleCredentials.fromStream(fileInputStream)
					.createScoped(FormsScopes.all());

			return credential.getAccessToken() != null ?
					credential.getAccessToken().getTokenValue() :
					credential.refreshAccessToken().getTokenValue();
		}
	}


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

	private static CorrectAnswers generageCorrectAnswers(List<Answer> answerList) {
		CorrectAnswers correctAnswers = new CorrectAnswers();

		List<CorrectAnswer> correctAnswerList = answerList.stream().filter(Answer::isRight)
				.map(mapAnswerToCorrectAnswer).toList();
		correctAnswers.setAnswers(correctAnswerList);
		return correctAnswers;
	}
}
