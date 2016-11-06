/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.ac.unitec.myunitec;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;

/**
 * REST Web Service
 *
 * @author mmahmoud
 */
@Path("timetable")
public class Timetable {

    @Context
    private UriInfo context;
    private final Connection connection;
    private final PreparedStatement getStudentEnrolledModules;
    private final PreparedStatement getModuleName;
    private final PreparedStatement getTimetableEntry;
    private final String dbTimetableEntryTable;
    private final String dbModuleEnrollmentTable;
    private final String dbModuleTable;
    
    private final String dbUsernameAtt;
    private final String dbModuleIdAtt;
    private final String dbSemesterAtt;
    private final String dbYearAtt;
    private final String dbStatusAtt;
    private final String dbModuleNameAtt;
    private final String dbWeekdayAtt;
    private final String dbRoomAtt;
    private final String dbTimePeriodAtt;

    /**
     * Creates a new instance of Timetable
     *
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     * @throws java.sql.SQLException
     */
    public Timetable() throws IOException, ClassNotFoundException, SQLException {
        Properties properties = new Properties();
        properties.loadFromXML(getClass().getResourceAsStream("DBConfig.xml"));
        String dbDriver = properties.get("dbDriver").toString();
        String dbUrl = properties.get("dbUrl").toString();
        String dbUserName = properties.get("dbusername").toString();
        String dbPassword = properties.get("dbpassword").toString();
        Class.forName(dbDriver);
        dbModuleEnrollmentTable = properties.get("dbModuleEnrollmentTable").toString();
        dbTimetableEntryTable = properties.get("dbTimetableEntryTable").toString();
        dbModuleTable = properties.get("dbModuleTable").toString();
        
        dbUsernameAtt = properties.get("dbUsernameAtt").toString();
        dbModuleIdAtt = properties.get("dbModuleIdAtt").toString();
        dbSemesterAtt = properties.get("dbSemesterAtt").toString();
        dbYearAtt = properties.get("dbYearAtt").toString();
        dbStatusAtt = properties.get("dbStatusAtt").toString();
        dbModuleNameAtt = properties.get("dbModuleNameAtt").toString();
        dbWeekdayAtt = properties.get("dbWeekdayAtt").toString();
        dbRoomAtt = properties.get("dbRoomAtt").toString();
        dbTimePeriodAtt = properties.get("dbTimePeriodAtt").toString();
        
        
        connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
        getStudentEnrolledModules = connection.prepareStatement("SELECT * FROM " +
                dbModuleEnrollmentTable + " WHERE " + dbUsernameAtt + " = ? " +
                " AND " + dbStatusAtt + " = 'enrolled'");
        getModuleName = connection.prepareStatement("SELECT * FROM " + 
                dbModuleTable + " WHERE " + dbModuleIdAtt + " = ? ");
        getTimetableEntry = connection.prepareStatement("SELECT * FROM " +
                dbTimetableEntryTable + " WHERE " + dbModuleIdAtt + " = ?" +
                " AND " + dbSemesterAtt + " = ? AND " + dbYearAtt + " = ?");
    }

    /**
     *
     * @param content
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject postHandler(JsonObject content) {
        String username = content.getString("username");
        JsonObject jsonObject;
        boolean isFound;
        ResultSet resultSet;

        try {
            getStudentEnrolledModules.setString(1, username);
            resultSet = getStudentEnrolledModules.executeQuery();
            isFound = resultSet.first();
        } catch (SQLException ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
            jsonObject = Json.createObjectBuilder()
                    .add("result", "false").build();
            return jsonObject;
        }

        try {
            JsonArrayBuilder timetable = Json.createArrayBuilder();
            if (isFound) {
                do {
                    int moduleID = resultSet.getInt(dbModuleIdAtt);
                    int semester = resultSet.getInt(dbSemesterAtt);
                    int year = resultSet.getInt(dbYearAtt);
                    getModuleName.setInt(1, moduleID);
                    ResultSet resultSet1 = getModuleName.executeQuery();
                    resultSet1.first();
                    String moduleName = resultSet1.getString(dbModuleNameAtt);
                    getTimetableEntry.setInt(1, moduleID);
                    getTimetableEntry.setInt(2, semester);
                    getTimetableEntry.setInt(3, year);
                    ResultSet resultSet2 = getTimetableEntry.executeQuery();
                    if (resultSet2.first()) {
                        String weekday = resultSet2.getString(dbWeekdayAtt);
                        String room = resultSet2.getString(dbRoomAtt);
                        String timePeriod = resultSet2.getString(dbTimePeriodAtt);
                        timetable.add(Json.createObjectBuilder().add(
                                "moduleID", moduleID).add(
                                        "moduleName", moduleName).add(
                                        "semester", semester).add(
                                        "room", room).add(
                                        "weekday", weekday).add(
                                        "year", year).add(
                                        "timePeriod", timePeriod).build());
                    }

                } while (resultSet.next());
            }
            jsonObject = Json.createObjectBuilder().add("result", "true").add(
                    "timetable", timetable.build()).build();
        } catch (SQLException ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
            jsonObject = Json.createObjectBuilder()
                    .add("result", "false").build();
        }
        return jsonObject;
    }
}
