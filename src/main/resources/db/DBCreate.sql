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
INSERT INTO Nodes VALUES('00000000-0000-0000-0000-000000000000', 1135, 1152, 1, 3, '00000000-0000-0000-0000-000000000000', 'Kiosk');
INSERT INTO LOGINS VALUES('admin', '$2a$10$TzLC8ubvjWCaG/zc53OwIOhxwP1BicrifkL7Do80Dgpu1kT9iNJHG');

INSERT INTO Nodes VALUES('D1A75ABF-CE5A-45AC-A25C-78034BFB171B', 1107, 326, 2, 1, '00000000-0000-0000-0000-000000000000', 'Atrium Elevator 1');
INSERT INTO Nodes VALUES('D1A75ABF-CE5A-45AC-A25C-78034BFB172B', 1107, 326, 2, 2, '00000000-0000-0000-0000-000000000000', 'Atrium Elevator 2');
INSERT INTO Nodes VALUES('D1A75ABF-CE5A-45AC-A25C-78034BFB173B', 1107, 326, 2, 3, '00000000-0000-0000-0000-000000000000', 'Atrium Elevator 3');
INSERT INTO Nodes VALUES('D1A75ABF-CE5A-45AC-A25C-78034BFB174B', 1107, 326, 2, 4, '00000000-0000-0000-0000-000000000000', 'Atrium Elevator 4');
INSERT INTO Nodes VALUES('D1A75ABF-CE5A-45AC-A25C-78034BFB175B', 1107, 326, 2, 5, '00000000-0000-0000-0000-000000000000', 'Atrium Elevator 5');
INSERT INTO Nodes VALUES('D1A75ABF-CE5A-45AC-A25C-78034BFB176B', 1107, 326, 2, 6, '00000000-0000-0000-0000-000000000000', 'Atrium Elevator 6');
INSERT INTO Nodes VALUES('D1A75ABF-CE5A-45AC-A25C-78034BFB177B', 1107, 326, 2, 7, '00000000-0000-0000-0000-000000000000', 'Atrium Elevator 7');

INSERT INTO Nodes VALUES('D1A75ABF-CE5A-45AC-A25C-78034BFB178A', 1078, 1075, 2, 1, '00000000-0000-0000-0000-000000000000', 'Hillside Elevator 1');
INSERT INTO Nodes VALUES('D1A75ABF-CE5A-45AC-A25C-78034BFB1781', 1078, 1075, 2, 2, '00000000-0000-0000-0000-000000000000', 'Hillside Elevator 2');
INSERT INTO Nodes VALUES('D1A75ABF-CE5A-45AC-A25C-78034BFB1782', 1078, 1075, 2, 3, '00000000-0000-0000-0000-000000000000', 'Hillside Elevator 3');
INSERT INTO Nodes VALUES('D1A75ABF-CE5A-45AC-A25C-78034BFB1783', 1078, 1075, 2, 4, '00000000-0000-0000-0000-000000000000', 'Hillside Elevator 4');
INSERT INTO Nodes VALUES('D1A75ABF-CE5A-45AC-A25C-78034BFB1784', 1078, 1075, 2, 5, '00000000-0000-0000-0000-000000000000', 'Hillside Elevator 5');
INSERT INTO Nodes VALUES('D1A75ABF-CE5A-45AC-A25C-78034BFB1785', 1078, 1075, 2, 6, '00000000-0000-0000-0000-000000000000', 'Hillside Elevator 6');
INSERT INTO Nodes VALUES('D1A75ABF-CE5A-45AC-A25C-78034BFB1786', 1078, 1075, 2, 7, '00000000-0000-0000-0000-000000000000', 'Hillside Elevator 7');

INSERT INTO Edges VALUES ('D1A75ABF-CE5A-45AC-A25C-78034BFB178A', 'D1A75ABF-CE5A-45AC-A25C-78034BFB1781');
INSERT INTO Edges VALUES ('D1A75ABF-CE5A-45AC-A25C-78034BFB1781', 'D1A75ABF-CE5A-45AC-A25C-78034BFB1782');
INSERT INTO Edges VALUES ('D1A75ABF-CE5A-45AC-A25C-78034BFB1782', 'D1A75ABF-CE5A-45AC-A25C-78034BFB1783');
INSERT INTO Edges VALUES ('D1A75ABF-CE5A-45AC-A25C-78034BFB1783', 'D1A75ABF-CE5A-45AC-A25C-78034BFB1784');
INSERT INTO Edges VALUES ('D1A75ABF-CE5A-45AC-A25C-78034BFB1784', 'D1A75ABF-CE5A-45AC-A25C-78034BFB1785');
INSERT INTO Edges VALUES ('D1A75ABF-CE5A-45AC-A25C-78034BFB1785', 'D1A75ABF-CE5A-45AC-A25C-78034BFB1786');
INSERT INTO Edges VALUES ('D1A75ABF-CE5A-45AC-A25C-78034BFB1786', 'D1A75ABF-CE5A-45AC-A25C-78034BFB1787');

INSERT INTO Edges VALUES ('D1A75ABF-CE5A-45AC-A25C-78034BFB171B', 'D1A75ABF-CE5A-45AC-A25C-78034BFB172B');
INSERT INTO Edges VALUES ('D1A75ABF-CE5A-45AC-A25C-78034BFB172B', 'D1A75ABF-CE5A-45AC-A25C-78034BFB173B');
INSERT INTO Edges VALUES ('D1A75ABF-CE5A-45AC-A25C-78034BFB173B', 'D1A75ABF-CE5A-45AC-A25C-78034BFB174B');
INSERT INTO Edges VALUES ('D1A75ABF-CE5A-45AC-A25C-78034BFB174B', 'D1A75ABF-CE5A-45AC-A25C-78034BFB175B');
INSERT INTO Edges VALUES ('D1A75ABF-CE5A-45AC-A25C-78034BFB175B', 'D1A75ABF-CE5A-45AC-A25C-78034BFB176B');
INSERT INTO Edges VALUES ('D1A75ABF-CE5A-45AC-A25C-78034BFB176B', 'D1A75ABF-CE5A-45AC-A25C-78034BFB177B');