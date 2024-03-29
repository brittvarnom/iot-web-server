package servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import data.LockData;
import com.google.gson.Gson;

import java.sql.*;

// A very basic Sensor Server.
// Simply stores the last sensor name and value in memory.
//No data is permanently stored.

@WebServlet("/DoorLookup")

public class DoorLookup extends HttpServlet {

	// Collects or returns data for sensorname, sensorvalue parameters
	private static final long serialVersionUID = 1L;

	Connection connection = null;
	Statement statement;
	Gson gson = new Gson();

	public DoorLookup() {
		super();
	}

	public void init(ServletConfig config) throws ServletException {
		System.out.println("Sensor server is up and running\n");
		System.out.println(
				"Upload sensor data with http://localhost:8080/PhidgetServer2019/SensorServer?sensorname=xxx&sensorvalue=nnn");
		System.out.println(
				"View last sensor reading at  http://localhost:8080/PhidgetServer2019/SensorServer?getdata=true \n\n");
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setStatus(HttpServletResponse.SC_OK);

		LockData rfidData = new LockData(null, null);
		String jsonStringData = request.getParameter("stringJson");

		rfidData = gson.fromJson(jsonStringData, LockData.class);
		String resultsJson = getRoomForDoor(rfidData);
		PrintWriter out = response.getWriter();
		out.println(resultsJson);
		out.close();
	}

	private void getConnection() {
		// This will load the driver and establish a connection
		String user = "varnomb";
		String password = "Treg7booq";
		String url = "jdbc:mysql://mudfoot.doc.stu.mmu.ac.uk:6306/" + user;

		// Load the database driver
		try {
			Class.forName("com.mysql.jdbc.Driver").getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			System.out.println(e);
		}
		// get a connection with the user/pass
		try {
			connection = DriverManager.getConnection(url, user, password);
			// System.out.println("DEBUG: Connection to database successful.");
			statement = connection.createStatement();
		} catch (SQLException se) {
			System.out.println(se);
			System.out.println("\nDid you alter the lines to set user/password in the sensor server code?");
		}
	}

	private void closeConnection() {
		// get a connection with the user/pass
		try {
			connection.close();
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	String getRoomForDoor(LockData lockData) {
		System.out.println("getRoomForDoor reached");
		String selectSQL = "SELECT * FROM doorlookup where doorid='" + lockData.getDoorid() + "' ORDER BY roomid asc;";
		System.out.println(selectSQL);

		ResultSet results;
		LockData roomData = new LockData(null, null);
		String doorid = lockData.getRoomid();

		getConnection();
		try {
			results = statement.executeQuery(selectSQL);

			if (results.next() == true) {
				System.out.println("Success - valid result set");
				do {
					System.out.println("arrived");
					roomData.setDoorid(doorid);
					roomData.setRoomid(results.getString("roomid"));
				} while (results.next());
				System.out.println("Out of while loop");
			} else {
				System.out.println("Error - result set invalid, must not be empty");
				roomData.setDoorid(doorid);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnection();
		System.out.println(roomData);
		System.out.println(gson.toJson(roomData));
		return gson.toJson(roomData);
	}
}
