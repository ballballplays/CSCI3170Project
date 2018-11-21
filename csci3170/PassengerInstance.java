package csci3170;

import java.util.*;
import java.sql.*;
import static csci3170.Utilities.*;

class PassengerInstance {
	static void start(Connection c) {
		while (true) {
			List<String> options = Arrays.asList("Request a ride","Check trip records","Rate a trip","Go back");
			int option = Welcome.getOption("Passenger, what would you like to do?",options);
			switch (option) {
				case 1: {
					Scanner sc = new Scanner(System.in);
					int id = forceInt(sc,"Please enter your ID.");
					int passengerCount = forceInt(sc,"Please enter the number of passengers.");
					int earliestYear = forceNaturalNumber(sc,"Please enter the earlist model year. (Press enter to skip)",true);
					System.out.println("Please enter the model. (Press enter to skip)");
					String model = sc.nextLine();
					//NOTE: we have to make up requestID
					int count = 0;
					try (Statement st = c.createStatement()) {
						ResultSet result = st.executeQuery("SELECT COUNT(*) AS Number FROM Request");
						result.next();
						count = result.getInt(1);
					} catch (SQLException ex) {
						System.out.println(ex.getMessage()+"\n");
						break;
					}
					String query = "SELECT COUNT(*) AS Number FROM Vehicle V, Driver D WHERE (D.vid = V.vid AND V.seats >= ?" + (earliestYear != -1?" AND V.myear >= ?":"") + (model.equals("")?"":" AND LOWER(V.model) LIKE ?") + ")";
					int driverCount = -1;
					try (PreparedStatement prep = c.prepareStatement(query)) {
						prep.setInt(1,passengerCount);
						int previousArgumentCount = 1;
						if (earliestYear != -1) {
							prep.setInt(2,earliestYear);
							previousArgumentCount++;
						}
						if (!(model.equals(""))) {
							prep.setString(previousArgumentCount+1,model+"%");
						}
						ResultSet result = prep.executeQuery();
						result.next();
						driverCount = result.getInt(1);
						if (driverCount == 0) {
							System.out.println("[ERROR] No matching driver. Please relax conditions.\n");
							return;
						}
						//System.out.println(" " + driverCount + " driver" + (driverCount==1?"":"s") + " are able to take the request.\n");
					} catch (SQLException ex) {
						System.out.println(ex.getMessage());
						break;
					}
					try (PreparedStatement prep = c.prepareStatement("INSERT INTO Request VALUES (?,?,?,?,?,?)")) {
						prep.setInt(1,count+1);
						prep.setInt(2,id);
						prep.setInt(3,earliestYear);
						prep.setString(4,model);
						prep.setInt(5,passengerCount);
						prep.setInt(6,0); //0 for not taken
						prep.executeUpdate();
						System.out.println("Your request is placed. " + driverCount + " driver" + (driverCount!=1?"s":"") + " are able to take the request.\n");
					} catch (SQLException ex) {
						String message = ex.getMessage();
						if (message.contains("PassengerExistence")) {
							System.out.println("[ERROR] Passenger not found.");
						} else {
							System.out.println(message);
						}
						System.out.println();
					}
				} break;
				
				case 2: { //check trip records
					Scanner sc = new Scanner(System.in);
					int pid = forceInt(sc,"Please enter your ID.");
					Timestamp startTime = forceTimestamp(sc,"Please enter the start date.",true);
					Timestamp endTime = forceTimestamp(sc,"Please enter the end date.",false);
					try (PreparedStatement prep = c.prepareStatement("SELECT * FROM Passenger P WHERE P.pid = ?")) {
						prep.setInt(1,pid);
						ResultSet result = prep.executeQuery();
						if (!result.next()) {
							System.out.println("[ERROR] Passenger not found.\n");
							break;
						}
					} catch (SQLException ex) {
						System.out.println(ex.getMessage()+"\n");
					}
					try (PreparedStatement prep = c.prepareStatement("SELECT T.tid, D.name, V.vid, V.model, T.start, T.end, T.fee, T.rating FROM Trip T, Driver D, Vehicle V WHERE (T.pid = ? AND T.did = D.did AND V.vid = D.vid AND T.start >= ? AND T.end <= ?) ORDER BY T.tid DESC")) {
						prep.setInt(1,pid);
						prep.setTimestamp(2,startTime);
						prep.setTimestamp(3,endTime);
						ResultSet result = prep.executeQuery();
						if (result.isLast()) {
							System.out.println("[ERROR] No trip found.");
						} else {
							System.out.println("Trip ID, Driver Name, Vehicle ID, Vehicle Model, Start, End, Fee, Rating");
							while (result.next()) {
								System.out.print(result.getInt(1) + ", ");
								System.out.print(result.getString(2) + ", ");
								System.out.print(result.getString(3) + ", ");
								System.out.print(result.getString(4) + ", ");
								System.out.print(prettifyNull(dropLastNullable(result.getTimestamp(5).toString(),2)) + ", ");
								System.out.print(prettifyNull(dropLastNullable(result.getTimestamp(6).toString(),2)) + ", ");
								System.out.print(result.getInt(7) + ", ");
								System.out.println(Utilities.prettifyRating(result.getInt(8)));
							}
						}
						System.out.println();
					} catch (SQLException ex) {
						System.out.println(ex.getMessage()+"\n");
					}
				} break;
				
				case 3: {
					Scanner Sc = new Scanner(System.in);
					int pid = forceInt(Sc, "Please enter your ID.");
					int tripId = forceInt(Sc, "Please enter your trip ID.");
					boolean valid = false;
					int rating = -1;
					do{
						System.out.println("Please enter the rating.");
						try{
							rating = Sc.nextInt();
							if(rating>=1 && 5>=rating){
								valid=true;
							}
						}	catch (Exception ex){
							valid = false;
						}
						if (!valid){
							System.out.println("[ERROR] Invalid input.");
						}
					} while (!valid);
					String query = "UPDATE Trip SET rating = ? WHERE pid = ? AND tid = ?";
					try (PreparedStatement prep = c.prepareStatement(query)){
						prep.setInt(1,rating);
						prep.setInt(2,pid);
						prep.setInt(3,tripId);
						int numOfChanges = prep.executeUpdate();
						if(numOfChanges==0){
							System.out.println("[Error] No trip found.");
							break;
						} else{
							System.out.println("Trip ID, Driver Name, Vehicle ID, Vehicle Model, Start, End, Fee, Rating");
							String newquery = "SELECT T.tid, D.name, V.vid, V.model, T.start, T.end, T.fee, T.rating FROM Trip T, Driver D, Vehicle V WHERE (T.pid = ? AND T.did = D.did AND V.vid = D.vid AND T.tid = ?)";

							PreparedStatement prep2 = c.prepareStatement(newquery);
							prep2.setInt(1, pid);
							prep2.setInt(2, tripId);
							ResultSet result = prep2.executeQuery();
							while (result.next()){
								System.out.print(result.getInt(1) + ", ");
								System.out.print(result.getString(2) + ", ");
								System.out.print(result.getString(3) + ", ");
								System.out.print(result.getString(4) + ", ");
								System.out.print(prettifyNull(dropLastNullable(result.getTimestamp(5).toString(),2)) + ", ");
								System.out.print(prettifyNull(dropLastNullable(result.getTimestamp(6).toString(),2)) + ", ");
								System.out.print(result.getInt(7) + ", ");
								System.out.println(Utilities.prettifyRating(result.getInt(8)));
							}
						}
						
					}
					catch (SQLException ex){
						System.out.println(ex.getMessage()+"\n");
					}
				} break;
				
				case 4: {
					System.out.println();
				} return;
			}
		}
	}
	
	@Deprecated
	static int forceInt(Scanner sc, String question) {
		return Utilities.forceInt(sc,question);
	}
	
	@Deprecated
	static int forceNaturalNumber(Scanner sc, String question, boolean allowEmpty) {
		return Utilities.forceNaturalNumber(sc,question,allowEmpty);
	}
	
	@Deprecated
	static Timestamp forceTimestamp(Scanner sc, String question, boolean startDate) {
		return Utilities.forceTimestamp(sc,question,startDate);
	}
		
	@Deprecated
	static String dropLast(String s, int n) {
		System.out.println("NOTE: This function is not null-safe. Please consider using Utilities.dropLastNullable(null,n) to return null.");
		return s.substring(0,s.length()-n);
	}
	
	@Deprecated
	static String prettifyNull(String s) {
		return Utilities.prettifyNull(s);
	}
}
