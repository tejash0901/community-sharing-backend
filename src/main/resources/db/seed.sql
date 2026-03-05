INSERT INTO communities (name, address, city, invite_code, created_at)
VALUES ('Green Heights', '12 Residency Road', 'Bengaluru', 'GREEN123', NOW())
ON CONFLICT (invite_code) DO NOTHING;