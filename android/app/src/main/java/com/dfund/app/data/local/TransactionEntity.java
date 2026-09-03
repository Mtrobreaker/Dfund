package com.dfund.app.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class TransactionEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String bankName;
    private double amount;
    private String type; // "DEBIT" or "CREDIT"
    private String category; // "EMI", "GROCERY", "FOOD", "FUEL", "BILL", "SURPLUS", "SALARY", "OTHER"
    private String vpa;
    private String utr;
    private String rawMessage;
    private boolean isRecurring;
    private boolean isEmi;
    private long timestamp;

    public TransactionEntity() {
    }

    public TransactionEntity(String bankName, double amount, String type, String category, 
                             String vpa, String utr, String rawMessage, boolean isRecurring, 
                             boolean isEmi, long timestamp) {
        this.bankName = bankName;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.vpa = vpa;
        this.utr = utr;
        this.rawMessage = rawMessage;
        this.isRecurring = isRecurring;
        this.isEmi = isEmi;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getVpa() { return vpa; }
    public void setVpa(String vpa) { this.vpa = vpa; }

    public String getUtr() { return utr; }
    public void setUtr(String utr) { this.utr = utr; }

    public String getRawMessage() { return rawMessage; }
    public void setRawMessage(String rawMessage) { this.rawMessage = rawMessage; }

    public boolean isRecurring() { return isRecurring; }
    public void setRecurring(boolean recurring) { isRecurring = recurring; }

    public boolean isEmi() { return isEmi; }
    public void setEmi(boolean emi) { isEmi = emi; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
