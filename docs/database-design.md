-- =====================================================
-- School & Madrasa Management SaaS
-- Multi-Tenant MySQL 8 Schema
-- =====================================================

CREATE DATABASE IF NOT EXISTS school_madrasa_saas;
USE school_madrasa_saas;

-- =====================================================
-- TENANTS
-- =====================================================

CREATE TABLE tenants (
id BIGINT PRIMARY KEY AUTO_INCREMENT,
tenant_code VARCHAR(50) NOT NULL UNIQUE,
institution_name VARCHAR(255) NOT NULL,
institution_type ENUM('SCHOOL','MADRASA','BOTH') DEFAULT 'BOTH',

    email VARCHAR(255),
    phone VARCHAR(30),

    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),

    logo_url VARCHAR(500),

    subscription_plan VARCHAR(100),
    subscription_start_date DATE,
    subscription_end_date DATE,

    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);

-- =====================================================
-- ROLES
-- =====================================================

CREATE TABLE roles (
id BIGINT PRIMARY KEY AUTO_INCREMENT,

    tenant_id BIGINT NULL,

    role_name VARCHAR(100) NOT NULL,

    description VARCHAR(255),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_roles_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id)
);

-- =====================================================
-- USERS
-- =====================================================

CREATE TABLE users (
id BIGINT PRIMARY KEY AUTO_INCREMENT,

    tenant_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,

    username VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    mobile VARCHAR(20),

    password_hash VARCHAR(500) NOT NULL,

    first_name VARCHAR(100),
    last_name VARCHAR(100),

    is_active BOOLEAN DEFAULT TRUE,

    last_login_at DATETIME NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_user_tenant_username (
        tenant_id,
        username
    ),

    CONSTRAINT fk_users_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id),

    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
);

-- =====================================================
-- PARENTS
-- =====================================================

CREATE TABLE parents (
id BIGINT PRIMARY KEY AUTO_INCREMENT,

    tenant_id BIGINT NOT NULL,

    user_id BIGINT NULL,

    father_name VARCHAR(255),
    mother_name VARCHAR(255),
    guardian_name VARCHAR(255),

    phone VARCHAR(30),
    email VARCHAR(255),

    occupation VARCHAR(255),

    address TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_parent_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id),

    CONSTRAINT fk_parent_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);

-- =====================================================
-- TEACHERS
-- =====================================================

CREATE TABLE teachers (
id BIGINT PRIMARY KEY AUTO_INCREMENT,

    tenant_id BIGINT NOT NULL,

    user_id BIGINT NOT NULL,

    employee_no VARCHAR(50),

    designation VARCHAR(100),

    joining_date DATE,

    qualification VARCHAR(255),

    salary DECIMAL(12,2),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_teacher_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id),

    CONSTRAINT fk_teacher_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);

-- =====================================================
-- CLASSROOMS
-- =====================================================

CREATE TABLE classrooms (
id BIGINT PRIMARY KEY AUTO_INCREMENT,

    tenant_id BIGINT NOT NULL,

    class_name VARCHAR(100) NOT NULL,
    section VARCHAR(50),

    academic_year VARCHAR(20),

    class_teacher_id BIGINT NULL,

    capacity INT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_classroom_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id),

    CONSTRAINT fk_classroom_teacher
        FOREIGN KEY (class_teacher_id)
        REFERENCES teachers(id)
);

-- =====================================================
-- STUDENTS
-- =====================================================

CREATE TABLE students (
id BIGINT PRIMARY KEY AUTO_INCREMENT,

    tenant_id BIGINT NOT NULL,

    user_id BIGINT NULL,

    classroom_id BIGINT NOT NULL,

    parent_id BIGINT NOT NULL,

    admission_no VARCHAR(100) NOT NULL,

    roll_no VARCHAR(50),

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),

    gender ENUM('MALE','FEMALE'),

    date_of_birth DATE,

    admission_date DATE,

    quran_level VARCHAR(100),
    hifz_status VARCHAR(100),

    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uk_student_admission (
        tenant_id,
        admission_no
    ),

    CONSTRAINT fk_student_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id),

    CONSTRAINT fk_student_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_student_parent
        FOREIGN KEY (parent_id)
        REFERENCES parents(id),

    CONSTRAINT fk_student_classroom
        FOREIGN KEY (classroom_id)
        REFERENCES classrooms(id)
);

-- =====================================================
-- ATTENDANCE
-- =====================================================

CREATE TABLE attendance (
id BIGINT PRIMARY KEY AUTO_INCREMENT,

    tenant_id BIGINT NOT NULL,

    student_id BIGINT NOT NULL,

    attendance_date DATE NOT NULL,

    status ENUM(
        'PRESENT',
        'ABSENT',
        'LEAVE',
        'LATE'
    ) NOT NULL,

    remarks VARCHAR(255),

    marked_by BIGINT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uk_attendance (
        student_id,
        attendance_date
    ),

    CONSTRAINT fk_attendance_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id),

    CONSTRAINT fk_attendance_student
        FOREIGN KEY (student_id)
        REFERENCES students(id),

    CONSTRAINT fk_attendance_marked_by
        FOREIGN KEY (marked_by)
        REFERENCES users(id)
);

-- =====================================================
-- INVOICES
-- =====================================================

CREATE TABLE invoices (
id BIGINT PRIMARY KEY AUTO_INCREMENT,

    tenant_id BIGINT NOT NULL,

    student_id BIGINT NOT NULL,

    invoice_no VARCHAR(100) NOT NULL,

    invoice_date DATE NOT NULL,

    due_date DATE NOT NULL,

    subtotal DECIMAL(12,2) DEFAULT 0,
    discount_amount DECIMAL(12,2) DEFAULT 0,
    tax_amount DECIMAL(12,2) DEFAULT 0,

    total_amount DECIMAL(12,2) NOT NULL,

    paid_amount DECIMAL(12,2) DEFAULT 0,

    balance_amount DECIMAL(12,2) NOT NULL,

    status ENUM(
        'DRAFT',
        'UNPAID',
        'PARTIAL',
        'PAID',
        'OVERDUE'
    ) DEFAULT 'UNPAID',

    notes TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uk_invoice_no (
        tenant_id,
        invoice_no
    ),

    CONSTRAINT fk_invoice_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id),

    CONSTRAINT fk_invoice_student
        FOREIGN KEY (student_id)
        REFERENCES students(id)
);

-- =====================================================
-- PAYMENTS
-- =====================================================

CREATE TABLE payments (
id BIGINT PRIMARY KEY AUTO_INCREMENT,

    tenant_id BIGINT NOT NULL,

    invoice_id BIGINT NOT NULL,

    student_id BIGINT NOT NULL,

    payment_date DATETIME NOT NULL,

    amount DECIMAL(12,2) NOT NULL,

    payment_method ENUM(
        'CASH',
        'BANK_TRANSFER',
        'UPI',
        'CARD',
        'ONLINE'
    ) NOT NULL,

    transaction_reference VARCHAR(255),

    remarks VARCHAR(255),

    received_by BIGINT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id),

    CONSTRAINT fk_payment_invoice
        FOREIGN KEY (invoice_id)
        REFERENCES invoices(id),

    CONSTRAINT fk_payment_student
        FOREIGN KEY (student_id)
        REFERENCES students(id),

    CONSTRAINT fk_payment_user
        FOREIGN KEY (received_by)
        REFERENCES users(id)
);

-- =====================================================
-- RECEIPTS
-- =====================================================

CREATE TABLE receipts (
id BIGINT PRIMARY KEY AUTO_INCREMENT,

    tenant_id BIGINT NOT NULL,

    payment_id BIGINT NOT NULL,

    receipt_no VARCHAR(100) NOT NULL,

    receipt_date DATETIME NOT NULL,

    receipt_url VARCHAR(500),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uk_receipt_no (
        tenant_id,
        receipt_no
    ),

    CONSTRAINT fk_receipt_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id),

    CONSTRAINT fk_receipt_payment
        FOREIGN KEY (payment_id)
        REFERENCES payments(id)
);

-- =====================================================
-- RECOMMENDED INDEXES
-- =====================================================

CREATE INDEX idx_users_tenant
ON users(tenant_id);

CREATE INDEX idx_students_tenant
ON students(tenant_id);

CREATE INDEX idx_attendance_date
ON attendance(attendance_date);

CREATE INDEX idx_invoice_status
ON invoices(status);

CREATE INDEX idx_payment_date
ON payments(payment_date);

CREATE INDEX idx_classroom_tenant
ON classrooms(tenant_id);