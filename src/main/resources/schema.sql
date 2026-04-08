-- ============================================================
--  Crowdfunding Finance Management System — Database Schema
--  MySQL 8.x compatible
--  Note: Spring JPA with ddl-auto=update will also manage schema.
--        This script provides the explicit reference schema and
--        seed data for testing purposes.
-- ============================================================

-- Create (or switch to) the database
CREATE DATABASE IF NOT EXISTS CrowdfundingDB
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE CrowdfundingDB;

-- ============================================================
--  TABLE: administrator
--  Platform admins who own and manage fundraising campaigns.
--  They receive 99% of each donation (net_amount).
-- ============================================================
CREATE TABLE IF NOT EXISTS administrator (
    admin_id        BIGINT          NOT NULL AUTO_INCREMENT,
    name            VARCHAR(100)    NOT NULL,
    email           VARCHAR(100)    NOT NULL UNIQUE,
    total_earnings  DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    created_at      DATETIME        NOT NULL,
    PRIMARY KEY (admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  TABLE: donor
--  Individuals who donate money to fundraisers.
--  total_donated tracks their gross cumulative contributions.
-- ============================================================
CREATE TABLE IF NOT EXISTS donor (
    donor_id        BIGINT          NOT NULL AUTO_INCREMENT,
    dname           VARCHAR(100)    NOT NULL,
    demail          VARCHAR(100)    NOT NULL UNIQUE,
    dphone          VARCHAR(20),
    total_donated   DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    created_at      DATETIME        NOT NULL,
    PRIMARY KEY (donor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  TABLE: fundraiser
--  A crowdfunding campaign managed by an administrator.
--  raised_amount + remaining_amount = goal_amount (maintained by app logic).
--  Status lifecycle: Planned → Active → 'Goal Reached' / 'Closed'
-- ============================================================
CREATE TABLE IF NOT EXISTS fundraiser (
    fundraiser_no           BIGINT          NOT NULL AUTO_INCREMENT,
    admin_id                BIGINT          NOT NULL,
    bank_details            VARCHAR(255),
    title                   VARCHAR(200)    NOT NULL,
    description             TEXT,
    goal_amount             DECIMAL(12,2)   NOT NULL,
    raised_amount           DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    remaining_amount        DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    deadline                DATE,
    status                  VARCHAR(50)     NOT NULL DEFAULT 'Planned'
                                CHECK (status IN ('Planned','Active','Goal Reached','Closed')),
    fundraiser_owner_name   VARCHAR(150),
    created_date            DATETIME        NOT NULL,
    PRIMARY KEY (fundraiser_no),
    CONSTRAINT fk_fundraiser_admin FOREIGN KEY (admin_id)
        REFERENCES administrator(admin_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  TABLE: transaction
--  Each individual donation.
--  platform_fee = amount * 0.01   (1%  — platform revenue)
--  net_amount   = amount * 0.99   (99% — goes to fundraiser/admin)
-- ============================================================
CREATE TABLE IF NOT EXISTS transaction (
    transaction_id      BIGINT          NOT NULL AUTO_INCREMENT,
    donor_id            BIGINT          NOT NULL,
    fundraiser_no       BIGINT          NOT NULL,
    amount              DECIMAL(12,2)   NOT NULL,
    platform_fee        DECIMAL(12,2)   NOT NULL,
    net_amount          DECIMAL(12,2)   NOT NULL,
    payment_mode        VARCHAR(50)     NOT NULL,
    transaction_date    DATETIME        NOT NULL,
    PRIMARY KEY (transaction_id),
    CONSTRAINT fk_transaction_donor      FOREIGN KEY (donor_id)
        REFERENCES donor(donor_id)        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_transaction_fundraiser FOREIGN KEY (fundraiser_no)
        REFERENCES fundraiser(fundraiser_no) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  TABLE: payroll
--  Auto-generated when a transaction is processed.
--  One payroll row per transaction (enforced by UNIQUE on transaction_id).
--  admin_earnings       = net_amount   (99% credited to admin)
--  platform_fee_deducted = platform_fee (1% kept by platform)
-- ============================================================
CREATE TABLE IF NOT EXISTS payroll (
    payroll_id              BIGINT          NOT NULL AUTO_INCREMENT,
    admin_id                BIGINT          NOT NULL,
    fundraiser_no           BIGINT          NOT NULL,
    transaction_id          BIGINT          NOT NULL UNIQUE,   -- one payroll per transaction
    admin_earnings          DECIMAL(12,2)   NOT NULL,
    platform_fee_deducted   DECIMAL(12,2)   NOT NULL,
    payout_date             DATETIME,
    payout_type             VARCHAR(50)     NOT NULL DEFAULT 'Automatic',
    PRIMARY KEY (payroll_id),
    CONSTRAINT fk_payroll_admin       FOREIGN KEY (admin_id)
        REFERENCES administrator(admin_id)    ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_payroll_fundraiser  FOREIGN KEY (fundraiser_no)
        REFERENCES fundraiser(fundraiser_no)  ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_payroll_transaction FOREIGN KEY (transaction_id)
        REFERENCES transaction(transaction_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  TABLE: visit
--  Tracks each time a donor views a fundraiser page.
--  interest_level is computed by the application's Strategy Pattern
--  based on cumulative visit count for the donor-fundraiser pair.
-- ============================================================
CREATE TABLE IF NOT EXISTS visit (
    visit_id        BIGINT          NOT NULL AUTO_INCREMENT,
    donor_id        BIGINT          NOT NULL,
    fundraiser_no   BIGINT          NOT NULL,
    visit_date      DATETIME        NOT NULL,
    duration        INT             DEFAULT 0 COMMENT 'Time spent on page in minutes',
    interest_level  VARCHAR(20)     NOT NULL DEFAULT 'Low'
                        CHECK (interest_level IN ('Low','Medium','High','Very High')),
    visit_type      VARCHAR(20)     NOT NULL DEFAULT 'View'
                        CHECK (visit_type IN ('View','Transaction')),
    PRIMARY KEY (visit_id),
    CONSTRAINT fk_visit_donor      FOREIGN KEY (donor_id)
        REFERENCES donor(donor_id)        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_visit_fundraiser FOREIGN KEY (fundraiser_no)
        REFERENCES fundraiser(fundraiser_no) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  SAMPLE DATA — for development and testing
--  (Amounts are realistic, fee math checks out)
-- ============================================================

-- Administrators
INSERT INTO administrator (name, email, total_earnings, created_at) VALUES
    ('Alice Johnson',   'alice@crowdfund.com',  297000.00, '2024-01-10 09:00:00'),
    ('Bob Martinez',    'bob@crowdfund.com',    148500.00, '2024-01-15 10:30:00'),
    ('Carol Williams',  'carol@crowdfund.com',   49500.00, '2024-02-01 11:00:00'),
    ('David Kim',       'david@crowdfund.com',       0.00, '2024-03-01 08:45:00');

-- Donors
INSERT INTO donor (dname, demail, dphone, total_donated, created_at) VALUES
    ('Emma Thompson',   'emma@gmail.com',    '+1-555-0101', 5000.00, '2024-01-20 14:00:00'),
    ('Liam Anderson',   'liam@gmail.com',    '+1-555-0102', 2500.00, '2024-01-25 15:30:00'),
    ('Olivia Brown',    'olivia@gmail.com',  '+1-555-0103', 1000.00, '2024-02-05 09:15:00'),
    ('Noah Wilson',     'noah@gmail.com',    '+1-555-0104', 10000.00,'2024-02-10 16:45:00');

-- Fundraisers
-- raised_amount and remaining_amount will be updated by the app as transactions come in;
-- initial values reflect what the sample transactions below produce.
INSERT INTO fundraiser (admin_id, bank_details, title, description, goal_amount, raised_amount, remaining_amount, deadline, status, fundraiser_owner_name, created_date) VALUES
    (1, 'IBAN: US12345678901234', 'Clean Water Initiative',
     'Providing clean drinking water to rural communities in Sub-Saharan Africa.',
     300000.00, 297000.00, 3000.00, '2025-12-31', 'Active', 'Alice Johnson', '2024-01-12 10:00:00'),

    (2, 'IBAN: US09876543210987', 'Tech Education for Youth',
     'Funding laptops and coding bootcamps for underprivileged students.',
     150000.00, 148500.00, 1500.00, '2025-06-30', 'Active', 'Bob Martinez', '2024-01-20 11:00:00'),

    (3, 'IBAN: US11223344556677', 'Wildlife Conservation Fund',
     'Protecting endangered species and their natural habitats.',
     50000.00, 50000.00, 0.00, '2025-03-31', 'Goal Reached', 'Carol Williams', '2024-02-03 09:00:00'),

    (4, 'IBAN: US99887766554433', 'Community Garden Project',
     'Building sustainable community gardens in urban neighbourhoods.',
     20000.00, 0.00, 20000.00, '2026-01-01', 'Planned', 'David Kim', '2024-03-05 14:00:00');

-- Transactions
-- Verify: amount=5000, fee=50 (1%), net=4950 (99%)
INSERT INTO transaction (donor_id, fundraiser_no, amount, platform_fee, net_amount, payment_mode, transaction_date) VALUES
    (1, 1, 5000.00,  50.00,  4950.00, 'Credit Card',   '2024-02-01 10:00:00'),
    (4, 1, 2000.00,  20.00,  1980.00, 'UPI',           '2024-02-05 14:30:00'),
    (2, 2, 2500.00,  25.00,  2475.00, 'Bank Transfer', '2024-02-10 11:00:00'),
    (3, 3, 1000.00,  10.00,   990.00, 'PayPal',        '2024-02-15 16:00:00');

-- Payroll (one record per transaction — auto-created by the app in production)
INSERT INTO payroll (admin_id, fundraiser_no, transaction_id, admin_earnings, platform_fee_deducted, payout_date, payout_type) VALUES
    (1, 1, 1, 4950.00, 50.00, '2024-02-01 10:00:01', 'Automatic'),
    (1, 1, 2, 1980.00, 20.00, '2024-02-05 14:30:01', 'Automatic'),
    (2, 2, 3, 2475.00, 25.00, '2024-02-10 11:00:01', 'Automatic'),
    (3, 3, 4,  990.00, 10.00, '2024-02-15 16:00:01', 'Automatic');

-- Visits
-- interest_level set to reflect the visit counts at time of insertion
INSERT INTO visit (donor_id, fundraiser_no, visit_date, duration, interest_level, visit_type) VALUES
    (1, 1, '2024-01-25 09:00:00', 5,  'Low',       'View'),
    (1, 1, '2024-01-28 10:00:00', 8,  'Low',       'View'),
    (1, 1, '2024-01-30 11:00:00', 12, 'Medium',    'View'),
    (1, 1, '2024-02-01 10:00:00', 20, 'Medium',    'Transaction'),
    (4, 1, '2024-02-01 13:00:00', 6,  'Low',       'View'),
    (4, 1, '2024-02-05 14:00:00', 15, 'Low',       'Transaction'),
    (2, 2, '2024-02-08 09:00:00', 7,  'Low',       'View'),
    (2, 2, '2024-02-10 10:30:00', 18, 'Low',       'Transaction'),
    (3, 3, '2024-02-12 14:00:00', 4,  'Low',       'View'),
    (3, 3, '2024-02-15 15:30:00', 22, 'Low',       'Transaction');
