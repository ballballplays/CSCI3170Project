package csci3170;

import java.util.*;
import java.sql.*;

class DriverInstance {
	static void start(Connection c) {
		while (true) {
			List<String> options = Arrays.asList("Take a request","Finish a trip","Check driver rating","Go back");
			int option = Welcome.getOption("Driver, what would you like to do?",options);
			switch (option) {
				case 1: {
					Scanner sc = new Scanner(System.in);
					int id = PassengerInstance.forceInt(sc,"Please enter your ID.");
					try {
						PreparedStatement prep = c.prepareStatement("SELECT * FROM Driver D WHERE D.did = ?"); //check if did exists
						prep.setInt(1,id);
						ResultSet result = prep.executeQuery();
						if (!result.next()) {
							System.out.println("[ERROR] Driver not found.\n");
							break;
						}
					} catch (SQLException ex) {
						System.out.println(ex.getMessage()+"\n");
					}
          //check if did is finished
          String query = "SELECT R.rid,P.name,R.passengers FROM Driver D, Request R, Passenger P,Vehicle V WHERE (R.pid = P.pid AND V.seats >= R.passengers AND R.myear <= V.myear AND LOWER(V.model) LIKE CONCAT(R.model, '%') AND D.did = ? AND D.vid = V.vid AND R.taken = 0)";
          //not sure if also need to lower case the r.model
					try {
						PreparedStatement prep2 = c.prepareStatement(query);
						prep2.setInt(1,id);
						ResultSet result = prep2.executeQuery();
						if (result.isLast()) {
							System.out.println("[ERROR] No request found.");
						} else {
							System.out.println("Request ID, Passenger Name, Passengers");
							while (result.next()) {
								System.out.print(result.getInt(1) + ", ");
								System.out.print(result.getString(2) + ", ");
								System.out.print(result.getInt(3));
								System.out.println();
							}
						}
						System.out.println();
						//System.out.println(" " + driverCount + " driver" + (driverCount==1?"":"s") + " are able to take the request.\n");
					} catch (SQLException ex) {
						System.out.println(ex.getMessage());
						break;
					}


					//int requestID = forceInt(sc,"Please enter the number of passengers.");
				} break;
			}
		}
	}
}
