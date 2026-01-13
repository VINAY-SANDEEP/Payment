import React from "react";
import { useSearchParams, useNavigate } from "react-router-dom";

function Failure() {
  const [params] = useSearchParams();
  const navigate = useNavigate();

  const paymentId = params.get("payment_id");

  return (
    <div
      style={{
        height: "100vh",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        background: "#f8d7da",
      }}
    >
      <div
        style={{
          padding: 30,
          background: "#fff",
          borderRadius: 10,
          textAlign: "center",
        }}
      >
        <h2>❌ Payment Failed</h2>
        <p>Payment could not be processed.</p>
        <p>
          <strong>Payment ID:</strong> {paymentId}
        </p>

        <button
          onClick={() => navigate(-1)}
          style={{ padding: 10, marginTop: 10 }}
        >
          Try Again
        </button>
      </div>
    </div>
  );
}

export default Failure;