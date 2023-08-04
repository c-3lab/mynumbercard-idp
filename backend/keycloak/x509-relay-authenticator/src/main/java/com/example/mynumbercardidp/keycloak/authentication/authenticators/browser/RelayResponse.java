package com.example.mynumbercardidp.keycloak.authentication.authenticators.browser;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.ws.rs.core.MultivaluedMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;

public class RelayResponse {
    public static final int VERIFY_RESULT_INTERNAL_ERROR = -2;
    public static final int VERIFY_RESULT_UNDEFINED = -1;
    public static final int VERIFY_RESULT_SUCCESS = 0;
    public static final int VERIFY_RESULT_INVAILD_FORMAT = 1;
    public static final int VERIFY_RESULT_EXPIRED = 2;
    public static final int VERIFY_RESULT_REVOKED = 3;

    @JsonProperty("uniqueId")
    private String uniqueId;

    @JsonProperty("verifyResultCode")
    private int verifyResultCode;

    @JsonProperty("name")
    private String name;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("address")
    private String address;

    @JsonProperty("birthDate")
    private LocalDate birthDate;

    public RelayResponse() {
        this.setUniqueId("");
        this.setVerifyResultCode(VERIFY_RESULT_UNDEFINED);
        this.setName("");
        this.setGender("");
        this.setAddress("");
        this.setBirthDate(LocalDate.now());
    }

    public RelayResponse(@JsonProperty("uniqueId") String uniqueId, @JsonProperty("verifyResultCode") int verifyResultCode) {
        this.setUniqueId(uniqueId);
        this.setVerifyResultCode(verifyResultCode);
    }

    public RelayResponse(
               @JsonProperty("uniqueId") String uniqueId,
               @JsonProperty("verifyResultCode") int verifyResultCode,
               @JsonProperty("name") String name,
               @JsonProperty("gender") String gender,
               @JsonProperty("address") String address,
               @JsonProperty("birthDate") LocalDate birthDate) {
        this.setUniqueId(uniqueId);
        this.setVerifyResultCode(verifyResultCode);
        this.setName(name);
        this.setGender(gender);
        this.setAddress(address);
        this.setBirthDate(birthDate);
    }

    @JsonCreator
    public RelayResponse(
               @JsonProperty("uniqueId") String uniqueId,
               @JsonProperty("verifyResultCode") int verifyResultCode,
               @JsonProperty("name") String name,
               @JsonProperty("gender") String gender,
               @JsonProperty("address") String address,
               @JsonProperty("birthDate") String birthDate) {
        this.setUniqueId(uniqueId);
        this.setVerifyResultCode(verifyResultCode);
        this.setName(name);
        this.setGender(gender);
        this.setAddress(address);
        this.setBirthDate(birthDate);
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getUniqueId() {
        return this.uniqueId;
    }

    public void setVerifyResultCode(int verifyResultCode) {
        this.verifyResultCode = verifyResultCode;
    }

    public int getVerifyResultCode() {
        return this.verifyResultCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGender() {
        return this.gender;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return this.address;
    }

    public void setBirthDate(String birthDate) {
        String _birthDate = birthDate.replace("/","-");
        this.birthDate = LocalDate.parse(_birthDate);
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public LocalDate getBirthDate() {
        return this.birthDate;
    }

    @Override
    public String toString() {
      return getClass().getName() +
          ", getUniqueId: " + getUniqueId() +
          ", getVerifyResultCode: " + getVerifyResultCode() +
	  ", getName: " + getName() +
	  ", getGender: " + getGender() +
	  ", getAddress: " + getAddress() +
	  ", getbirthDate " + getBirthDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public MultivaluedMap<String, String> toMultivaluedMap() {
        MultivaluedMap<String, String> map = new MultivaluedMapImpl();
        try {
            map.add("username", this.getUniqueId());
            map.add("uniqueId", this.getUniqueId());

            // Name
            String[] _name = this.getName().split("\\s|　+");
            map.add("firstName", _name[1]);
            map.add("givenName", _name[0]);

            // Gender
            map.add("gender", this.getGender());

            // Address
            Pattern addressPattern = Pattern.compile("^(.{2}[都道府県]|.{3}県)(.+?)$");
            Matcher addressMatcher = addressPattern.matcher(this.getAddress());

            if (addressMatcher.find()) {
               map.add("region", addressMatcher.group(1));
               map.add("locality", addressMatcher.group(2));
            }

            // Birth date
            map.add("birthdate",
                         this.getBirthDate().format(DateTimeFormatter.ISO_LOCAL_DATE));

        } catch (Exception e) {
            e.printStackTrace();
            map = null;
        }
        return map;
    }

    public static String getVerifyResult(int verifyResultCode) {
        switch (verifyResultCode) {
            case VERIFY_RESULT_INTERNAL_ERROR:
                return "Internal error";
            case VERIFY_RESULT_UNDEFINED:
                return "Undefined";
            case VERIFY_RESULT_SUCCESS:
                return "Success";
            case VERIFY_RESULT_INVAILD_FORMAT:
                return "Invaild format";
            case VERIFY_RESULT_EXPIRED:
                return "Expired";
            case VERIFY_RESULT_REVOKED:
                return "Revoked";
            default:
                 throw new IllegalStateException("Invalid verify result code: " + verifyResultCode);
        }
    }
}
