package xyz.rugman27.drycleanerspos.config;

import lombok.Data;

@Data
public class TaxConfig {
    private static double taxPercentage = 7.80;

    public static double getTaxPercentage() {
        return taxPercentage /100;
    }
}
