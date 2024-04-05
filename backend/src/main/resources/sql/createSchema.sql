CREATE TABLE IF NOT EXISTS breed
(
  id BIGINT PRIMARY KEY,
  name VARCHAR(32) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS horse
(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  -- Instead of an ENUM (H2 specific) this could also be done with a character string type and a check constraint.
  sex ENUM ('MALE', 'FEMALE') NOT NULL,
  date_of_birth DATE NOT NULL,
  height NUMERIC(4,2),
  weight NUMERIC(7,2),
  // TODO handle optional everywhere
  breed_id BIGINT REFERENCES breed(id)
);

CREATE TABLE IF NOT EXISTS tournament
( 
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS tournament_participant
(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tournament_id BIGINT REFERENCES tournament(id) NOT NULL,
  horse_id BIGINT REFERENCES horse(id) NOT NULL,
  entry_number INT NOT NULL,
  round_reached INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS tournament_tree (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tournament_id BIGINT REFERENCES tournament(id) NOT NULL,
  participant_id BIGINT REFERENCES tournament_participant(id) NULL,
  parent_id BIGINT REFERENCES tournament_tree(id) ON DELETE SET NULL,
  branch_position ENUM ('FINAL_WINNER', 'UPPER', 'LOWER') NOT NULL
);
