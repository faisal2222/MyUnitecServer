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
import java.util.HashSet;
import java.util.Objects;
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
@Path("enrolleric")
public class Enroll {

    @Context
    private UriInfo context;
    private final Connection connection;
    private final PreparedStatement enroll;
    private final String dbModuleEnrollmentTable;
    private final String dbUsernameAtt;
    private final String dbModuleIdAtt;
    private final String dbStatusAtt;
    private final String dbSemesterAtt;
    private final String dbYearAtt;

    /**
     * Creates a new instance of ActiveModuleGrades
     *
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     * @throws java.sql.SQLException
     */
    public Enroll() throws IOException, ClassNotFoundException, SQLException {
        Properties properties = new Properties();
        properties.loadFromXML(getClass().getResourceAsStream("DBConfig.xml"));
        String dbDriver = properties.get("dbDriver").toString();
        String dbUrl = properties.get("dbUrl").toString();
        String dbUserName = properties.get("dbusername").toString();
        String dbPassword = properties.get("dbpassword").toString();
        Class.forName(dbDriver);
        dbModuleEnrollmentTable = properties.get("dbModuleEnrollmentTable").toString();
        dbUsernameAtt = properties.get("dbUsernameAtt").toString();
        dbModuleIdAtt = properties.get("dbModuleIdAtt").toString();
        dbStatusAtt = properties.get("dbStatusAtt").toString();
        dbSemesterAtt = properties.get("dbSemesterAtt").toString();
        dbYearAtt = properties.get("dbYearAtt").toString();
        connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
        enroll = connection.prepareStatement("INSERT INTO " +
                dbModuleEnrollmentTable + "(" + dbUsernameAtt + ", " +
                dbModuleIdAtt + ", " + dbSemesterAtt + ", " + dbYearAtt + ", " +
                dbStatusAtt + ") VALUES (?,?,?,?,'pending')");

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
        String id = content.getString("id");
        String semester = content.getString("semester");
        String year = content.getString("year");
        JsonObject jsonObject;
        int result;
        try {
            enroll.setString(1, username);
            enroll.setInt(2, Integer.parseInt(id));
            enroll.setInt(3, Integer.parseInt(semester));
            enroll.setInt(4, Integer.parseInt(year));
            result = enroll.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
            jsonObject = Json.createObjectBuilder()
                    .add("result", "false").build();
            return jsonObject;
        }
        if (result > 0) {
            jsonObject = Json.createObjectBuilder()
                    .add("result", "true").build();
            return jsonObject;
        } else {
            jsonObject = Json.createObjectBuilder()
                    .add("result", "false").build();
            return jsonObject;
        }
    }

    
}
