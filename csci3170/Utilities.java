package csci3170;

import java.util.*;
import java.sql.*;

final class Utilities {
	private Utilities() {
		/*
		 * Utility class. Do not construct any instance.
		 */
		throw new InternalError();
	}
	
	static String dropLastNullable(String s, int n) {
		if (s == null || s.length() < n) {
			return s;
		} else {
			return s.substring(0,s.length()-n);
		}
	}
	
	static String prettifyNull(String s) {
		if (s != null) {
			return s;
		} else {
			return "[UNKNOWN]";
		}
	}
	
	static String prettifyRating(int rating) {
		if (rating <= 5 && rating >= 1) {
			return rating + "";
		} else {
			return "[UNKNOWN]";
		}
	}
	
	static int forceInt(Scanner sc, String question) {
		return forceNaturalNumber(sc,question,false);
	}
	
	static int forceNaturalNumber(Scanner sc, String question, boolean allowEmpty) {
		do {
			System.out.println(question);
			String input = sc.nextLine();
			try {
				if (input.equals("") && allowEmpty) {
					return -1;
				}
				int inputInt = Integer.parseInt(input);
				return inputInt;
			} catch (Exception ex) {
				System.out.println("[ERROR] Invalid input.");
			}
		} while (true);
	}
	
	static Timestamp forceTimestamp(Scanner sc, String question, boolean startDate) {
		do {
			System.out.println(question);
			String input = sc.nextLine();
			try {
				Timestamp stamp = Timestamp.valueOf(input + (startDate?" 00:00:00":" 23:59:59"));
				return stamp;
			} catch (Exception ex) {
				System.out.println("[ERROR] Invalid input.");
			}
		} while (true);
	}
}