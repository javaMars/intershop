INSERT INTO account_balance (user_id, balance)
VALUES ('user123', 5000.00)
ON CONFLICT (user_id) DO UPDATE SET
    balance = 5000.00,
    date_update = CURRENT_TIMESTAMP;
