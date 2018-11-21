package csci3170;

import java.util.*;
import java.sql.*;
import java.util.concurrent.TimeUnit;

class DriverInstance {
	static void start(Connection c) {
		while (true) {
			List<String> options = Arrays.asList("Take a request","Finish a trip","Check driver rating","Go back");
			int option = Welcome.getOption("Driver, what would you like to do?",options);
			switch (option) {
				case 1: {
					Scanner sc = new Scanner(System.in);
					int id = Utilities.forceInt(sc,"Please enter your ID.");
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
					//check driver is finished
					try {
						PreparedStatement prep2 = c.prepareStatement("SELECT * FROM Driver D, Trip T WHERE T.did = ? AND T.end IS NULL"); //check if did is finished
						prep2.setInt(1,id);
						ResultSet result = prep2.executeQuery();
						if (result.next()) {
							System.out.println("[ERROR] Driver is not finished.\n");
							break;
						}
					} catch (SQLException ex) {
						System.out.println(ex.getMessage()+"\n");
					}
					//query the available request
				ArrayList<Integer> arrli =  new ArrayList<Integer>();
         String query = "SELECT R.rid,P.name,R.passengers FROM Driver D, Request R, Passenger P,Vehicle V WHERE (R.pid = P.pid AND V.seats >= R.passengers AND R.myear <= V.myear AND LOWER(V.model) LIKE CONCAT(R.model, '%') AND D.did = ? AND D.vid = V.vid AND R.taken = 0)";
					try {
						PreparedStatement prep3 = c.prepareStatement(query);
						prep3.setInt(1,id);
						ResultSet result = prep3.executeQuery();
						if (!result.next()) {
							System.out.println("[ERROR] No request found.");
							break;
						} else {
							System.out.println("Request ID, Passenger Name, Passengers");
							do {
								int rid = result.getInt(1);
								arrli.add(rid);
								System.out.print(rid + ", ");
								System.out.print(result.getString(2) + ", ");
								System.out.print(result.getInt(3));
								System.out.println();
							} while (result.next());
						}
						//System.out.println(" " + driverCount + " driver" + (driverCount==1?"":"s") + " are able to take the request.\n");
					} catch (SQLException ex) {
						System.out.println(ex.getMessage());
						break;
					}
					//Selecting a request id and set taken = 1
					int selectedID = Utilities.forceInt(sc,"Please enter the request ID.");
					if (!arrli.contains(selectedID)) {
						System.out.println("[ERROR] The Above list does not contain this request ID");
						break;
					}
					try {
						PreparedStatement prep = c.prepareStatement("UPDATE Request R SET R.taken = 1 WHERE R.rid = ?");
						prep.setInt(1, selectedID);
						prep.executeUpdate();
					} catch (SQLException ex) {
						String message = ex.getMessage();
						if (message.contains("RequestExistence")) {
							System.out.println("[ERROR] Request not found.");
						} else {
							System.out.println(message);
						}
						System.out.println();
					}
					//query the request selectedID
					String query2 = "SELECT P.name,P.pid FROM Request R, Passenger P WHERE (R.rid = ? AND R.pid = P.pid)";
					String passengerName;
					int passengerID;
					try {
						PreparedStatement prep = c.prepareStatement(query2);
						prep.setInt(1,selectedID);
						ResultSet result = prep.executeQuery();
						result.next();
						passengerName = result.getString(1);
						passengerID = result.getInt(2);
					} catch (SQLException ex) {
						System.out.println(ex.getMessage());
						break;
					}
					//Count how many trips are there
					int count = 0;
					try {
						Statement st = c.createStatement();
						ResultSet result = st.executeQuery("SELECT COUNT(*) FROM Trip");
						result.next();
						count = result.getInt(1);
					} catch (SQLException ex) {
						System.out.println(ex.getMessage()+"\n");
						break;
					}

					//insert into trip
					try {
						PreparedStatement prep = c.prepareStatement("INSERT INTO Trip VALUES (?,?,?,?,?,?,?)");
						Timestamp timestamp = new Timestamp(System.currentTimeMillis());
						int tid = count + 1;
						prep.setInt(1,count+1);
						prep.setInt(2,id);
						prep.setInt(3,passengerID);
						prep.setTimestamp(4,timestamp);
						prep.setNull(5, Types.TIMESTAMP);
						prep.setInt(6,-1); //-1 for null fee
						prep.setInt(7,-1); //-1 for null rating
						prep.executeUpdate();
						System.out.println("Trip ID, Passenger name, Start");
						System.out.println(tid + ", " + passengerName + ", " + Utilities.dropLastNullable(timestamp.toString(),4));
					} catch (SQLException ex) {
						String message = ex.getMessage();
						if (message.contains("TripExistence")) {
							System.out.println("[ERROR] Trip not found.");
						} else {
							System.out.println(message);
						}
						System.out.println();
					}
				} break;

				case 2: {
					Scanner sc = new Scanner(System.in);
					int id = Utilities.forceInt(sc,"Please enter your ID.");
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
					/*//check driver is finished
					try {
						PreparedStatement prep2 = c.prepareStatement("SELECT * FROM Driver D, Trip T WHERE T.did = ? AND T.end IS NULL"); //check if did is finished
						prep2.setInt(1,id);
						ResultSet result = prep2.executeQuery();
						if (!result.next()) {
							System.out.println("[ERROR] There are no unfinished trips.\n");
							break;
						}
					} catch (SQLException ex) {
						System.out.println(ex.getMessage()+"\n");
					}*/
					String query = "SELECT T.tid,T.pid,T.start,P.name FROM Trip T,Passenger P WHERE (T.did = ? AND T.end IS NULL AND T.pid = P.pid)";
          int tid;
					int pid;
					Timestamp ts;
					String passengerName;
					try {
						PreparedStatement prep = c.prepareStatement(query);
						prep.setInt(1,id);
						ResultSet result = prep.executeQuery();
						if (!result.next()) {
							System.out.println("[ERROR] No unfinished trips found.");
							break;
						} else {
							tid = result.getInt(1);
							pid = result.getInt(2);
							ts = result.getTimestamp(3);
							passengerName = result.getString(4);
							System.out.println("Trip ID, Passenger ID, Start");
						  System.out.print(result.getInt(1) + ", ");
							System.out.print(result.getInt(2) + ", ");
							System.out.print(Utilities.dropLastNullable(result.getTimestamp(3).toString(),2)); //substring 2
							System.out.println();
						}
						//System.out.println(" " + driverCount + " driver" + (driverCount==1?"":"s") + " are able to take the request.\n");
					} catch (SQLException ex) {
						System.out.println(ex.getMessage());
						break;
					}
					System.out.println("Do you wish to finish the trip? [y/n]");
					//update trip table
					String answer = sc.nextLine();
					if (answer.equals("y")) {
						try {
							Timestamp endTimeStamp = new Timestamp(System.currentTimeMillis());
							int fee;
							long diff = endTimeStamp.getTime() - ts.getTime();
							long minute = TimeUnit.MILLISECONDS.toMinutes(diff);
							fee = (int) minute;
							System.out.println(fee);
							PreparedStatement prep = c.prepareStatement("UPDATE Trip T SET T.start = ?, T.end = ?, T.fee = ? WHERE T.tid = ?");
							prep.setTimestamp(1, ts);
							prep.setTimestamp(2, endTimeStamp);
							prep.setInt(3,fee);
							prep.setInt(4,tid);
							prep.executeUpdate();
							System.out.println("Trip ID, Passenger name, Start, End, Fee");
							System.out.println(tid + ", " + passengerName + ", " + Utilities.dropLastNullable(ts.toString(),2) + ", " + Utilities.dropLastNullable(endTimeStamp.toString() ,4) + ", " + fee);
						} catch (SQLException ex) {
							String message = ex.getMessage();
							if (message.contains("RequestExistence")) {
								System.out.println("[ERROR] Request not found.");
							} else {
								System.out.println(message);
							}
							System.out.println();
						}
					}else if (!answer.equals("n")) {
						System.out.println("[ERROR] Invalid input.");
						break;
					}
				} break;
				case 3: {
					Scanner sc = new Scanner(System.in);
					int id = Utilities.forceInt(sc,"Please enter your ID.");
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
					String query = "SELECT COUNT(S.rating),AVG(S.rating) FROM (SELECT T.rating FROM Trip T WHERE T.did = ? AND T.rating > -1 ORDER BY T.tid DESC LIMIT 5)S";
					try {
						PreparedStatement prep3 = c.prepareStatement(query);
						prep3.setInt(1,id);
						ResultSet result = prep3.executeQuery();
						result.next();
						int count = result.getInt(1);
						if (count < 5) {
							System.out.println("Rating is not yet determined, please ask for more rating.");
							break;
						}else if (count == 5) {
							System.out.print("Your driver rating is " + Math.round(result.getFloat(2) * 100.0) / 100.0 + ".");
							System.out.println();
						}
						//System.out.println(" " + driverCount + " driver" + (driverCount==1?"":"s") + " are able to take the request.\n");
					} catch (SQLException ex) {
						System.out.println(ex.getMessage());
						break;
					}
				}break;
				case 4: {
					System.out.println();
				} return;
			}
		}
	}
}
