package com.expense.servlet;

import com.expense.dao.ExpenseDAO;
import com.expense.model.Expense;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Single servlet acting as a lightweight REST-ish API.
 *
 * GET    /api/expenses          → JSON list of all expenses
 * GET    /api/expenses?summary  → JSON summary (balance/income/expense)
 * POST   /api/expenses          → add expense  (form params)
 * DELETE /api/expenses?id=N     → delete by id
 */
@WebServlet("/api/expenses")
public class ExpenseServlet extends HttpServlet {

    private final ExpenseDAO dao = new ExpenseDAO();

    // ── CORS helper ────────────────────────────────────────────────
    private void setCors(HttpServletResponse res) {
        res.setHeader("Access-Control-Allow-Origin",  "*");
        res.setHeader("Access-Control-Allow-Methods", "GET,POST,DELETE,OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Content-Type");
        res.setContentType("application/json;charset=UTF-8");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res) throws IOException {
        setCors(res);
        res.setStatus(HttpServletResponse.SC_OK);
    }

    // ── GET ────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        setCors(res);
        PrintWriter out = res.getWriter();
        try {
            if (req.getParameter("summary") != null) {
                // Return summary JSON
                BigDecimal[] s = dao.getSummary();
                out.print("{\"balance\":" + s[0] + ",\"income\":" + s[1] + ",\"expense\":" + s[2] + "}");
            } else {
                // Return all expenses as JSON array
                List<Expense> list = dao.getAllExpenses();
                out.print("[");
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) out.print(",");
                    out.print(toJson(list.get(i)));
                }
                out.print("]");
            }
        } catch (Exception e) {
            res.setStatus(500);
            out.print("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    // ── POST ───────────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        setCors(res);
        req.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();
        try {
            Expense e = new Expense(
                req.getParameter("title"),
                new BigDecimal(req.getParameter("amount")),
                req.getParameter("type"),
                req.getParameter("category"),
                req.getParameter("note") == null ? "" : req.getParameter("note")
            );
            boolean ok = dao.addExpense(e);
            out.print("{\"success\":" + ok + "}");
        } catch (Exception e) {
            res.setStatus(400);
            out.print("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    // ── DELETE ─────────────────────────────────────────────────────
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        setCors(res);
        PrintWriter out = res.getWriter();
        try {
            int id = Integer.parseInt(req.getParameter("id"));
            boolean ok = dao.deleteExpense(id);
            out.print("{\"success\":" + ok + "}");
        } catch (Exception e) {
            res.setStatus(400);
            out.print("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    // ── helpers ────────────────────────────────────────────────────
    private String toJson(Expense e) {
        return "{" +
               "\"id\":"       + e.getId()                  + "," +
               "\"title\":\""  + escape(e.getTitle())       + "\"," +
               "\"amount\":"   + e.getAmount()              + "," +
               "\"type\":\""   + e.getType()                + "\"," +
               "\"category\":\"" + escape(e.getCategory())  + "\"," +
               "\"note\":\""   + escape(e.getNote())        + "\"," +
               "\"createdAt\":\"" + (e.getCreatedAt() != null ? e.getCreatedAt().toString() : "") + "\"" +
               "}";
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
