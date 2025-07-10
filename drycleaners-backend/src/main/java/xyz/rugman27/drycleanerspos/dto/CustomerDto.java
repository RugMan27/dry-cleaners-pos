    package xyz.rugman27.drycleanerspos.dto;

    import com.fasterxml.jackson.annotation.JsonIgnore;
    import lombok.AccessLevel;
    import lombok.Data;
    import lombok.Setter;
    import xyz.rugman27.drycleanerspos.model.CustomerModel;
    import xyz.rugman27.drycleanerspos.utilites.MergeUtils;
    import xyz.rugman27.drycleanerspos.utilites.PhoneNumberUtil;

    import java.util.List;

    @Data
    public class CustomerDto {
        @Setter(AccessLevel.NONE)
        private String id;
        private String firstName;
        private String lastName;
        private String phone;
        private String email;
        @Setter(AccessLevel.NONE)
        private final Long createdAt;
        private ExtraCustomerData extraData;
        @JsonIgnore
        private CustomerModel originalModel;

        public CustomerDto(String id, long createdAt) {
            this.id = id;
            this.createdAt = createdAt;
        }

        @Data
        public static class ExtraCustomerData {
            enum StarchLevel {
                NONE,
                LIGHT,
                MEDIUM,
                HEAVY
            }
            enum Weekday {
                SUNDAY,
                MONDAY,
                TUESDAY,
                WEDNESDAY,
                THURSDAY,
                FRIDAY,
                SATURDAY,
            }

            enum FoldingPreference{
                HANGER,
                FOLDED
            }

            enum CommunicationMethod {
                SMS,
                EMAIL,
                CALL,
            }

            // Scheduling
            private Weekday normalPickupDay;
            private Weekday normalDropOffDay;

            // Personalization
            private String title;
            private String notes;

            //Billing
            private double discountPercentage;

            //Preferences
            private StarchLevel starchLevel;
            private FoldingPreference foldingPreference;

            // Communication
            private List<CommunicationMethod> communicationMethods;
            private List<CommunicationMethod> reminders;
            private boolean emailReceipt;

            //Customer History
            private long accountCreated;
            private long lastVisit;
            private int visitCount;
        }
    }
