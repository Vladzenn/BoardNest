USE boarding_house_db;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  full_name VARCHAR(100) NOT NULL,
  username VARCHAR(50) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_users_username UNIQUE (username)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS properties (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(120) NOT NULL,
  address VARCHAR(255) NULL,
  owner_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  KEY idx_properties_owner (owner_id),
  CONSTRAINT fk_properties_owner FOREIGN KEY (owner_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS floors (
  id BIGINT NOT NULL AUTO_INCREMENT,
  floor_number INT NOT NULL,
  property_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_floor_property_number UNIQUE (property_id, floor_number),
  CONSTRAINT fk_floors_property FOREIGN KEY (property_id) REFERENCES properties(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS rooms (
  id BIGINT NOT NULL AUTO_INCREMENT,
  room_number VARCHAR(30) NOT NULL,
  capacity INT NOT NULL,
  floor_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_room_floor_number UNIQUE (floor_id, room_number),
  CONSTRAINT fk_rooms_floor FOREIGN KEY (floor_id) REFERENCES floors(id),
  CONSTRAINT chk_rooms_capacity CHECK (capacity >= 1)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tenants (
  id BIGINT NOT NULL AUTO_INCREMENT,
  full_name VARCHAR(120) NOT NULL,
  phone VARCHAR(30) NULL,
  email VARCHAR(120) NULL,
  property_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  KEY idx_tenants_property (property_id),
  CONSTRAINT fk_tenants_property FOREIGN KEY (property_id) REFERENCES properties(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS occupancies (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  room_id BIGINT NOT NULL,
  assigned_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  CONSTRAINT uk_occupancy_tenant UNIQUE (tenant_id),
  KEY idx_occupancies_room (room_id),
  CONSTRAINT fk_occupancies_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
  CONSTRAINT fk_occupancies_room FOREIGN KEY (room_id) REFERENCES rooms(id)
) ENGINE=InnoDB;
