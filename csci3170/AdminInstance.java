package csci3170;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.nio.*;
import java.nio.file.*;
import java.nio.charset.*;

class AdminInstance {
	static void start(Connection c) {
		while (true) {
			List<String> options = Arrays.asList("Create tables","Delete tables","Load data","Check data","Go back");
			int option = Welcome.getOption("Administrator, what would you like to do?",options);
			switch (option) {
				case 1: {
					/*
					 * create tables
					 * Relational Schema:
					 * driver(_id_,name,vehicle_id)
					 * vehicle(_id_,model,model_year,seats)
					 * passenger(_id,name)
					 * request(_id_,passenger_id,model_year,model,passengers,taken)
					 * trip(_id_,driver_id,passenger_id,start,end,fee,rating);
					 */
					System.out.print("Processing...");
					//NOTE: use Statement for static string only.
					String driver = "CREATE TABLE Driver"
									+ "(did INTEGER,"
									+ "name VARCHAR(30),"
									+ "vid CHAR(6),"
									+ "CONSTRAINT DriverKey PRIMARY KEY (did))";
					String vehicle = "CREATE TABLE Vehicle"
									+ "(vid CHAR(6),"
									+ "model VARCHAR(30),"
									+ "myear INTEGER,"
									+ "seats INTEGER,"
									+ "CONSTRAINT VehicleKey PRIMARY KEY (vid),"
									+ "CONSTRAINT FourDigit CHECK (myear >= 2010 AND myear <= 2018))";
					String passenger = "CREATE TABLE Passenger"
									+ "(pid INTEGER,"
									+ "name VARCHAR(30),"
									+ "CONSTRAINT PassengerKey PRIMARY KEY (pid))";
					String request = "CREATE TABLE Request"
									+ "(rid INTEGER,"
									+ "pid INTEGER,"
									+ "myear INTEGER,"
									+ "model VARCHAR(30),"
									+ "passengers INTEGER,"
									+ "taken INTEGER,"
									+ "CONSTRAINT RequestKey PRIMARY KEY (rid),"
									+ "CONSTRAINT PassengerExistence FOREIGN KEY (pid) REFERENCES Passenger(pid) ON DELETE NO ACTION ON UPDATE NO ACTION)";
					String trip = "CREATE TABLE Trip"
									+ "(tid INTEGER,"
									+ "did INTEGER,"
									+ "pid INTEGER,"
									+ "start TIMESTAMP,"
									+ "end TIMESTAMP NULL DEFAULT NULL,"
									+ "fee INTEGER,"
									+ "rating INTEGER,"
									+ "CONSTRAINT TripKey PRIMARY KEY(tid))";
					try (Statement st = c.createStatement()) {
						for (String query: Arrays.asList(driver,vehicle,passenger,request,trip)) {
							st.executeUpdate(query);
						}
						System.out.println("Done! Tables are created!\n");
					} catch (SQLException ex) {
						System.out.println(ex.getMessage()+"\n");
					}
				} break;

				case 2: {
					String drop = "DROP TABLE IF EXISTS Driver, Vehicle, Passenger, Request, Trip";
					System.out.print("Processing...");
					try (Statement st = c.createStatement()) {
						st.executeUpdate(drop);
						System.out.println("Done! Tables are deleted!\n");
					} catch (SQLException ex) {
						System.out.println(ex.getMessage());
					}
				} break;

				case 3: {
					System.out.println("Please enter the folder path.");
					Scanner sc = new Scanner(System.in);
					String path = sc.nextLine();
					System.out.print("Processing...");
					File[] files = new File(path).listFiles(new FileFilter() {
						@Override
						public boolean accept(File pathname) {
							return pathname.getName().toUpperCase().endsWith("CSV");
						}
					});
					if (files == null) {
						System.out.println("\n[ERROR] An I/O Error occured. The path may not be a directory.");
					} else {
						for (File file: files) {
							List<List<String>> list = loadCSVAsList(file);
							switch (file.getName().toLowerCase()) {
								case "drivers.csv": {
									try (PreparedStatement prep = c.prepareStatement("INSERT INTO Driver VALUES (?, ?, ?)")) {
										c.setAutoCommit(false);
										for (List<String> tuple: list) {
											if (tuple.size() != 3) {
												System.out.println("\n[ERROR] Invalid tuple.");
											} else {
												try {
													//System.out.println(tuple);
													prep.setInt(1,Integer.parseInt(tuple.get(0)));
													prep.setString(2,tuple.get(1));
													prep.setString(3,tuple.get(2));
													prep.executeUpdate();
												} catch (Exception ex) {
													System.out.println("\n[ERROR] Invalid tuple.");
													System.out.println(ex.getMessage());
												}
											}
										}
										c.commit();
									} catch (SQLException ex) {
										System.out.println(ex.getMessage());
									}
								} break;

								case "vehicles.csv": {
									try (PreparedStatement prep = c.prepareStatement("INSERT INTO Vehicle VALUES (?, ?, ?, ?)")) {
										c.setAutoCommit(false);
										for (List<String> tuple: list) {
											if (tuple.size() != 4) {
												System.out.println("\n[ERROR] Invalid tuple.");
											} else {
												try {
													prep.setString(1,tuple.get(0));
													prep.setString(2,tuple.get(1));
													prep.setInt(3,Integer.parseInt(tuple.get(2)));
													prep.setInt(4,Integer.parseInt(tuple.get(3)));
													prep.executeUpdate();
												} catch (Exception ex) {
													System.out.println("\n[ERROR] Invalid tuple.");
												}
											}
										}
										c.commit();
									} catch (SQLException ex) {
										System.out.println(ex.getMessage());
									}
								} break;

								case "passengers.csv": {
									try (PreparedStatement prep = c.prepareStatement("INSERT INTO Passenger Values (?, ?)")) {
										c.setAutoCommit(false);
										for (List<String> tuple: list) {
											if (tuple.size() != 2) {
												System.out.println("\n[ERROR] Invalid tuple.");
											} else {
												try {
													prep.setInt(1,Integer.parseInt(tuple.get(0)));
													prep.setString(2,tuple.get(1));
													prep.executeUpdate();
												} catch (Exception ex) {
													System.out.println("\n[ERROR] Invalid tuple.");
												}
											}
										}
										c.commit();
									} catch (SQLException ex) {
										System.out.println(ex.getMessage());
									}

								} break;

								case "trips.csv": {
									try (PreparedStatement prep = c.prepareStatement("INSERT INTO Trip Values (?, ?, ?, ?, ?, ?, ?)")) {
										c.setAutoCommit(false);
										for (List<String> tuple: list) {
											if (tuple.size() != 7) {
												System.out.println("\n[ERROR] Invalid tuple.");
											} else {
												try {
													prep.setInt(1,Integer.parseInt(tuple.get(0)));
													prep.setInt(2,Integer.parseInt(tuple.get(1)));
													prep.setInt(3,Integer.parseInt(tuple.get(2)));
													prep.setTimestamp(4,Timestamp.valueOf(tuple.get(3)));
													prep.setTimestamp(5,Timestamp.valueOf(tuple.get(4)));
													prep.setInt(6,Integer.parseInt(tuple.get(5)));
													prep.setInt(7,Integer.parseInt(tuple.get(6)));
													prep.executeUpdate();
												} catch (Exception ex) {
													System.out.println("\n[ERROR] Invalid tuple.");
												}
											}
										}
										c.commit();
									} catch (SQLException ex) {
										System.out.println(ex.getMessage());
									}
								} break;

								default:
									System.out.println("[WARNING] Ignoring " + file.getName() + ".");
									break;
							}
						}
						System.out.println("Data is loaded!\n");
					}
				} break;

				case 4: {
					final List<String> tableNames = Arrays.asList("Driver","Vehicle","Passenger","Request","Trip");
					try (Statement st = c.createStatement()) {
						for (String name: tableNames) {
							String query = "SELECT COUNT(*) AS Number FROM " + name;
							ResultSet s = st.executeQuery(query);
							try {
								s.next();
								int count = s.getInt("Number");
								System.out.println(name + ": " + count);
							} catch (Exception ex) {
								System.out.println(name + ": " + ex.getMessage());
							}
						}
					} catch (SQLException ex) {
						System.out.println(ex.getMessage());
					}
					System.out.println();
				} break;

				case 5: {
					System.out.println();
				} return;
			}
		}
	}

	static List<List<String>> loadCSVAsList(File f) {
		try {
			Path p = f.toPath();
			List<String> lines = Files.readAllLines(p,Charset.forName("UTF-8"));
			List<List<String>> result = new ArrayList<>(lines.size());
			for (String line: lines) {
				result.add(new ArrayList<>(Arrays.asList(line.split(","))));
			}
			return result;
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return null;
		}
	}
}
