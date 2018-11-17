package csci3170;

import java.util.*;

class PassengerInstance {
	static void start() {
		while (true) {
			List<String> options = Arrays.asList("Request a ride","Check trip records","Rate a trip","Go back");
			int option = Welcome.getOption("Passenger, what would you like to do?",options);
			switch (option) {
				case 1: {
					Scanner sc = new Scanner(System.in);
					int id = forceInt(sc,"Please enter your ID.");
				} break;
				
				case 2: {
				} break;
				
				case 3: {
				} break;
				
				case 4: {
					System.out.println();
				} return;
			}
		}
	}
	
	static int forceInt(Scanner sc, String question) {
		do {
			System.out.println(question);
			String input = sc.nextLine();
			try {
				int inputInt = Integer.parseInt(input);
				return inputInt;
			} catch (Exception ex) {
				System.out.println("[ERROR] Invalid input.");
			}
		} while (true);
	}
}
