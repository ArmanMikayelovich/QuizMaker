package com.mikayelovich.quizmaker.util;

import static com.mikayelovich.quizmaker.util.ConsoleUtils.Color.RESET;

public final class ConsoleUtils {

	public static String makeLineColorful(Color color, String lineText) {
		return color.getCode() + lineText + RESET.getCode();
	}


	public enum Color {
		RESET("\u001B[0m"), RED("\u001B[31m"), GREEN("\u001B[32m"), YELLOW("\u001B[33m"),
		BLUE("\u001B[34m");

		private final String code;

		Color(String code) {
			this.code = code;
		}

		private String getCode() {
			return code;
		}
	}


}
