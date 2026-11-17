package latihan.util;

public class Session {

    private static int userId;
    private static String username;
    private static String email;
    private static String role;

    private Session() {
    }

    public static void login(
            int id,
            String username,
            String email,
            String role
    ) {
        Session.userId = id;
        Session.username = username;
        Session.email = email;
        Session.role = role;
    }

    public static int getUserId() {
        return userId;
    }

    public static String getUsername() {
        return username;
    }

    public static String getEmail() {
        return email;
    }

    public static String getRole() {
        return role;
    }

    public static boolean isLoggedIn() {
        return userId > 0;
    }

    public static boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public static void logout() {
        userId = 0;
        username = null;
        email = null;
        role = null;
    }
}