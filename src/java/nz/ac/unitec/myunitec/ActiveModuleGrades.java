/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.ac.unitec.myunitec;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * REST Web Service
 *
 * @author mmahmoud
 */
@Path("activemodulegrades")
public class ActiveModuleGrades {

    @Context
    private UriInfo context;
    private final Connection connection;
    private final PreparedStatement getActiveModuleGrades;
    private final String dbGradesTable;
    private final String dbAssesmentAtt;
    private final String dbUsernameAtt;
    private final String dbModuleIdAtt;
    private final String dbSemesterAtt;
    private final String dbYearAtt;
    private final String dbGradeAtt;

    /**
     * Creates a new instance of ActiveModuleGrades
     *
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     * @throws java.sql.SQLException
     */
    public ActiveModuleGrades() throws IOException, ClassNotFoundException, SQLException {
        Properties properties = new Properties();
        properties.loadFromXML(getClass().getResourceAsStream("DBConfig.xml"));
        String dbDriver = properties.get("dbDriver").toString();
        String dbUrl = properties.get("dbUrl").toString();
        String dbUserName = properties.get("dbusername").toString();
        String dbPassword = properties.get("dbpassword").toString();
        Class.forName(dbDriver);
        dbGradesTable = properties.get("dbGradesTable").toString();
        dbAssesmentAtt = properties.get("dbAssesmentAtt").toString();
        dbUsernameAtt = properties.get("dbUsernameAtt").toString();
        dbModuleIdAtt = properties.get("dbModuleIdAtt").toString();
        dbSemesterAtt = properties.get("dbSemesterAtt").toString();
        dbYearAtt = properties.get("dbYearAtt").toString();
        dbGradeAtt = properties.get("dbGradeAtt").toString();
        connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
        getActiveModuleGrades = connection.prepareStatement("SELECT * FROM "
                + dbGradesTable + " WHERE " + dbUsernameAtt + " = ? "
                + " AND " + dbModuleIdAtt + " = ?");
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
        String moduleID = content.getString("moduleID");
        JsonObject jsonObject;
        boolean isFound;
        ResultSet resultSet;

        try {
            getActiveModuleGrades.setString(1, username);
            getActiveModuleGrades.setString(2, moduleID);
            resultSet = getActiveModuleGrades.executeQuery();
            isFound = resultSet.first();
        } catch (SQLException ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
            jsonObject = Json.createObjectBuilder()
                    .add("result", "false").build();
            return jsonObject;
        }

        try {
            JsonArrayBuilder grades = Json.createArrayBuilder();
            if (isFound) {
                do {
                    ActiveModuleGrades.Grade grade = new ActiveModuleGrades.Grade();
                    grade.assesment = resultSet.getString(dbAssesmentAtt);
                    grade.username = resultSet.getString(dbUsernameAtt);
                    grade.moduleid = resultSet.getString(dbModuleIdAtt);
                    grade.semester = resultSet.getString(dbSemesterAtt);
                    grade.year = resultSet.getString(dbYearAtt);
                    grade.grade = resultSet.getString(dbGradeAtt);
                    grades.add(Json.createObjectBuilder().add(
                            "assesment", grade.assesment).add(
                                    "username", grade.username).add(
                                    "moduleid", grade.moduleid).add(
                                    "semester", grade.semester).add(
                                    "year", grade.year).add(
                                    "grade", grade.grade).build());
                } while (resultSet.next());
            }
            jsonObject = Json.createObjectBuilder().add("result", "true").add(
                    "grades", grades.build()).build();
        } catch (SQLException ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
            jsonObject = Json.createObjectBuilder()
                    .add("result", "false").build();
        }
        return jsonObject;
    }

    private class Grade {

        public String assesment;
        public String username;
        public String moduleid;
        public String semester;
        public String year;
        public String grade;

    }
}
