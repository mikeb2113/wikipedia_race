-- sequences
CREATE SEQUENCE IF NOT EXISTS seq_players START 1;
CREATE SEQUENCE IF NOT EXISTS seq_articles START 1;
CREATE SEQUENCE IF NOT EXISTS seq_games START 1;
CREATE SEQUENCE IF NOT EXISTS seq_moves START 1;
CREATE SEQUENCE IF NOT EXISTS seq_audit START 1;

-- players
CREATE TABLE IF NOT EXISTS players (
    player_id   BIGINT PRIMARY KEY DEFAULT nextval('seq_players'),
    username    VARCHAR(64) NOT NULL UNIQUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    pass_hash   VARCHAR(255)
);

-- articles (cached)
CREATE TABLE IF NOT EXISTS articles (
    article_id      BIGINT PRIMARY KEY DEFAULT nextval('seq_articles'),
    title           VARCHAR NOT NULL,
    canonical_title VARCHAR NOT NULL UNIQUE,
    last_updated    TIMESTAMP
);

-- games
CREATE TABLE IF NOT EXISTS games (
    game_id         BIGINT PRIMARY KEY DEFAULT nextval('seq_games'),
    start_time      TIMESTAMP NOT NULL,
    end_time        TIMESTAMP,
    start_article_id BIGINT NOT NULL REFERENCES articles(article_id),
    "state"           VARCHAR NOT NULL DEFAULT 'PENDING',
    created_by      BIGINT REFERENCES players(player_id),
    target_article_id BIGINT REFERENCES articles(article_id),
    winner_id       BIGINT REFERENCES players(player_id)
);

-- moves
CREATE TABLE IF NOT EXISTS moves (
    move_id        BIGINT PRIMARY KEY DEFAULT nextval('seq_moves'),
    "timestamp"      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    move_seq       INTEGER NOT NULL,
    move_status    VARCHAR NOT NULL,
    from_article_id BIGINT NOT NULL REFERENCES articles(article_id),
    to_article_id   BIGINT NOT NULL REFERENCES articles(article_id),
    game_id        BIGINT NOT NULL REFERENCES games(game_id),
    player_id      BIGINT NOT NULL REFERENCES players(player_id)
);

-- plays (junction between players and games)
CREATE TABLE IF NOT EXISTS plays (
    player_id    BIGINT NOT NULL REFERENCES players(player_id),
    game_id      BIGINT NOT NULL REFERENCES games(game_id),
    steps_taken  INTEGER NOT NULL DEFAULT 0,
    joined_at    TIMESTAMP NOT NULL,
    left_at      TIMESTAMP,
    path_length  INTEGER DEFAULT 0,
    penalties    INTEGER DEFAULT 0,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (player_id, game_id)
);

-- links between articles
CREATE TABLE IF NOT EXISTS links (
    from_article_id BIGINT NOT NULL REFERENCES articles(article_id),
    to_article_id   BIGINT NOT NULL REFERENCES articles(article_id),
    PRIMARY KEY (from_article_id, to_article_id)
);

-- visited articles per game
CREATE TABLE IF NOT EXISTS visited_articles (
    game_id            BIGINT NOT NULL REFERENCES games(game_id),
    article_id         BIGINT NOT NULL REFERENCES articles(article_id),
    first_seen_ts      TIMESTAMP,
    first_seen_by_move BIGINT REFERENCES moves(move_id),
    PRIMARY KEY (game_id, article_id)
);

-- audit log
CREATE TABLE IF NOT EXISTS audit_log (
    id        BIGINT PRIMARY KEY DEFAULT nextval('seq_audit'),
    "at"        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    player_id BIGINT REFERENCES players(player_id),
    "action"    VARCHAR NOT NULL,
    "target"    VARCHAR,
    details   VARCHAR
);

-- redirects
CREATE TABLE IF NOT EXISTS redirects (
    from_title   VARCHAR PRIMARY KEY NOT NULL,
    to_article_id BIGINT NOT NULL REFERENCES articles(article_id)
);

-- sessions
CREATE TABLE IF NOT EXISTS sessions (
    session_id   VARCHAR PRIMARY KEY,
    player_id    BIGINT NOT NULL REFERENCES players(player_id),
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at   TIMESTAMP NOT NULL,
    last_seen_at TIMESTAMP NOT NULL
);

-- rate limit
CREATE TABLE IF NOT EXISTS rate_limit_log (
    player_id     BIGINT NOT NULL REFERENCES players(player_id),
    window_start  TIMESTAMP NOT NULL,
    "event"         VARCHAR NOT NULL,
    window_seconds INTEGER NOT NULL,
    count         INTEGER NOT NULL,
    PRIMARY KEY (player_id, window_start, event)
);