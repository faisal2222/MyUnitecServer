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
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * REST Web Service
 *
 * @author Faisal
 */
@Path("activemodules")
public class Activemodules {

    @Context
    private UriInfo context;
    private final Connection connection;
    private final PreparedStatement getStudentActiveModules;
    private final PreparedStatement getProgrammeModules;
    private final PreparedStatement getModuleName;
    private final String dbModuleEnrollmentTable;
    private final String dbProgrammeModuleTable;
    private final String dbModuleTable;
    private final String dbProgrammeIdAtt;
    private final String dbModuleIdAtt;
    private final String dbModuleNameAtt;
    private final String dbSemesterAtt;
    private final String dbYearAtt;
    private final String dbGradeAtt;
    private final String dbUsernameAtt;
    private final String dbStatusAtt;

    /**
     * Creates a new instance of Activemodules
     *
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     * @throws java.sql.SQLException
     */
    public Activemodules() throws IOException, ClassNotFoundException, SQLException {
        Properties properties = new Properties();
        properties.loadFromXML(getClass().getResourceAsStream("DBConfig.xml"));
        String dbDriver = properties.get("dbDriver").toString();
        String dbUrl = properties.get("dbUrl").toString();
        String dbUserName = properties.get("dbusername").toString();
        String dbPassword = properties.get("dbpassword").toString();
        Class.forName(dbDriver);
        dbModuleEnrollmentTable = properties.get("dbModuleEnrollmentTable").toString();
        dbProgrammeModuleTable = properties.get("dbProgrammeModuleTable").toString();
        dbModuleTable = properties.get("dbModuleTable").toString();
        dbProgrammeIdAtt = properties.get("dbProgrammeIdAtt").toString();
        dbModuleIdAtt = properties.get("dbModuleIdAtt").toString();
        dbModuleNameAtt = properties.get("dbModuleNameAtt").toString();
        dbSemesterAtt = properties.get("dbSemesterAtt").toString();
        dbYearAtt = properties.get("dbYearAtt").toString();
        dbGradeAtt = properties.get("dbGradeAtt").toString();
        dbUsernameAtt = properties.get("dbUsernameAtt").toString();
        dbStatusAtt = properties.get("dbStatusAtt").toString();
        connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
        getStudentActiveModules = connection.prepareStatement("SELECT * FROM "
                + dbModuleEnrollmentTable + " WHERE " + dbUsernameAtt + " = ? "
                + " AND " + dbStatusAtt + " = 'enrolled'");
        getProgrammeModules = connection.prepareStatement("SELECT * FROM "
                + dbProgrammeModuleTable + " WHERE " + dbProgrammeIdAtt + " = ? ");
        getModuleName = connection.prepareStatement("SELECT * FROM "
                + dbModuleTable + " WHERE " + dbModuleIdAtt + " = ?");
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
        String programmeID = content.getString("programmeID");
        JsonObject jsonObject;
        boolean isFound;
        ResultSet resultSet;
        ResultSet resultSet1;
        try {
            getStudentActiveModules.setString(1, username);
            resultSet = getStudentActiveModules.executeQuery();
            getProgrammeModules.setString(1, programmeID);
            resultSet1 = getProgrammeModules.executeQuery();
            isFound = resultSet.first() && resultSet1.first();
        } catch (SQLException ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
            jsonObject = Json.createObjectBuilder()
                    .add("result", "false").build();
            return jsonObject;
        }

        if (isFound) {
            try {
                Set<String> moduleIDs = new HashSet<>();
                do {
                    moduleIDs.add(resultSet1.getString(dbModuleIdAtt));
                } while (resultSet1.next());
                JsonArrayBuilder modules = Json.createArrayBuilder();
                do {
                    Module module = new Module();
                    module.id = resultSet.getString(dbModuleIdAtt);
                    if (moduleIDs.contains(module.id)) {
                        module.year = resultSet.getString(dbYearAtt);
                        module.grade = resultSet.getString(dbGradeAtt);
                        module.semester = resultSet.getString(dbSemesterAtt);
                        module.status = resultSet.getString(dbStatusAtt);
                        getModuleName.setString(1, module.id);
                        ResultSet moduleName = getModuleName.executeQuery();
                        if (moduleName.first()) {
                            module.name = moduleName.getString(dbModuleNameAtt);
                        } else {
                            jsonObject = Json.createObjectBuilder()
                                    .add("result", "false").build();
                            return jsonObject;
                        }
                        modules.add(Json.createObjectBuilder().add(
                                "id", module.id).add(
                                        "moduleName", module.name).add(
                                        "semester", module.semester).add(
                                        "year", module.year).add(
                                        "grade", module.grade).add(
                                        "status", module.status).build());
                    }
                } while (resultSet.next());
                jsonObject = Json.createObjectBuilder().add("result", "true").add(
                        "modules", modules.build()).build();
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

    private class Module {

        public String id;
        public String name;
        public String semester;
        public String year;
        public String grade;
        public String status;

    }
}
