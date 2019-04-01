package naihebaver.policeassistantapp.Models;

public class Violation {

    String date;
    String time;
    String reg_number;
    String country_car;
    String address;
    double lng;
    double lat;
    int type;
    String text_type;
    String offender_photo;
    String from_user;
    String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getReg_number() {
        return reg_number;
    }

    public void setReg_number(String reg_number) {
        this.reg_number = reg_number;
    }

    public String getCountryCar() {
        return country_car;
    }

    public void setCountryCar(String country_car) {
        this.country_car = country_car;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLg() {
        return lng;
    }

    public void setLg(double lng) {
        this.lng = lng;
    }

    public double getLt() {
        return lat;
    }

    public void setLt(double lat) {
        this.lat = lat;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getText_type() {
        return text_type;
    }

    public void setText_type(String text_type) {
        this.text_type = text_type;
    }

    public String getOffender_photo() {
        return offender_photo;
    }

    public void setOffender_photo(String offender_photo) {
        this.offender_photo = offender_photo;
    }

    public String getFrom_user() {
        return from_user;
    }

    public void setFrom_user(String from_user) {
        this.from_user = from_user;
    }
}
