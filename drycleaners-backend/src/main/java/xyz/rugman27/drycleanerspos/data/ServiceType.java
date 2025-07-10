package xyz.rugman27.drycleanerspos.data;

public enum ServiceType {
    LAUNDRY('L'),
    DRY_CLEANING('D'),
    PRESS_ONLY('P'),
    ALTERATION('A'),
    LEATHER_WORK('W'),
    WEDDING('E'),
    TUXEDO('T');

    private final char code;

    ServiceType(char code) {
        this.code = code;
    }

    public char getCode() {
        return code;
    }

    // Optional: Get enum from char code
    public static ServiceType fromCode(Character code) {
        for (ServiceType type : ServiceType.values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
    public static ServiceType safeParse(String value) {
        if(value.matches(".*(?i)[ldpa].*")){
            return fromCode(value.charAt(0));
        }
        try {
            return ServiceType.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null; // or throw custom exception
        }
    }
}