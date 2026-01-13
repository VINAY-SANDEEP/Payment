import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

export default function Transactions() {
  const navigate = useNavigate();
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    fetchPayments();
  }, []);

  async function fetchPayments() {
    try {
      const apiKey = localStorage.getItem("apiKey");
      const apiSecret = localStorage.getItem("apiSecret");

      const res = await fetch("http://localhost:8000/api/v1/payments", {
        headers: {
          "X-Api-Key": apiKey,
          "X-Api-Secret": apiSecret
        }
      });

      if (!res.ok) throw new Error("Failed to load payments");

      const data = await res.json();
      setPayments(Array.isArray(data) ? data : []);
    } catch (err) {
      setError("Unable to fetch transactions");
      setPayments([]);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={styles.page}>
      {/* Header */}
      <div style={styles.header}>
        <div>
          <h2 style={styles.title}>Transactions</h2>
          <p style={styles.subtitle}>
            All payment activity for your account
          </p>
        </div>

        <button
          style={styles.backButton}
          onClick={() => navigate("/dashboard")}
        >
          ← Back to Dashboard
        </button>
      </div>

      {/* Table Wrapper */}
      <div style={styles.tableWrapper}>
        <div style={styles.card}>
          {loading ? (
            <p>Loading transactions...</p>
          ) : error ? (
            <p style={{ color: "red" }}>{error}</p>
          ) : (
            <table
              data-test-id="transactions-table"
              style={styles.table}
            >
              <thead>
                <tr>
                  <th>Payment ID</th>
                  <th>Order ID</th>
                  <th>Amount</th>
                  <th>Method</th>
                  <th>Status</th>
                  <th>Created</th>
                </tr>
              </thead>

              <tbody>
                {payments.length === 0 ? (
                  <tr>
                    <td colSpan="6" style={{ textAlign: "center" }}>
                      No transactions found
                    </td>
                  </tr>
                ) : (
                  payments.map((p) => (
                    <tr
                      key={p.id}
                      data-test-id="transaction-row"
                      data-payment-id={p.id}
                    >
                      <td data-test-id="payment-id" style={styles.mono}>
                        {p.id}
                      </td>

                      <td data-test-id="order-id" style={styles.mono}>
                        {p.orderId || p.order_id}
                      </td>

                      <td data-test-id="amount">
                        ₹{(p.amount / 100).toFixed(2)}
                      </td>

                      <td data-test-id="method">
                        {p.method.toUpperCase()}
                      </td>

                      <td data-test-id="status">
                        <span
                          style={{
                            ...styles.status,
                            ...(p.status === "success"
                              ? styles.success
                              : p.status === "failed"
                              ? styles.failed
                              : styles.processing)
                          }}
                        >
                          {p.status}
                        </span>
                      </td>

                      <td data-test-id="created-at">
                        {new Date(
                          p.createdAt || p.created_at
                        ).toLocaleString()}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}

/* ================= STYLES ================= */

const styles = {
  page: {
    padding: "32px",
    minHeight: "100vh",
    background: "#f5f7fb"
  },

  header: {
    maxWidth: "1200px",
    margin: "0 auto 24px",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center"
  },

  title: {
    margin: 0,
    fontSize: "28px"
  },

  subtitle: {
    marginTop: "6px",
    color: "#666"
  },

  backButton: {
    background: "#2a5298",
    color: "#fff",
    border: "none",
    padding: "10px 16px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600"
  },

  tableWrapper: {
    maxWidth: "1200px",
    margin: "0 auto"
  },

  card: {
    background: "#fff",
    borderRadius: "14px",
    padding: "16px",
    boxShadow: "0 10px 25px rgba(0,0,0,0.08)",
    overflowX: "auto" // ✅ FIXES LEFT SHIFT ISSUE
  },

  table: {
    width: "100%",
    minWidth: "900px", // ✅ prevents cramping
    borderCollapse: "collapse"
  },

  mono: {
    fontFamily: "monospace",
    fontSize: "13px"
  },

  status: {
    padding: "4px 10px",
    borderRadius: "12px",
    fontSize: "12px",
    fontWeight: "600",
    textTransform: "capitalize"
  },

  success: {
    background: "#eafaf1",
    color: "#2ecc71"
  },

  failed: {
    background: "#fdecea",
    color: "#e74c3c"
  },

  processing: {
    background: "#fff6e5",
    color: "#f39c12"
  }
};