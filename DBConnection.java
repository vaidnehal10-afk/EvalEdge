import java.sql.*;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/evaluation_db";
private static final String USER = "root";
private static final String PASS = "root";

    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASS);
    }
}