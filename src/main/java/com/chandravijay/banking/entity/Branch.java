package com.chandravijay.banking.entity;

/**
 * A small set of predefined branches, each with a realistic-format IFSC and MICR code,
 * so accounts look like they belong to a real bank rather than a single generic entry.
 *
 * IFSC format: 4-letter bank code + 0 + 6-digit branch code (matches the real RBI format).
 * MICR format: 9 digits (city code + bank code + branch code), matches the real format.
 */
public enum Branch {

    HYDERABAD_MAIN("Hyderabad Main Branch", "Hyderabad", "Telangana", "CVBK0001001", "500001001"),
    BENGALURU("Bengaluru Branch", "Bengaluru", "Karnataka", "CVBK0001002", "560001002"),
    MUMBAI_FORT("Mumbai Fort Branch", "Mumbai", "Maharashtra", "CVBK0001003", "400001003"),
    CHENNAI_TNAGAR("Chennai T. Nagar Branch", "Chennai", "Tamil Nadu", "CVBK0001004", "600001004"),
    DELHI_CP("Delhi Connaught Place Branch", "New Delhi", "Delhi", "CVBK0001005", "110001005");

    private final String branchName;
    private final String city;
    private final String state;
    private final String ifscCode;
    private final String micrCode;

    Branch(String branchName, String city, String state, String ifscCode, String micrCode) {
        this.branchName = branchName;
        this.city = city;
        this.state = state;
        this.ifscCode = ifscCode;
        this.micrCode = micrCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public String getMicrCode() {
        return micrCode;
    }
}
