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
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * REST Web Service
 *
 * @author Faisal
 */
@Path("activeenrollment")
public class Activeenrollment {

    @Context
    private UriInfo context;
    private final Connection connection;
    private final PreparedStatement getStudentActiveEnrollment;
    private final PreparedStatement getProgrammeName;
    private final String dbProgrammeEnrollmentTable;
    private final String dbProgrammeTable;
    private final String dbProgrammeIdAtt;
    private final String dbProgrammeNameAtt;
    private final String dbUsernameAtt;
    private final String dbStartDateAtt;
    private final String dbStatusAtt;

    /**
     * Creates a new instance of Activeenrollment
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     * @throws java.sql.SQLException
     */
    public Activeenrollment() throws IOException, ClassNotFoundException, SQLException {
        Properties properties = new Properties();
        properties.loadFromXML(getClass().getResourceAsStream("DBConfig.xml"));
        String dbDriver = properties.get("dbDriver").toString();
        String dbUrl = properties.get("dbUrl").toString();
        String dbUserName = properties.get("dbusername").toString();
        String dbPassword = properties.get("dbpassword").toString();
        Class.forName(dbDriver);
        dbProgrammeEnrollmentTable = properties.get("dbProgrammeEnrollmentTable").toString();
        dbProgrammeTable = properties.get("dbProgrammeTable").toString();
        dbProgrammeIdAtt = properties.get("dbProgrammeIdAtt").toString();
        dbProgrammeNameAtt = properties.get("dbProgrammeNameAtt").toString();
        dbUsernameAtt = properties.get("dbUsernameAtt").toString();
        dbStartDateAtt = properties.get("dbStartDateAtt").toString();
        dbStatusAtt = properties.get("dbStatusAtt").toString();
        connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
        getStudentActiveEnrollment = connection.prepareStatement("SELECT * FROM " +
                dbProgrammeEnrollmentTable + " WHERE " + dbUsernameAtt + " = ? " +
                " AND " + dbStatusAtt + " = 'enrolled'");
        getProgrammeName = connection.prepareStatement("SELECT * FROM " +
                dbProgrammeTable + " WHERE " + dbProgrammeIdAtt + " = ?");
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
            getStudentActiveEnrollment.setString(1, username);
            resultSet = getStudentActiveEnrollment.executeQuery();
            isFound = resultSet.first();
        } catch (SQLException ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
            jsonObject = Json.createObjectBuilder()
                    .add("result", "false").build();
            return jsonObject;
        }
        
        if (isFound) {
            try {
                JsonArrayBuilder programmes = Json.createArrayBuilder();
                do {
                    Programme programme = new Programme();
                    programme.id = resultSet.getString(dbProgrammeIdAtt);
                    programme.enrollmentDate = resultSet.getString(dbStartDateAtt);
                    getProgrammeName.setString(1, programme.id );
                    ResultSet resultSet1 = getProgrammeName.executeQuery();
                    if (resultSet1.first()) {
                        programme.name = resultSet1.getString(dbProgrammeNameAtt);
                        programmes.add(Json.createObjectBuilder().add(
                            "id", programme.id).add(
                            "programmeName", programme.name).add(
                            "startDate", programme.enrollmentDate).build());
                    } else {
                        jsonObject = Json.createObjectBuilder()
                                .add("result", "false").build();
                        return jsonObject;
                    }            
                } while (resultSet.next());
                jsonObject = Json.createObjectBuilder().add("result", "true").add(
                        "enrollments", programmes.build()).build();
            } catch (SQLException ex) {
                Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
                jsonObject = Json.createObjectBuilder()
                        .add("result", "false").build();
            }
        } else {
            jsonObject = Json.createObjectBuilder()
                    .add("result", "false").build();
        }
        return jsonObject;
    }

    private class Programme {

        public String id;
        public String name;
        public String enrollmentDate;

    }
}
