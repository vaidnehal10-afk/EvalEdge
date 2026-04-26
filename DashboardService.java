import java.sql.*;
import java.util.*;

/**
 * DashboardService.java
 * Provides all backend operations for the Student Dashboard.
 * Integrates with the existing evaluation_db and DBConnection class.
 */
public class DashboardService {

    // ─── Authentication ───────────────────────────────────────────────────────

    /**
     * Authenticates a student by ID and password.
     * Returns a map of student profile fields, or null if login fails.
     */
    public Map<String, String> loginStudent(int studentId, String password) {
        try (Connection con = DBConnection.getConnection()) {
            String query = "SELECT student_id, name, course, semester FROM students " +
                           "WHERE student_id = ? AND password = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, studentId);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Map<String, String> profile = new LinkedHashMap<>();
                profile.put("student_id", String.valueOf(rs.getInt("student_id")));
                profile.put("name",       rs.getString("name"));
                profile.put("course",     rs.getString("course"));
                profile.put("semester",   String.valueOf(rs.getInt("semester")));
                return profile;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ─── PBL Listing ─────────────────────────────────────────────────────────

    /**
     * Returns all PBLs a student is enrolled in.
     */
    public List<Map<String, String>> getStudentPBLs(int studentId) {
        List<Map<String, String>> pbls = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            String query =
                "SELECT p.pbl_id, p.title, p.description, sp.status, t.name AS teacher_name " +
                "FROM student_pbls sp " +
                "JOIN pbls p ON sp.pbl_id = p.pbl_id " +
                "LEFT JOIN teachers t ON p.teacher_id = t.teacher_id " +
                "WHERE sp.student_id = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, String> pbl = new LinkedHashMap<>();
                pbl.put("pbl_id",       String.valueOf(rs.getInt("pbl_id")));
                pbl.put("title",        rs.getString("title"));
                pbl.put("description",  rs.getString("description"));
                pbl.put("status",       rs.getString("status"));
                pbl.put("teacher_name", rs.getString("teacher_name"));
                pbls.add(pbl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pbls;
    }

    // ─── Phase Details ────────────────────────────────────────────────────────

    /**
     * Returns phase evaluation data (marks, remarks, feedback) for a student's PBL.
     * Returns a list of 3 maps (one per phase).
     */
    public List<Map<String, String>> getPhaseDetails(int studentId, int pblId) {
        List<Map<String, String>> phases = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            String query =
                "SELECT phase_no, marks, max_marks, remarks, feedback, evaluated_at " +
                "FROM pbl_phases WHERE student_id = ? AND pbl_id = ? ORDER BY phase_no";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, studentId);
            ps.setInt(2, pblId);
            ResultSet rs = ps.executeQuery();

            // Build a map keyed by phase_no for easy lookup
            Map<Integer, Map<String, String>> phaseMap = new LinkedHashMap<>();
            while (rs.next()) {
                Map<String, String> phase = new LinkedHashMap<>();
                phase.put("phase_no",     String.valueOf(rs.getInt("phase_no")));
                phase.put("marks",        String.valueOf(rs.getDouble("marks")));
                phase.put("max_marks",    String.valueOf(rs.getDouble("max_marks")));
                phase.put("remarks",      rs.getString("remarks") != null ? rs.getString("remarks") : "");
                phase.put("feedback",     rs.getString("feedback") != null ? rs.getString("feedback") : "");
                phase.put("evaluated_at", rs.getString("evaluated_at") != null ? rs.getString("evaluated_at") : "");
                phaseMap.put(rs.getInt("phase_no"), phase);
            }

            // Ensure all 3 phases are always returned (empty if not evaluated yet)
            for (int i = 1; i <= 3; i++) {
                if (phaseMap.containsKey(i)) {
                    phases.add(phaseMap.get(i));
                } else {
                    Map<String, String> empty = new LinkedHashMap<>();
                    empty.put("phase_no",     String.valueOf(i));
                    empty.put("marks",        "0");
                    empty.put("max_marks",    "100");
                    empty.put("remarks",      "Not evaluated yet");
                    empty.put("feedback",     "");
                    empty.put("evaluated_at", "");
                    phases.add(empty);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return phases;
    }

    // ─── Attendance (Meets) ───────────────────────────────────────────────────

    /**
     * Returns meet attendance records for a student's PBL, grouped by phase.
     * Returns a map: phase_no -> list of meets.
     */
    public Map<Integer, List<Map<String, String>>> getMeetAttendance(int studentId, int pblId) {
        Map<Integer, List<Map<String, String>>> result = new LinkedHashMap<>();
        for (int i = 1; i <= 3; i++) result.put(i, new ArrayList<>());

        try (Connection con = DBConnection.getConnection()) {
            String query =
                "SELECT phase_no, meet_no, attended, meet_date, notes " +
                "FROM pbl_meets WHERE student_id = ? AND pbl_id = ? ORDER BY phase_no, meet_no";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, studentId);
            ps.setInt(2, pblId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, String> meet = new LinkedHashMap<>();
                meet.put("meet_no",   String.valueOf(rs.getInt("meet_no")));
                meet.put("attended",  rs.getBoolean("attended") ? "true" : "false");
                meet.put("meet_date", rs.getString("meet_date") != null ? rs.getString("meet_date") : "TBD");
                meet.put("notes",     rs.getString("notes") != null ? rs.getString("notes") : "");
                result.get(rs.getInt("phase_no")).add(meet);
            }

            // Ensure 2 meet slots per phase even if not yet scheduled
            for (int p = 1; p <= 3; p++) {
                List<Map<String, String>> meets = result.get(p);
                for (int m = meets.size() + 1; m <= 2; m++) {
                    Map<String, String> placeholder = new LinkedHashMap<>();
                    placeholder.put("meet_no",   String.valueOf(m));
                    placeholder.put("attended",  "false");
                    placeholder.put("meet_date", "Not scheduled");
                    placeholder.put("notes",     "");
                    meets.add(placeholder);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // ─── Teacher Operations ───────────────────────────────────────────────────

    /**
     * Saves or updates phase evaluation from teacher side.
     */
    public void savePhaseEvaluation(int studentId, int pblId, int phaseNo,
                                    double marks, String remarks, String feedback) {
        try (Connection con = DBConnection.getConnection()) {
            String upsert =
                "INSERT INTO pbl_phases (student_id, pbl_id, phase_no, marks, remarks, feedback) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE marks=VALUES(marks), remarks=VALUES(remarks), feedback=VALUES(feedback)";
            PreparedStatement ps = con.prepareStatement(upsert);
            ps.setInt(1, studentId);
            ps.setInt(2, pblId);
            ps.setInt(3, phaseNo);
            ps.setDouble(4, marks);
            ps.setString(5, remarks);
            ps.setString(6, feedback);
            ps.executeUpdate();
            System.out.println("Phase " + phaseNo + " evaluation saved.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Marks a meet as attended or not.
     */
    public void markMeetAttendance(int studentId, int pblId, int phaseNo,
                                   int meetNo, boolean attended) {
        try (Connection con = DBConnection.getConnection()) {
            String query =
                "UPDATE pbl_meets SET attended = ? " +
                "WHERE student_id = ? AND pbl_id = ? AND phase_no = ? AND meet_no = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setBoolean(1, attended);
            ps.setInt(2, studentId);
            ps.setInt(3, pblId);
            ps.setInt(4, phaseNo);
            ps.setInt(5, meetNo);
            ps.executeUpdate();
            System.out.println("Attendance updated.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
