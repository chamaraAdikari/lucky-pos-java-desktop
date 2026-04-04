package com.turboboostteam.pos.model.customer;

public enum LoyaltyTier {
    BRONZE(0,    1.0),   // 1 point per dollar
    SILVER(500,  1.5),   // 1.5 points per dollar
    GOLD(2000,   2.0);   // 2 points per dollar

    private final int pointsRequired;
    private final double multiplier;

    LoyaltyTier(int pointsRequired, double multiplier) {
        this.pointsRequired = pointsRequired;
        this.multiplier     = multiplier;
    }

    public int getPointsRequired()  { return pointsRequired; }
    public double getMultiplier()   { return multiplier; }

    // Determine tier from points
    public static LoyaltyTier fromPoints(int points) {
        if (points >= GOLD.pointsRequired)   return GOLD;
        if (points >= SILVER.pointsRequired) return SILVER;
        return BRONZE;
    }
}