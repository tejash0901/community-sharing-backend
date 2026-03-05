CREATE TABLE IF NOT EXISTS communities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(120) NOT NULL,
    invite_code VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_community_name_address UNIQUE (name, address)
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(30) UNIQUE,
    community_id BIGINT NOT NULL,
    block VARCHAR(50),
    floor VARCHAR(50),
    flat_number VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_user_community FOREIGN KEY (community_id) REFERENCES communities (id)
);

CREATE TABLE IF NOT EXISTS tools (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    condition VARCHAR(30),
    estimated_price NUMERIC(12,2),
    owner_id BIGINT NOT NULL,
    community_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_tool_owner FOREIGN KEY (owner_id) REFERENCES users (id),
    CONSTRAINT fk_tool_community FOREIGN KEY (community_id) REFERENCES communities (id)
);
CREATE INDEX IF NOT EXISTS idx_tools_owner_id ON tools(owner_id);
CREATE INDEX IF NOT EXISTS idx_tools_community_id ON tools(community_id);

CREATE TABLE IF NOT EXISTS availability_slots (
    id BIGSERIAL PRIMARY KEY,
    tool_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_slot_tool FOREIGN KEY (tool_id) REFERENCES tools (id),
    CONSTRAINT ck_slot_time_valid CHECK (start_time < end_time)
);
CREATE INDEX IF NOT EXISTS idx_availability_tool_id ON availability_slots(tool_id);

CREATE TABLE IF NOT EXISTS booking_requests (
    id BIGSERIAL PRIMARY KEY,
    tool_id BIGINT NOT NULL,
    borrower_id BIGINT NOT NULL,
    slot_id BIGINT NOT NULL,
    requested_start_time TIMESTAMP NOT NULL,
    requested_end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_booking_tool FOREIGN KEY (tool_id) REFERENCES tools (id),
    CONSTRAINT fk_booking_borrower FOREIGN KEY (borrower_id) REFERENCES users (id),
    CONSTRAINT fk_booking_slot FOREIGN KEY (slot_id) REFERENCES availability_slots (id),
    CONSTRAINT ck_booking_time_valid CHECK (requested_start_time < requested_end_time)
);
CREATE INDEX IF NOT EXISTS idx_booking_borrower_id ON booking_requests(borrower_id);
CREATE INDEX IF NOT EXISTS idx_booking_tool_id ON booking_requests(tool_id);
CREATE INDEX IF NOT EXISTS idx_booking_slot_id ON booking_requests(slot_id);
CREATE INDEX IF NOT EXISTS idx_booking_requested_time ON booking_requests(requested_start_time, requested_end_time);
