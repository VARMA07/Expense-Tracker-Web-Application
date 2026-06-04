/**
 * Expense Tracker — Frontend
 * Connects to Java Servlet backend at /api/expenses
 */

const API = 'http://localhost:8080/expense-tracker/api/expenses';

// ── State ───────────────────────────────────────────────────────
let transactions = [];
let selectedType = 'income';
let activeFilter = 'all';

// ── DOM refs ────────────────────────────────────────────────────
const balanceEl     = document.getElementById('balance');
const incomeEl      = document.getElementById('income');
const expenseEl     = document.getElementById('expenseTotal');
const txList        = document.getElementById('txList');
const emptyState    = document.getElementById('emptyState');
const formMsg       = document.getElementById('formMsg');

const titleInput    = document.getElementById('title');
const amountInput   = document.getElementById('amount');
const categoryInput = document.getElementById('category');
const noteInput     = document.getElementById('note');

// ── Init ────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  fetchAll();
  bindTypeToggle();
  bindFilters();
  document.getElementById('addBtn').addEventListener('click', addTransaction);
});

// ── Fetch all transactions + summary ────────────────────────────
async function fetchAll() {
  try {
    const [txRes, sumRes] = await Promise.all([
      fetch(API),
      fetch(API + '?summary')
    ]);
    transactions = await txRes.json();
    const summary = await sumRes.json();
    updateSummary(summary);
    renderList();
  } catch (err) {
    console.error('Fetch error:', err);
    showMsg('Could not connect to server.', 'err');
  }
}

// ── Update balance cards ─────────────────────────────────────────
function updateSummary({ balance, income, expense }) {
  balanceEl.textContent    = fmt(balance);
  incomeEl.textContent     = fmt(income);
  expenseEl.textContent    = fmt(expense);
}

// ── Render list ──────────────────────────────────────────────────
function renderList() {
  const filtered = activeFilter === 'all'
    ? transactions
    : transactions.filter(t => t.type === activeFilter);

  txList.innerHTML = '';

  if (filtered.length === 0) {
    txList.innerHTML = '<li class="empty-state">No transactions yet</li>';
    return;
  }

  filtered.forEach(t => {
    const li = document.createElement('li');
    li.className = 'tx-item';
    li.dataset.id = t.id;

    const isIncome = t.type === 'income';
    const icon = categoryIcon(t.category);
    const date = t.createdAt ? formatDate(t.createdAt) : '';

    li.innerHTML = `
      <div class="tx-icon ${t.type}">${icon}</div>
      <div class="tx-info">
        <div class="tx-title">${escHtml(t.title)}</div>
        <div class="tx-meta">${escHtml(t.category)}${t.note ? ' · ' + escHtml(t.note) : ''}${date ? ' · ' + date : ''}</div>
      </div>
      <span class="tx-amount ${t.type}">${isIncome ? '+' : '−'}${fmt(t.amount)}</span>
      <button class="btn-del" title="Delete" onclick="deleteTransaction(${t.id})">✕</button>
    `;
    txList.appendChild(li);
  });
}

// ── Add transaction ──────────────────────────────────────────────
async function addTransaction() {
  const title    = titleInput.value.trim();
  const amount   = parseFloat(amountInput.value);
  const category = categoryInput.value;

  if (!title)           return showMsg('Title is required.', 'err');
  if (!amount || amount <= 0) return showMsg('Enter a valid amount.', 'err');
  if (!category)        return showMsg('Select a category.', 'err');

  const body = new URLSearchParams({
    title,
    amount: amount.toFixed(2),
    type: selectedType,
    category,
    note: noteInput.value.trim()
  });

  try {
    const res  = await fetch(API, { method: 'POST', body });
    const data = await res.json();
    if (data.success) {
      showMsg('Transaction added!', 'ok');
      resetForm();
      fetchAll();
    } else {
      showMsg(data.error || 'Failed to add.', 'err');
    }
  } catch (err) {
    showMsg('Server error.', 'err');
  }
}

// ── Delete transaction ───────────────────────────────────────────
async function deleteTransaction(id) {
  try {
    const res  = await fetch(API + '?id=' + id, { method: 'DELETE' });
    const data = await res.json();
    if (data.success) fetchAll();
  } catch (err) {
    showMsg('Could not delete.', 'err');
  }
}

// ── Type toggle ──────────────────────────────────────────────────
function bindTypeToggle() {
  document.querySelectorAll('.toggle').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.toggle').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      selectedType = btn.dataset.type;
    });
  });
}

// ── Filter buttons ───────────────────────────────────────────────
function bindFilters() {
  document.querySelectorAll('.filter').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.filter').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      activeFilter = btn.dataset.filter;
      renderList();
    });
  });
}

// ── Helpers ──────────────────────────────────────────────────────
function fmt(n) {
  return '₹' + parseFloat(n || 0).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatDate(str) {
  try {
    const d = new Date(str.replace('T', ' '));
    return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short' });
  } catch { return ''; }
}

function escHtml(str) {
  return String(str || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function showMsg(msg, type) {
  formMsg.textContent  = msg;
  formMsg.className    = 'form-msg ' + type;
  setTimeout(() => { formMsg.textContent = ''; formMsg.className = 'form-msg'; }, 3000);
}

function resetForm() {
  titleInput.value    = '';
  amountInput.value   = '';
  categoryInput.value = '';
  noteInput.value     = '';
}

function categoryIcon(cat) {
  const map = {
    'Salary':'💼','Freelance':'💻','Investment':'📈','Gift':'🎁','Other Income':'💰',
    'Food':'🍱','Rent':'🏠','Transport':'🚌','Shopping':'🛍','Health':'💊',
    'Utilities':'💡','Entertainment':'🎬','Education':'📚','Other Expense':'📌'
  };
  return map[cat] || '💳';
}
