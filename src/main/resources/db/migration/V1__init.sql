CREATE TABLE artists (
                         id BIGSERIAL PRIMARY KEY,
                         name VARCHAR(255) NOT NULL
);

CREATE TABLE albums (
                        id BIGSERIAL PRIMARY KEY,
                        title VARCHAR(255) NOT NULL,
                        cover_object_key VARCHAR(500)
);

CREATE TABLE artist_album (
                              artist_id BIGINT NOT NULL,
                              album_id BIGINT NOT NULL,
                              PRIMARY KEY (artist_id, album_id),
                              CONSTRAINT fk_artist FOREIGN KEY (artist_id) REFERENCES artists(id),
                              CONSTRAINT fk_album FOREIGN KEY (album_id) REFERENCES albums(id)
);