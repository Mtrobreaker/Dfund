package com.dfund.app.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "saving_goals")
public class SavingGoalEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    private double targetAmount;
    private double savedAmount;
    private String category; // "SAFETY_SHIELD", "GOLD", "SIP", "RD"
    private long targetDate;
    private long createdAt;

    public SavingGoalEntity() {
    }

    public SavingGoalEntity(String title, double targetAmount, double savedAmount, 
                            String category, long targetDate, long createdAt) {
        this.title = title;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.category = category;
        this.targetDate = targetDate;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }

    public double getSavedAmount() { return savedAmount; }
    public void setSavedAmount(double savedAmount) { this.savedAmount = savedAmount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getTargetDate() { return targetDate; }
    public void setTargetDate(long targetDate) { this.targetDate = targetDate; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
