package com.expense.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Expense {

    private int           id;
    private String        title;
    private BigDecimal    amount;
    private String        type;       // "income" | "expense"
    private String        category;
    private String        note;
    private LocalDateTime createdAt;

    public Expense() {}

    public Expense(String title, BigDecimal amount, String type, String category, String note) {
        this.title    = title;
        this.amount   = amount;
        this.type     = type;
        this.category = category;
        this.note     = note;
    }

    // Getters & Setters
    public int           getId()        { return id; }
    public void          setId(int id)  { this.id = id; }

    public String        getTitle()                  { return title; }
    public void          setTitle(String title)      { this.title = title; }

    public BigDecimal    getAmount()                      { return amount; }
    public void          setAmount(BigDecimal amount)     { this.amount = amount; }

    public String        getType()                   { return type; }
    public void          setType(String type)        { this.type = type; }

    public String        getCategory()                    { return category; }
    public void          setCategory(String category)     { this.category = category; }

    public String        getNote()                   { return note; }
    public void          setNote(String note)        { this.note = note; }

    public LocalDateTime getCreatedAt()                       { return createdAt; }
    public void          setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }
}
