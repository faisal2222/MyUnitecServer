/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.ac.unitec.myunitec;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonObject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.json.Json;

/**
 * REST Web Service
 *
 * @author Faisal
 */
@Path("login")
public class Login {

    @Context
    private UriInfo context;
    private final Connection connection;
    private final PreparedStatement getUser;
    private final String dbUsersTable;
    private final String dbUsernameAtt;
    private final String dbFirstNameAtt;
    private final String dbLastNameAtt;
    private final String dbPasswordAtt;
    private final String dbSaltAtt;

    /**
     * Creates a new instance of Login
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     * @throws java.sql.SQLException
     */
    public Login() throws IOException, ClassNotFoundException, SQLException {
        Properties properties = new Properties();
        properties.loadFromXML(getClass().getResourceAsStream("DBConfig.xml"));
        String dbDriver = properties.get("dbDriver").toString();
        String dbUrl = properties.get("dbUrl").toString();
        String dbUserName = properties.get("dbusername").toString();
        String dbPassword = properties.get("dbpassword").toString();
        Class.forName(dbDriver);
        dbUsersTable = properties.get("dbUsersTable").toString();
        dbUsernameAtt = properties.get("dbUsernameAtt").toString();
        dbFirstNameAtt = properties.get("dbFirstNameAtt").toString();
        dbLastNameAtt = properties.get("dbLastNameAtt").toString();
        dbPasswordAtt = properties.get("dbPasswordAtt").toString();
        dbSaltAtt = properties.get("dbSaltAtt").toString();
        connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
        getUser = connection.prepareStatement("SELECT * FROM " + dbUsersTable + " WHERE " + dbUsernameAtt + " = ?");
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
        String password = content.getString("password");
        JsonObject jsonObject;
        boolean isFound;
        ResultSet resultSet;
        try {
            getUser.setString(1, username);
            resultSet = getUser.executeQuery();
            isFound = resultSet.first();
        } catch (SQLException ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
            jsonObject = Json.createObjectBuilder()
                    .add("result", "false").build();
            return jsonObject;
        }

        if (isFound) {
            try {
                String firstName = resultSet.getString(dbFirstNameAtt);
                String LastName = resultSet.getString(dbLastNameAtt);
                String userPassword = resultSet.getString(dbPasswordAtt);
                String userSalt = resultSet.getString(dbSaltAtt);
                Base64.Decoder decoder = Base64.getDecoder();
                byte[] salt = decoder.decode(userSalt);
                KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
                SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                byte[] hash = secretKeyFactory.generateSecret(keySpec).getEncoded();
                Base64.Encoder encoder = Base64.getEncoder();
                String hashedPassword = encoder.encodeToString(hash);
                if (hashedPassword.compareTo(userPassword) == 0) {
                    jsonObject = Json.createObjectBuilder()
                            .add("result", "true")
                            .add("firstName", firstName)
                            .add("lastName", LastName).build();
                } else {
                    jsonObject = Json.createObjectBuilder()
                            .add("result", "false").build();
                }
            } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
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

}
