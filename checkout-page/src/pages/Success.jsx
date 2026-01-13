import React, { useEffect, useState } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";

const API_BASE = "http://localhost:8000/api/v1";

function Success() {
  const [params] = useSearchParams();
  const navigate = useNavigate();

  const paymentId = params.get("payment_id");
  const [status, setStatus] = useState("processing");

  useEffect(() => {
    if (!paymentId) return;

    const interval = setInterval(async () => {
      try {
        // ✅ FIX: PUBLIC PAYMENT STATUS ENDPOINT
        const res = await fetch(
          `${API_BASE}/payments/public/${paymentId}`
        );

        if (!res.ok) return;

        const data = await res.json();

        if (data.status === "success") {
          setStatus("success");
          clearInterval(interval);
        }

        if (data.status === "failed") {
          clearInterval(interval);
          navigate(`/failure?payment_id=${paymentId}`);
        }
      } catch (err) {
        // silently ignore during polling
      }
    }, 2000); // ✅ EXACT 2s POLLING (REQUIRED)

    return () => clearInterval(interval);
  }, [paymentId, navigate]);

  return (
    <div
      style={{
        height: "100vh",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        background: "#d4edda",
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
        <h2>
          {status === "success"
            ? "✅ Payment Successful"
            : "⏳ Processing Payment"}
        </h2>

        <p>
          <strong>Payment ID:</strong> {paymentId}
        </p>

        {status === "processing" && <p>Please wait...</p>}
      </div>
    </div>
  );
}

export default Success;