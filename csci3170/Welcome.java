package csci3170;

import java.util.*;
import java.sql.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.*;

public class Welcome {
	public static void main(String[] args) {
		printWelcomeDialog();
	}

	public static int getOption(String question, List<String> options) {
		System.out.println(question);
		int count = 0;
		for (String option: options) {
			count++;
			System.out.println(count + ". " + option);
		}
		final int size = count;
		boolean valid = false;
		int input = 0;
		Scanner sc = new Scanner(System.in);
		do {
			System.out.println("Please enter [1-" + count + "].");
			try {
				input = sc.nextInt();
				if (input >= 1 && input <= size) {
					valid = true;
				}
			} catch (Exception ex) {
				valid = false;
			}
			if (!valid) {
				System.out.println("[ERROR] Invalid input.");
			}
		} while (!valid);
		//sc.close(); //no you cannot close System.in
		return input;
	}

	static Connection connectDatabase() { //return whether connection is successful
		String databaseAddress = "jdbc:mysql://projgw.cse.cuhk.edu.hk:2633/db36";
		String databaseUsername = "Group36";
		String databasePassword = readPassword(); //change later!
		Connection c = null;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			c = DriverManager.getConnection(databaseAddress,databaseUsername,databasePassword);
		} catch (ClassNotFoundException ex) {
			System.out.println("[ERROR]: Java MySQL DB Driver not found!!");
			System.exit(0);
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
			return null;
		}
		return c;
	}

	static void printWelcomeDialog() {
		Connection c = connectDatabase();
		if (c == null) return;
		outwhile:
		while (true) {
			List<String> identityOptions = Arrays.asList("An administrator", "A passenger", "A driver", "None of the above");
			int identity = getOption("Welcome! Who are you?", identityOptions);
			System.out.println();
			switch (identity) {
				case 1:
					AdminInstance.start(c);
					break;
				case 2:
					PassengerInstance.start(c);
					break;
				case 3:
					DriverInstance.start(c);
					break;
				case 4:
					//then why are you here?
					System.out.println("Then why are you here?");
					break outwhile;
				default: throw new InternalError("Cannot happen");
			}
		}
	}
	
	static String readPassword() {
		try {
			return Files.readAllLines(new File("password").toPath(),Charset.forName("UTF-8")).get(0);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			System.exit(0);
			throw new InternalError();
		}
	}
}
