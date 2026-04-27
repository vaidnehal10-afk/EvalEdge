import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        TeacherService teacher = new TeacherService();
        StudentService student = new StudentService();
        BookingService booking = new BookingService();

        System.out.println("╔══════════════════════════════════╗");
        System.out.println("║         Welcome to EvalEdge      ║");
        System.out.println("╠══════════════════════════════════╣");
        System.out.println("║  1. Teacher                      ║");
        System.out.println("║  2. Student (console)            ║");
        System.out.println("║  3. Student Dashboard (web)      ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.print("Enter choice: ");

        int role = sc.nextInt();
        sc.nextLine();

        // ================= DASHBOARD (WEB) =================
        if (role == 3) {
            try {
                DashboardServer.start();
                // Keep the main thread alive so the server keeps running
                Thread.currentThread().join();
            } catch (Exception e) {
                System.out.println("Failed to start dashboard server: " + e.getMessage());
                e.printStackTrace();
            }
            return;
        }

        // ================= TEACHER =================
        if (role == 1) {
            System.out.println("1. New Teacher");
            System.out.println("2. Existing Teacher");
            int choice = sc.nextInt();
            sc.nextLine();

            int teacherId;
            if (choice == 1) {
                System.out.print("Enter Teacher Name: ");
                String name = sc.nextLine();
                teacherId = teacher.addTeacher(name);
                System.out.println("Your ID: " + teacherId);
            } else {
                System.out.print("Enter Teacher ID: ");
                teacherId = sc.nextInt();
                sc.nextLine();
            }

            while (true) {
                System.out.print("Enter Day: ");
                String day1 = sc.nextLine();
                System.out.print("Start Time (HH:MM): ");
                String start1 = sc.nextLine();
                System.out.print("End Time (HH:MM): ");
                String end1 = sc.nextLine();
                teacher.addSlots(teacherId, day1, start1, end1);
                System.out.print("Add more slots? (yes/no): ");
                String more = sc.nextLine();
                if (!more.equalsIgnoreCase("yes")) break;
            }
        }

        // ================= STUDENT (console) =================
        else if (role == 2) {
            System.out.println("1. New Student");
            System.out.println("2. Existing Student");
            int choice = sc.nextInt();
            sc.nextLine();

            int studentId;
            if (choice == 1) {
                System.out.print("Enter Student Name: ");
                String name = sc.nextLine();
                studentId = student.addStudent(name);
                System.out.println("Your ID: " + studentId);
            } else {
                System.out.print("Enter Student ID: ");
                studentId = sc.nextInt();
                sc.nextLine();
            }

            System.out.print("Enter Teacher ID: ");
            int teacherId = sc.nextInt();
            sc.nextLine();
            System.out.print("Enter Day: ");
            String day = sc.nextLine();

            boolean available = student.viewSlots(teacherId, day);
            if (available) {
                System.out.print("Enter Slot ID to book: ");
                int slotId = sc.nextInt();
                booking.bookSlot(studentId, slotId);
            } else {
                System.out.println("Cannot book — no slots available.");
            }
        }

        sc.close();
    }
}
