package com.mikayelovich.quizmaker;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.forms.v1.Forms;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Configuration
public class GoogleConfig {

	@Value("${application.name}")
	private String appName;

	@Bean
	public Drive getGoogleDrive() {
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

		try {
			return new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory,
					null).setApplicationName(appName).build();
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Bean
	public Forms getForms() {
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

		try {
			return new Forms.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory,
					null).setApplicationName(appName).build();
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

