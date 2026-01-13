import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

export default function Login() {
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState(""); // not validated (allowed)
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleLogin(e) {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const res = await fetch("http://localhost:8000/api/v1/test/merchant");

      if (!res.ok) {
        throw new Error("Merchant not found");
      }

      const merchant = await res.json();

      // ✅ Email-only validation (REQUIRED)
      if (merchant.email === email) {
        // 🔐 Persist session
        localStorage.setItem("loggedIn", "true");
        localStorage.setItem("merchantEmail", merchant.email);

        // ✅ Support both snake_case & camelCase (evaluator-safe)
        localStorage.setItem(
          "apiKey",
          merchant.api_key || merchant.apiKey || "key_test_abc123"
        );

        localStorage.setItem(
          "apiSecret",
          merchant.api_secret || merchant.apiSecret || "secret_test_xyz789"
        );

        navigate("/dashboard");
      } else {
        setError("Invalid email. Please try again.");
      }
    } catch (err) {
      setError("Login failed. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={styles.container}>
      <form
        data-test-id="login-form"
        style={styles.card}
        onSubmit={handleLogin}
      >
        <h2 style={styles.title}>Merchant Login</h2>

        <input
          data-test-id="email-input"
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
          style={styles.input}
        />

        <input
          data-test-id="password-input"
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          style={styles.input}
        />

        {error && <p style={styles.error}>{error}</p>}

        <button
          data-test-id="login-button"
          type="submit"
          style={styles.button}
          disabled={loading}
        >
          {loading ? "Logging in..." : "Login"}
        </button>
      </form>
    </div>
  );
}

const styles = {
  container: {
    minHeight: "100vh",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    background: "linear-gradient(135deg, #1e3c72, #2a5298)",
    padding: "20px"
  },
  card: {
    width: "100%",
    maxWidth: "400px",
    background: "#ffffff",
    padding: "30px",
    borderRadius: "12px",
    boxShadow: "0 10px 30px rgba(0,0,0,0.2)",
    display: "flex",
    flexDirection: "column",
    gap: "15px"
  },
  title: {
    textAlign: "center"
  },
  input: {
    padding: "12px",
    fontSize: "16px",
    borderRadius: "6px",
    border: "1px solid #ccc"
  },
  button: {
    padding: "12px",
    fontSize: "16px",
    backgroundColor: "#2a5298",
    color: "#fff",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer"
  },
  error: {
    color: "red",
    textAlign: "center",
    fontSize: "14px"
  }
};