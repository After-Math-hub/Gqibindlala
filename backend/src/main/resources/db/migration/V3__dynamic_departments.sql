CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    login_domain VARCHAR(255) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO departments (name, slug, login_domain) VALUES
    ('Chairs', 'chair', 'chair-gqi.co.za'),
    ('Tables', 'table', 'table-gqi.co.za'),
    ('Tents', 'tent', 'tent-gqi.co.za'),
    ('Toilets', 'toilets', 'toilets-gqi.co.za'),
    ('Mobile Fridge', 'mobile-fridge', 'mobile-fridge-gqi.co.za'),
    ('Meat Cutter', 'meat-cutter', 'meat-cutter-gqi.co.za'),
    ('Finance', 'finance', 'finance-gqi.co.za');

ALTER TABLE equipment ADD COLUMN department_id BIGINT;
UPDATE equipment e SET department_id = d.id
FROM departments d WHERE d.slug = CASE e.category
    WHEN 'CHAIR' THEN 'chair'
    WHEN 'TABLE' THEN 'table'
    WHEN 'TENT' THEN 'tent'
    WHEN 'TOILET' THEN 'toilets'
    WHEN 'MOBILE_FRIDGE' THEN 'mobile-fridge'
    WHEN 'MEAT_CUTTER' THEN 'meat-cutter'
END;
ALTER TABLE equipment ALTER COLUMN department_id SET NOT NULL;
ALTER TABLE equipment ADD CONSTRAINT fk_equipment_department
    FOREIGN KEY (department_id) REFERENCES departments(id);
ALTER TABLE equipment DROP COLUMN category;

ALTER TABLE app_users DROP CONSTRAINT chk_app_users_department_role;
ALTER TABLE app_users RENAME COLUMN email TO login_identifier;
ALTER TABLE app_users ADD COLUMN department_id BIGINT;
UPDATE app_users u SET department_id = d.id
FROM departments d WHERE d.slug = CASE u.department
    WHEN 'CHAIR' THEN 'chair'
    WHEN 'TABLE' THEN 'table'
    WHEN 'TENT' THEN 'tent'
    WHEN 'TOILET' THEN 'toilets'
    WHEN 'MOBILE_FRIDGE' THEN 'mobile-fridge'
    WHEN 'MEAT_CUTTER' THEN 'meat-cutter'
END;
UPDATE app_users u SET department_id = d.id
FROM departments d WHERE u.role = 'PAYMENTS' AND d.slug = 'finance';
ALTER TABLE app_users DROP COLUMN department;
ALTER TABLE app_users ADD CONSTRAINT fk_app_users_department
    FOREIGN KEY (department_id) REFERENCES departments(id);
ALTER TABLE app_users ADD CONSTRAINT chk_app_users_department_role CHECK (
    (role IN ('DEPARTMENT', 'PAYMENTS') AND department_id IS NOT NULL)
    OR (role NOT IN ('DEPARTMENT', 'PAYMENTS') AND department_id IS NULL)
);

CREATE INDEX idx_equipment_department ON equipment (department_id);
CREATE INDEX idx_app_users_department ON app_users (department_id);