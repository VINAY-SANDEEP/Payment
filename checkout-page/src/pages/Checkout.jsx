import React, { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

const API_BASE = "http://localhost:8000/api/v1";

function Checkout() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const orderId = params.get("order_id");

  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [method, setMethod] = useState(null);
  const [processing, setProcessing] = useState(false);

  const [vpa, setVpa] = useState("");
  const [card, setCard] = useState({
    number: "",
    expiry: "",
    cvv: "",
    name: "",
  });

  // Fetch order (public)
  useEffect(() => {
    if (!orderId) return;

    fetch(`${API_BASE}/orders/${orderId}/public`)
      .then((res) => res.json())
      .then((data) => {
        setOrder(data);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, [orderId]);

  // Poll payment status
  const pollPaymentStatus = async (paymentId) => {
    try {
      const res = await fetch(`${API_BASE}/payments/public/${paymentId}`);
      const data = await res.json();

      if (data.status === "success") {
        navigate(`/success?payment_id=${paymentId}`);
      } else if (data.status === "failed") {
        navigate(`/failure?payment_id=${paymentId}`);
      } else {
        setTimeout(() => pollPaymentStatus(paymentId), 2000);
      }
    } catch {
      navigate(`/failure?payment_id=network_error`);
    }
  };

  // Submit payment
  const submitPayment = async (body) => {
    try {
      setProcessing(true);

      const res = await fetch(`${API_BASE}/payments/public`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });

      const data = await res.json();

      if (res.ok && data?.id) {
        pollPaymentStatus(data.id);
      } else {
        navigate(`/failure?payment_id=unknown`);
      }
    } catch {
      navigate(`/failure?payment_id=network_error`);
    }
  };

  if (loading) return <h2 style={{ textAlign: "center" }}>Loading...</h2>;
  if (!order) return <h2 style={{ textAlign: "center" }}>Order not found</h2>;

  return (
    <div
      data-test-id="checkout-container"
      style={{
        minHeight: "100vh",
        background: "#f4f6fb",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        padding: 20,
      }}
    >
      <div
        style={{
          width: 380,
          background: "#ffffff",
          padding: 24,
          borderRadius: 12,
          boxShadow: "0 10px 30px rgba(0,0,0,0.1)",
        }}
      >
        {/* Order Summary */}
        <div
          data-test-id="order-summary"
          style={{
            marginBottom: 20,
            borderBottom: "1px solid #eee",
            paddingBottom: 15,
          }}
        >
          <h2 style={{ marginBottom: 10 }}>Complete Payment</h2>
          <p>
            <strong>Amount:</strong>{" "}
            <span data-test-id="order-amount">
              ₹{order.amount / 100}
            </span>
          </p>
          <p>
            <strong>Order ID:</strong>{" "}
            <span data-test-id="order-id">{order.id}</span>
          </p>
        </div>

        {/* Payment Methods */}
        <div
          data-test-id="payment-methods"
          style={{ display: "flex", gap: 10, marginBottom: 20 }}
        >
          <button
            data-test-id="method-upi"
            onClick={() => setMethod("upi")}
            style={{
              flex: 1,
              padding: 10,
              borderRadius: 6,
              border: method === "upi" ? "2px solid #2a5298" : "1px solid #ccc",
              background: method === "upi" ? "#eef3ff" : "#fff",
              cursor: "pointer",
              fontWeight: "bold",
            }}
          >
            UPI
          </button>

          <button
            data-test-id="method-card"
            onClick={() => setMethod("card")}
            style={{
              flex: 1,
              padding: 10,
              borderRadius: 6,
              border: method === "card" ? "2px solid #2a5298" : "1px solid #ccc",
              background: method === "card" ? "#eef3ff" : "#fff",
              cursor: "pointer",
              fontWeight: "bold",
            }}
          >
            Card
          </button>
        </div>

        {/* UPI Form */}
        {method === "upi" && !processing && (
          <form
            data-test-id="upi-form"
            onSubmit={(e) => {
              e.preventDefault();
              submitPayment({
                order_id: order.id,
                method: "upi",
                vpa,
              });
            }}
          >
            <input
              data-test-id="vpa-input"
              placeholder="username@bank"
              value={vpa}
              onChange={(e) => setVpa(e.target.value)}
              style={inputStyle}
              required
            />
            <button data-test-id="pay-button" style={payButtonStyle}>
              Pay ₹{order.amount / 100}
            </button>
          </form>
        )}

        {/* Card Form */}
        {method === "card" && !processing && (
          <form
            data-test-id="card-form"
            onSubmit={(e) => {
              e.preventDefault();
              submitPayment({
                order_id: order.id,
                method: "card",
                card: {
                  number: card.number,
                  expiry_month: card.expiry.split("/")[0],
                  expiry_year: card.expiry.split("/")[1],
                  cvv: card.cvv,
                  holder_name: card.name,
                },
              });
            }}
          >
            <input
              data-test-id="card-number-input"
              placeholder="Card Number"
              style={inputStyle}
              onChange={(e) =>
                setCard({ ...card, number: e.target.value })
              }
              required
            />
            <input
              data-test-id="expiry-input"
              placeholder="MM/YY"
              style={inputStyle}
              onChange={(e) =>
                setCard({ ...card, expiry: e.target.value })
              }
              required
            />
            <input
              data-test-id="cvv-input"
              placeholder="CVV"
              style={inputStyle}
              onChange={(e) =>
                setCard({ ...card, cvv: e.target.value })
              }
              required
            />
            <input
              data-test-id="cardholder-name-input"
              placeholder="Name on Card"
              style={inputStyle}
              onChange={(e) =>
                setCard({ ...card, name: e.target.value })
              }
              required
            />
            <button data-test-id="pay-button" style={payButtonStyle}>
              Pay ₹{order.amount / 100}
            </button>
          </form>
        )}

        {/* Processing */}
        {processing && (
          <div
            data-test-id="processing-state"
            style={{ textAlign: "center", marginTop: 20 }}
          >
            <p data-test-id="processing-message">
              Processing payment...
            </p>
          </div>
        )}
      </div>
    </div>
  );
}

const inputStyle = {
  width: "100%",
  padding: 10,
  marginBottom: 12,
  borderRadius: 6,
  border: "1px solid #ccc",
  fontSize: 14,
};

const payButtonStyle = {
  width: "100%",
  padding: 12,
  background: "#2a5298",
  color: "#fff",
  border: "none",
  borderRadius: 6,
  cursor: "pointer",
  fontSize: 16,
  fontWeight: "bold",
};

export default Checkout;