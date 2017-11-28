package net.coding.program.common.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 16/5/12.
 * 国家代码和电话前缀
 */
public class PhoneCountry implements Serializable {

    public String country = "";     //"China",
    public String country_code = "";//"86",
    public String iso_code = "";    //"cn"

    public PhoneCountry() {
    }

    public PhoneCountry(JSONObject json) {
        country = json.optString("country", "");
        country_code = json.optString("country_code", "");
        iso_code = json.optString("iso_code", "");
    }

    public static PhoneCountry getChina() {
        PhoneCountry phoneCountry = new PhoneCountry();
        phoneCountry.country = "China";
        phoneCountry.country_code = "86";
        phoneCountry.iso_code = "cn";
        return phoneCountry;
    }

    public String getFirstLetter() {
        String letter = country.substring(0, 1).toUpperCase();
        if (0 <= letter.compareTo("A") && letter.compareTo("Z") <= 0) {
            return letter;
        }

        return "#";
    }

    public String getCountryCode() {
        return "+" + country_code;
    }

}
