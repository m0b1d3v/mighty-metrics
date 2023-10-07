CREATE TABLE IF NOT EXISTS exercise (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	id_scorecard INTEGER,
	id_score INTEGER,
	uuid TEXT,
	value TEXT,
	FOREIGN KEY (id_scorecard) REFERENCES scorecard (id) ON DELETE CASCADE,
	FOREIGN KEY (id_score) REFERENCES score (id)
);

CREATE UNIQUE INDEX index_uuid ON exercise (uuid);
