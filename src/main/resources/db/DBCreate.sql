CREATE TABLE Nodes
(
  node_uuid VARCHAR(36) PRIMARY KEY NOT NULL,
  posX DOUBLE NOT NULL,
  posY DOUBLE NOT NULL,
  type INTEGER, --can be an enum in java?  //options are service, provider, hallway
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
  node VARCHAR(36) NOT NULL,
  name VARCHAR(128) NOT NULL,
  FOREIGN KEY (node) REFERENCES Nodes(node_uuid) ON DELETE CASCADE --services and nodes have 1-1 relationship
);

CREATE TABLE Providers
(
  provider_uuid VARCHAR(36) PRIMARY KEY NOT NULL,
  name VARCHAR(128) NOT NULL UNIQUE--If your name is bigger than 128 chars, you need a shorter name
);

CREATE TABLE DoctorOffices --Links between doctors and their offices.
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

--Delete all nodes in the building if the building gets deleted
ALTER TABLE Nodes ADD FOREIGN KEY (building) REFERENCES Buildings(building_uuid) ON DELETE CASCADE;
INSERT INTO Buildings VALUES('00000000-0000-0000-0000-000000000000', 'default');

CREATE TABLE AdminAccounts
(
  username VARCHAR(36) PRIMARY KEY NOT NULL,
  password VARCHAR(60) NOT NULL
);

INSERT INTO Nodes VALUES('00000000-0000-0000-0000-000000000090', 'Vascular Surgery  ', 'Practice',1032, 888 , 5);
INSERT INTO Nodes VALUES('00000000-0000-0000-0000-000000000091', 'Volunteer Services', 'Service',1000, 750 , 3);