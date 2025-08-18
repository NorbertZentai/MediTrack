-- Flyway migration: initial schema
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    gender VARCHAR(10),
    date_of_birth VARCHAR(255),
    address TEXT,
    phone_number VARCHAR(200),
    registration_date TIMESTAMP,
    last_login TIMESTAMP,
    profile_picture BYTEA,
    role VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    language VARCHAR(10) DEFAULT 'hu',
    deleted_at TIMESTAMP,
    CONSTRAINT unique_user_name_email UNIQUE (name, email)
);

CREATE TABLE IF NOT EXISTS profiles (
   id SERIAL PRIMARY KEY,
   user_id INTEGER NOT NULL REFERENCES users(id),
   name VARCHAR(100) NOT NULL,
   notes TEXT,
   CONSTRAINT unique_profile_per_user UNIQUE (user_id, name)
);

CREATE TABLE IF NOT EXISTS medications (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    image_url TEXT,
    registration_number VARCHAR(100),
    substance VARCHAR(255),
    atc_code VARCHAR(100),
    company VARCHAR(255),
    legal_basis VARCHAR(100),
    status VARCHAR(100),
    authorization_date DATE,
    narcotic VARCHAR(100),
    patient_info_url TEXT,
    smpc_url TEXT,
    label_url TEXT,
    contains_lactose BOOLEAN,
    packaging VARCHAR(100),
    release_date DATE,
    description TEXT,
    manufacturer VARCHAR(200),
    contains_gluten BOOLEAN,
    contains_benzoate BOOLEAN,
    packages_json TEXT,
    substitutes_json TEXT,
    final_samples_json TEXT,
    defective_forms_json TEXT,
    hazipatika_json TEXT,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS profile_medications (
    id SERIAL PRIMARY KEY,
    profile_id INTEGER NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    medication_id INTEGER NOT NULL REFERENCES medications(id),
    notes TEXT,
    reminders TEXT,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_profile_medication UNIQUE (profile_id, medication_id)
);

CREATE TABLE IF NOT EXISTS favorites (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    medication_id INTEGER NOT NULL REFERENCES medications(id),
    CONSTRAINT unique_favorite_per_user UNIQUE (user_id, medication_id)
);

CREATE TABLE IF NOT EXISTS reviews (
    id SERIAL PRIMARY KEY,
    item_id INTEGER NOT NULL REFERENCES medications(id),
    user_id INTEGER NOT NULL REFERENCES users(id),
    rating INTEGER NOT NULL,
    positive TEXT,
    negative TEXT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT unique_user_review UNIQUE (user_id, item_id)
);

CREATE TABLE IF NOT EXISTS medication_intake_log (
    id SERIAL PRIMARY KEY,
    profile_medication_id INTEGER NOT NULL REFERENCES profile_medications(id) ON DELETE CASCADE,
    intake_date DATE NOT NULL,
    intake_time TIME NOT NULL,
    taken BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (profile_medication_id, intake_date, intake_time)
);

CREATE TABLE IF NOT EXISTS push_subscriptions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    endpoint TEXT NOT NULL,
    p256dh VARCHAR(255),
    auth VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
