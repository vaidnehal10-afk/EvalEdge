import java.sql.*;


public class StudentService {

    /*public void viewSlots(int teacherId, String day) {
        try (Connection con = DBConnection.getConnection()) {

            String query = "SELECT * FROM slots WHERE teacher_id=? AND day=? AND is_booked=FALSE";
            PreparedStatement ps = con.prepareStatement(query);

            ps.setInt(1, teacherId);
            ps.setString(2, day);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                System.out.println(
                    "Slot ID: " + rs.getInt("slot_id") +
                    " | " + rs.getTime("start_time") +
                    " - " + rs.getTime("end_time")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    public int addStudent(String name) {
    int id = -1;
    try (Connection con = DBConnection.getConnection()) {

        String query = "INSERT INTO students (name) VALUES (?)";
        PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, name);
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            id = rs.getInt(1);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
    return id;
}
public boolean viewSlots(int teacherId, String day) {
    boolean found = false;

    try (Connection con = DBConnection.getConnection()) {

        String query = "SELECT * FROM slots WHERE teacher_id=? AND day=? AND is_booked=FALSE";
        PreparedStatement ps = con.prepareStatement(query);

        ps.setInt(1, teacherId);
        ps.setString(2, day);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            found = true;
            System.out.println(
                "Slot ID: " + rs.getInt("slot_id") +
                " | " + rs.getTime("start_time") +
                " - " + rs.getTime("end_time")
            );
        }

        if (!found) {
            System.out.println("No slots available!");
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return found;
}

}