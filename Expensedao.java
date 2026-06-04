package com.expense.dao;

import com.expense.model.Expense;
import com.expense.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDAO {

    /* ── INSERT ────────────────────────────────────────────────── */
    public boolean addExpense(Expense e) throws SQLException {
        String sql = "INSERT INTO expenses (title, amount, type, category, note) VALUES (?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getTitle());
            ps.setBigDecimal(2, e.getAmount());
            ps.setString(3, e.getType());
            ps.setString(4, e.getCategory());
            ps.setString(5, e.getNote());
            return ps.executeUpdate() > 0;
        }
    }

    /* ── SELECT ALL ─────────────────────────────────────────────── */
    public List<Expense> getAllExpenses() throws SQLException {
        String sql = "SELECT * FROM expenses ORDER BY created_at DESC";
        List<Expense> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    /* ── DELETE ─────────────────────────────────────────────────── */
    public boolean deleteExpense(int id) throws SQLException {
        String sql = "DELETE FROM expenses WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /* ── SUMMARY (balance / income / expense totals) ─────────────── */
    public BigDecimal[] getSummary() throws SQLException {
        String sql = "SELECT " +
                     "  SUM(CASE WHEN type='income'  THEN amount ELSE 0 END) AS total_income, " +
                     "  SUM(CASE WHEN type='expense' THEN amount ELSE 0 END) AS total_expense " +
                     "FROM expenses";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                BigDecimal income  = rs.getBigDecimal("total_income");
                BigDecimal expense = rs.getBigDecimal("total_expense");
                income  = income  == null ? BigDecimal.ZERO : income;
                expense = expense == null ? BigDecimal.ZERO : expense;
                BigDecimal balance = income.subtract(expense);
                return new BigDecimal[]{balance, income, expense};
            }
        }
        return new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
    }

    /* ── MAPPER ─────────────────────────────────────────────────── */
    private Expense map(ResultSet rs) throws SQLException {
        Expense e = new Expense();
        e.setId(rs.getInt("id"));
        e.setTitle(rs.getString("title"));
        e.setAmount(rs.getBigDecimal("amount"));
        e.setType(rs.getString("type"));
        e.setCategory(rs.getString("category"));
        e.setNote(rs.getString("note"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) e.setCreatedAt(ts.toLocalDateTime());
        return e;
    }
}
