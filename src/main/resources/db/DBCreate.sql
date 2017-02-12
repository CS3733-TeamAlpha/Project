CREATE TABLE Nodes
(
  node_uuid VARCHAR(36) PRIMARY KEY NOT NULL,
  posX DOUBLE NOT NULL,
  posY DOUBLE NOT NULL,
  type INTEGER,
  floor INTEGER NOT NULL,
  building VARCHAR(128) NOT NULL,
  name VARCHAR(128)
);

CREATE TABLE Edges --todo: investigate the array type...
(
  src VARCHAR(36) NOT NULL,
  dst VARCHAR(36) NOT NULL,
  FOREIGN KEY (src) REFERENCES Nodes(node_uuid) ON DELETE CASCADE
);

CREATE TABLE Services
(
  node VARCHAR(36) NOT NULL,
  name VARCHAR(128) NOT NULL,
  FOREIGN KEY (node) REFERENCES Nodes(node_uuid) ON DELETE CASCADE
);
CREATE TABLE Providers
(
  node VARCHAR(36) NOT NULL,
  name VARCHAR(128) NOT NULL,
  FOREIGN KEY (node) REFERENCES Nodes (node_uuid) ON DELETE CASCADE
);

CREATE TABLE Offices
(
  node VARCHAR(36) NOT NULL,
  name VARCHAR(256) NOT NULL, --title information should go in here!
  FOREIGN KEY (node) REFERENCES Nodes(node_uuid) ON DELETE CASCADE
);
