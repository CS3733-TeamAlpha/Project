CREATE TABLE Nodes
(
  node_uuid VARCHAR(36) PRIMARY KEY NOT NULL,
  posX DOUBLE NOT NULL,
  posY DOUBLE NOT NULL,
  type INTEGER, --can be an enum in java?
  -- 0-hallway, 1-doctor, 2-elevator, 3-restroom, 4-kiosk, 5-selectedkiosk, 6/7-entrance
  floor INTEGER NOT NULL,
  building VARCHAR(36) DEFAULT '00000000-0000-0000-0000-000000000000',
  name VARCHAR(128)
);

CREATE TABLE Edges
(
  src VARCHAR(36) NOT NULL,
  dst VARCHAR(36) NOT NULL,
  FOREIGN KEY (src) REFERENCES Nodes(node_uuid) ON DELETE CASCADE,
  CONSTRAINT UNIQ_EDGE UNIQUE(src,dst)
);

CREATE TABLE Services --Shops, cafes, etc. What is this, a mall?
(
  node VARCHAR(36),
  name VARCHAR(128) NOT NULL UNIQUE ,
  FOREIGN KEY (node) REFERENCES Nodes(node_uuid)
);

CREATE TABLE Providers
(
  provider_uuid VARCHAR(36) PRIMARY KEY NOT NULL,
  firstName VARCHAR(128) NOT NULL,
  lastName VARCHAR(128) NOT NULL,
  title VARCHAR(60),
  CONSTRAINT UNIQ_NAME UNIQUE(firstName, lastName) --two people can have the same name? no, no they can't, that would be silly.
);

CREATE TABLE ProviderOffices --Links between doctors and their offices.
(
  provider_uuid VARCHAR(36),
  node_uuid VARCHAR(36),
  FOREIGN KEY (node_uuid) REFERENCES Nodes(node_uuid) ON DELETE CASCADE,              --Delete this doctor's office if the office node is deleted
  FOREIGN KEY (provider_uuid) REFERENCES Providers(provider_uuid) ON DELETE CASCADE,  --Delete dangling provider connections if the provider is deleted
  CONSTRAINT UNIQ_OFFICE UNIQUE(provider_uuid, node_uuid)
);

CREATE TABLE Buildings
(
  building_uuid VARCHAR(36) PRIMARY KEY NOT NULL,
  name VARCHAR(128)
);

CREATE TABLE Logins
(
  username VARCHAR(64) UNIQUE NOT NULL,
  password VARCHAR(64) NOT NULL
);

--Note to future ninja coders who dare touch this file - do NOT add more buildings here! The default building for nodes
--is set to the UUID of faulkner_main because it's our main building, and nodes must have a building. If you add
--any further buildings here, I *will* find out when Travis starts screaming at us for breaking the build (again)!
INSERT INTO Buildings VALUES('00000000-0000-0000-0000-000000000000', 'faulkner_main');

--Delete all nodes in the building if the building gets deleted
ALTER TABLE Nodes ADD FOREIGN KEY (building) REFERENCES Buildings(building_uuid) ON DELETE CASCADE;

--Default login 'admin:admin'
INSERT INTO LOGINS VALUES('admin', '$2a$10$TzLC8ubvjWCaG/zc53OwIOhxwP1BicrifkL7Do80Dgpu1kT9iNJHG');


