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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * REST Web Service
 *
 * @author mmahmoud
 */
@Path("moduleenrollment")
public class ModuleEnrollment {

    @Context
    private UriInfo context;
    private final Connection connection;
    private final PreparedStatement getStudentActiveEnrollment;
    private final PreparedStatement getProgrammeModules;
    private final PreparedStatement getModuleName;
    private final String dbProgrammeEnrollmentTable;
    private final String dbProgrammeModuleTable;
    private final String dbModuleTable;
    private final String dbUsernameAtt;
    private final String dbProgrammeIdAtt;
    private final String dbStatusAtt;
    private final String dbModuleIdAtt;
    private final String dbModuleNameAtt;

    /**
     * Creates a new instance of ActiveModuleGrades
     *
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     * @throws java.sql.SQLException
     */
    public ModuleEnrollment() throws IOException, ClassNotFoundException, SQLException {
        Properties properties = new Properties();
        properties.loadFromXML(getClass().getResourceAsStream("DBConfig.xml"));
        String dbDriver = properties.get("dbDriver").toString();
        String dbUrl = properties.get("dbUrl").toString();
        String dbUserName = properties.get("dbusername").toString();
        String dbPassword = properties.get("dbpassword").toString();
        Class.forName(dbDriver);
        dbProgrammeEnrollmentTable = properties.get("dbProgrammeEnrollmentTable").toString();
        dbProgrammeModuleTable = properties.get("dbProgrammeModuleTable").toString();
        dbModuleTable = properties.get("dbModuleTable").toString();
        dbUsernameAtt = properties.get("dbUsernameAtt").toString();
        dbProgrammeIdAtt = properties.get("dbProgrammeIdAtt").toString();
        dbStatusAtt = properties.get("dbStatusAtt").toString();
        dbModuleIdAtt = properties.get("dbModuleIdAtt").toString();
        dbModuleNameAtt = properties.get("dbModuleNameAtt").toString();
        connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
        getStudentActiveEnrollment = connection.prepareStatement("SELECT * FROM " +
                dbProgrammeEnrollmentTable + " WHERE " + dbUsernameAtt + " = ? " +
                " AND " + dbStatusAtt + " = 'enrolled'");
        getProgrammeModules = connection.prepareStatement("SELECT * FROM " +
                dbProgrammeModuleTable + " WHERE " + dbProgrammeIdAtt + " = ? ");
        getModuleName = connection.prepareStatement("SELECT * FROM " + 
                dbModuleTable + " WHERE " + dbModuleIdAtt + " = ? ");
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

        try {
            JsonArrayBuilder enrollmentModules = Json.createArrayBuilder();
            Set<Module> modules = new HashSet<>();
            if (isFound) {
                do {
                    getProgrammeModules.setString(1, resultSet.getString(dbProgrammeIdAtt));
                    ResultSet resultSet1 = getProgrammeModules.executeQuery();
                    if (resultSet1.first()) {
                        do {
                            Module module = new Module();
                            module.id = resultSet1.getString(dbModuleIdAtt);
                            getModuleName.setString(1, module.id);
                            ResultSet resultSet2 = getModuleName.executeQuery();
                            resultSet2.first();
                            module.name = resultSet2.getString(dbModuleNameAtt);
                            modules.add(module);
                        } while (resultSet1.next());
                    }
                } while (resultSet.next());
                for (Module module : modules) {
                    enrollmentModules.add(Json.createObjectBuilder().add(
                            "id", module.id).add(
                                    "name", module.name).build());
                }
                
            }
            jsonObject = Json.createObjectBuilder().add("result", "true").add(
                    "enrollmentModules", enrollmentModules.build()).build();
        } catch (SQLException ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
            jsonObject = Json.createObjectBuilder()
                    .add("result", "false").build();
        }
        return jsonObject;
    }

    private class Module {

        public String name;
        public String id;

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 41 * hash + Objects.hashCode(this.id);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Module other = (Module) obj;
            return Objects.equals(this.id, other.id);
        }
        


    }
}
