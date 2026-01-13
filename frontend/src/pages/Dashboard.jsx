import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

export default function Dashboard() {
  const navigate = useNavigate();

  const [stats, setStats] = useState({
    totalTransactions: 0,
    totalAmount: 0,
    successRate: 0
  });

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

      const data = await res.json();
      const payments = Array.isArray(data) ? data : [];

      const totalTransactions = payments.length;
      const successfulPayments = payments.filter(p => p.status === "success");

      const totalAmount = successfulPayments.reduce(
        (sum, p) => sum + p.amount,
        0
      );

      const successRate =
        totalTransactions === 0
          ? 0
          : Math.round(
              (successfulPayments.length / totalTransactions) * 100
            );

      setStats({
        totalTransactions,
        totalAmount,
        successRate
      });
    } catch (err) {
      console.error("Failed to fetch dashboard data", err);
    }
  }

  return (
    <div data-test-id="dashboard" style={styles.container}>
      {/* Header */}
      <div style={styles.header}>
        <div>
          <h1 style={styles.title}>Merchant Dashboard</h1>
          <p style={styles.subtitle}>
            Monitor your payments and performance
          </p>
        </div>

        <button
          style={styles.txnButton}
          onClick={() => navigate("/dashboard/transactions")}
        >
          View Transactions →
        </button>
      </div>

      {/* API Credentials */}
      <div data-test-id="api-credentials" style={styles.card}>
        <h3 style={styles.cardTitle}>API Credentials</h3>

        <div style={styles.row}>
          <span>API Key</span>
          <span data-test-id="api-key" style={styles.mono}>
            {localStorage.getItem("apiKey")}
          </span>
        </div>

        <div style={styles.row}>
          <span>API Secret</span>
          <span data-test-id="api-secret" style={styles.mono}>
            {localStorage.getItem("apiSecret")}
          </span>
        </div>
      </div>

      {/* Stats */}
      <div data-test-id="stats-container" style={styles.stats}>
        <div style={{ ...styles.statBox, ...styles.statBlue }}>
          <div data-test-id="total-transactions" style={styles.statValue}>
            {stats.totalTransactions}
          </div>
          <small>Total Transactions</small>
        </div>

        <div style={{ ...styles.statBox, ...styles.statGreen }}>
          <div data-test-id="total-amount" style={styles.statValue}>
            ₹{(stats.totalAmount / 100).toFixed(2)}
          </div>
          <small>Total Amount</small>
        </div>

        <div style={{ ...styles.statBox, ...styles.statPurple }}>
          <div data-test-id="success-rate" style={styles.statValue}>
            {stats.successRate}%
          </div>
          <small>Success Rate</small>
        </div>
      </div>
    </div>
  );
}

/* ================= STYLES ================= */

const styles = {
  container: {
    padding: "32px",
    minHeight: "100vh",
    background: "linear-gradient(135deg, #f5f7fb, #eef1f7)"
  },

  header: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: "32px"
  },

  title: {
    margin: 0,
    fontSize: "28px"
  },

  subtitle: {
    margin: "4px 0 0",
    color: "#666"
  },

  txnButton: {
    background: "linear-gradient(135deg, #2a5298, #1e3c72)",
    color: "#fff",
    border: "none",
    padding: "12px 18px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
    transition: "transform 0.2s, box-shadow 0.2s",
    boxShadow: "0 6px 16px rgba(0,0,0,0.15)"
  },

  card: {
    background: "#fff",
    padding: "24px",
    borderRadius: "14px",
    marginBottom: "30px",
    boxShadow: "0 10px 25px rgba(0,0,0,0.08)"
  },

  cardTitle: {
    marginBottom: "16px"
  },

  row: {
    display: "flex",
    justifyContent: "space-between",
    marginTop: "12px",
    alignItems: "center"
  },

  mono: {
    fontFamily: "monospace",
    background: "#f1f3f6",
    padding: "6px 10px",
    borderRadius: "6px"
  },

  stats: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(240px, 1fr))",
    gap: "24px"
  },

  statBox: {
    background: "#fff",
    padding: "28px",
    borderRadius: "16px",
    textAlign: "center",
    boxShadow: "0 10px 25px rgba(0,0,0,0.08)",
    transition: "transform 0.2s, box-shadow 0.2s"
  },

  statValue: {
    fontSize: "32px",
    fontWeight: "700",
    marginBottom: "8px"
  },

  statBlue: {
    borderTop: "4px solid #3498db"
  },

  statGreen: {
    borderTop: "4px solid #2ecc71"
  },

  statPurple: {
    borderTop: "4px solid #9b59b6"
  }
};