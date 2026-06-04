-- Expense Tracker Database Schema
CREATE DATABASE IF NOT EXISTS expense_tracker;
USE expense_tracker;

CREATE TABLE IF NOT EXISTS expenses (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(100)   NOT NULL,
    amount      DECIMAL(10,2)  NOT NULL,
    type        ENUM('income','expense') NOT NULL,
    category    VARCHAR(50)    NOT NULL,
    note        VARCHAR(255)   DEFAULT '',
    created_at  DATETIME       DEFAULT CURRENT_TIMESTAMP
);
