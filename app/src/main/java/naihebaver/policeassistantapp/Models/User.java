package naihebaver.policeassistantapp.Models;

public class User {

    String key;
    String email;
    String name;
    String phone;
    String password;
    String user_photo;
    String state;
    String offender_photo;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserPhoto() {
        return user_photo;
    }

    public void setUserPhoto(String user_photo) {
        this.user_photo = user_photo;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getOffender_photo() {
        return offender_photo;
    }

    public void setOffender_photo(String offender_photo) {
        this.offender_photo = offender_photo;
    }
}
