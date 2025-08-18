-- Seed initial users (idempotent logic handled by constraints)
INSERT INTO users (name, email, password, role, is_active, registration_date)
VALUES ('Admin', 'zentai.norbert96@gmail.com', 'adminpass', 'ADMIN', true, now())
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (name, email, password, role, is_active, registration_date)
VALUES ('test', '96nucu@gmail.com', '$2a$10$YNBGD.VXFtiLfljxbETaz.OJQ4uIcKGYBJDTa/qOYNla./EJx6SfG', 'USER', true, now())
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (id, name, email, password, role, is_active, registration_date)
VALUES (9999, 'tesztfelhasználó', 'teszt@example.com', 'hashed_password', 'USER', true, now())
ON CONFLICT (email) DO NOTHING;
