CREATE TABLE IF NOT EXISTS scorecard (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	id_member INTEGER,
	id_coach INTEGER,
	id_score_group INTEGER,
	id_score_personal INTEGER,
	uuid TEXT,
	local_date_time TEXT,
	workout_intensity INTEGER,
	mighterium_collected INTEGER,
	FOREIGN KEY (id_member) REFERENCES member (id),
	FOREIGN KEY (id_coach) REFERENCES coach (id),
	FOREIGN KEY (id_score_group) REFERENCES score (id),
	FOREIGN KEY (id_score_personal) REFERENCES score (id)
);
