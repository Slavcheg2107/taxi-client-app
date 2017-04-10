package jdroidcoder.ua.taxi_bishkek.model;

/**
 * Created by jdroidcoder on 07.04.17.
 */
public class UserProfileDto {
    private String firstName;
    private String lastName;
    private String phone;
    private String email;

    public UserProfileDto() {
    }

    public UserProfileDto(String firstName, String lastName, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public static class User {
        private static String firstName;
        private static String lastName;
        private static String phone;
        private static String email;

        public static String getFirstName() {
            return firstName;
        }

        public static void setFirstName(String firstName) {
            User.firstName = firstName;
        }

        public static String getLastName() {
            return lastName;
        }

        public static void setLastName(String lastName) {
            User.lastName = lastName;
        }

        public static String getPhone() {
            return phone;
        }

        public static void setPhone(String phone) {
            User.phone = phone;
        }

        public static String getEmail() {
            return email;
        }

        public static void setEmail(String email) {
            User.email = email;
        }
    }
}
