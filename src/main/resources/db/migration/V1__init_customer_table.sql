CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE customer_services (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id VARCHAR(100) NOT NULL,
    service_type VARCHAR(50) NOT NULL CHECK (service_type IN ('HOSTING', 'PEC', 'SPID', 'FATTURAZIONE')),
    activation_date DATE NOT NULL,
    expiration_date DATE NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('ACTIVE', 'EXPIRED', 'PENDING_RENEWAL'))
);

ALTER TABLE customer_services
ADD CONSTRAINT uq_customer_service UNIQUE (customer_id, service_type, activation_date, expiration_date);

CREATE INDEX idx_customer_services_customer_id ON customer_services(customer_id);
CREATE INDEX idx_customer_services_status ON customer_services(status);
CREATE INDEX idx_customer_services_expiration_date ON customer_services(expiration_date);

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(50) NOT NULL CHECK (type IN ('EXPIRED_ALERT', 'UPSELL_EMAIL')),
    status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
    customer_id VARCHAR(100),
    retry_count INT NOT NULL DEFAULT 0,
    created_datetime TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_datetime TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_notifications_status ON notifications(status);