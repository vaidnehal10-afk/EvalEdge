import java.sql.*;

public class BookingService {

    public void bookSlot(int studentId, int slotId) {
        try (Connection con = DBConnection.getConnection()) {

            System.out.println(con); // DEBUG

            con.setAutoCommit(false);

            String check = "SELECT is_booked FROM slots WHERE slot_id=?";
            PreparedStatement ps1 = con.prepareStatement(check);
            ps1.setInt(1, slotId);

            ResultSet rs = ps1.executeQuery();

            if (!rs.next()) {
    System.out.println("Invalid Slot ID!");
    return;
}

if (!rs.getBoolean("is_booked"))  {

                String book = "INSERT INTO bookings (student_id, slot_id) VALUES (?, ?)";
                PreparedStatement ps2 = con.prepareStatement(book);
                ps2.setInt(1, studentId);
                ps2.setInt(2, slotId);
                ps2.executeUpdate();

                String update = "UPDATE slots SET is_booked=TRUE WHERE slot_id=?";
                PreparedStatement ps3 = con.prepareStatement(update);
                ps3.setInt(1, slotId);
                ps3.executeUpdate();

                con.commit();
                System.out.println("Booked!");

            } else {
                System.out.println("Slot not available!");
                con.rollback();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}