Payment Gateway – Multi-Method Processing & Hosted Checkout

Overview

This project is a complete payment gateway system similar to Razorpay or Stripe.
It allows merchants to create payment orders via API and lets customers complete payments using a hosted checkout page supporting UPI and Card payments.

The system includes:

Merchant authentication using API key and secret

Order creation and management

UPI and Card payment processing

Hosted checkout page

Merchant dashboard for monitoring transactions

PostgreSQL database

Fully dockerized deployment





System Architecture

The platform runs as four services using Docker Compose:

Service	Purpose	Port

API	Backend payment gateway	8000
Dashboard	Merchant dashboard	3000
Checkout	Hosted checkout page	3001
PostgreSQL	Database	5432


All services are started using a single docker-compose up -d command.



Folder Structure





Running the Project

1. Start all services

From the root folder:

docker-compose up -d

This starts:

PostgreSQL

API Server

Merchant Dashboard

Checkout Page





2. Access URLs

Service	URL

API	http://localhost:8000
Dashboard	http://localhost:3000
Checkout	http://localhost:3001




Test Merchant Credentials

These credentials are automatically seeded into the database on startup.

Field	Value

Email	test@example.com
API Key	key_test_abc123
API Secret	secret_test_xyz789




API Flow

1. Create Order

Merchant creates an order using API credentials.

POST /api/v1/orders

Returns order_id




2. Customer Checkout

Customer is redirected to:

http://localhost:3001/checkout?order_id=ORDER_ID

They choose UPI or Card and complete payment.



3. Payment Processing

The system:

Validates UPI VPA or card details

Simulates bank processing

Updates payment status to success or failed

Updates dashboard and transactions automatically




Payment Methods

UPI

Validates VPA format

Simulates 90% success rate


Card

Uses Luhn algorithm

Detects card network (Visa, Mastercard, Amex, RuPay)

Validates expiry

Simulates 95% success rate

Stores only last 4 digits





Test Mode

The API supports deterministic testing using environment variables:

TEST_MODE=true
TEST_PAYMENT_SUCCESS=true
TEST_PROCESSING_DELAY=1000

This allows controlled success or failure during automated evaluation.




Dashboard Features

The merchant dashboard shows:

API credentials

Total transactions

Total successful amount

Success rate

All transactions (success, failed, processing)




Checkout Page

The checkout page:

Displays order details

Allows UPI or Card payment

Shows processing state

Displays success or failure

Polls backend every 2 seconds for status





Health Check

GET /health

Returns application and database status.



