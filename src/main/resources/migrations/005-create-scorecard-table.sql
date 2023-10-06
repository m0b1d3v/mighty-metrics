CREATE TABLE IF NOT EXISTS scorecard (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	id_coach INTEGER,
	id_score_group INTEGER,
	id_score_personal INTEGER,
	localDateTime TEXT,
	workoutIntensity INTEGER,
	mighteriumCollected INTEGER,
	FOREIGN KEY (id_coach) REFERENCES coach (id),
	FOREIGN KEY (id_score_group) REFERENCES score (id),
	FOREIGN KEY (id_score_personal) REFERENCES score (id)
);
