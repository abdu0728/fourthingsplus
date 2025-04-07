package app.persistence;

import app.entities.User;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper {


        public static User login (String username, String password, ConnectionPool connectionPool) throws
        DatabaseException, SQLException {
            String sql = "select * from users where username=? and password=?";
            ConnectionPool connectionpool = connectionPool.getInstance("postgres", "postgres", "jdbc:postgresql://localhost:5432/%s?currentSchema=public", "fourthingsplus");
            try (
                    Connection connection = connectionpool.getConnection();

                    PreparedStatement ps = connection.prepareStatement(sql)

            ) {
                System.out.println("forbindelse oprettet");
                ps.setString(1, username);
                ps.setString(2, password);

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int id = rs.getInt("user_id");
                    String role = rs.getString("role");
                    return new User(id, username, password, role);
                } else {
                    System.out.println("Ingen bruger fundet med disse loginoplysninger.");
                    throw new DatabaseException("Fejl i login. Prøv igen");
                }
            } catch (SQLException e) {
                e.printStackTrace();  // Udskriv hele stacktracen
                System.out.println("SQL Fejl: " + e.getMessage());  // Udskriv den specifikke fejlmeddelelse
                throw new DatabaseException("DB fejl", e.getMessage());
            }
        }

        public static void createuser (String userName, String password, ConnectionPool connectionPool) throws
        DatabaseException
        {
            String sql = "insert into users (user_name, password) values (?,?)";

            try (
                    Connection connection = connectionPool.getConnection();
                    PreparedStatement ps = connection.prepareStatement(sql)
            ) {
                ps.setString(1, userName);
                ps.setString(2, password);

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected != 1) {
                    throw new DatabaseException("Fejl ved oprettelse af ny bruger");
                }
            } catch (SQLException e) {
                String msg = "Der er sket en fejl. Prøv igen";
                if (e.getMessage().startsWith("ERROR: duplicate key value ")) {
                    msg = "Brugernavnet findes allerede. Vælg et andet";
                }
                throw new DatabaseException(msg, e.getMessage());
            }
        }
    }
