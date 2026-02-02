-- ARTISTS

INSERT INTO artists (name, type)
SELECT 'Serj Tankian', 'CANTOR'
    WHERE NOT EXISTS (SELECT 1 FROM artists WHERE name = 'Serj Tankian');

INSERT INTO artists (name, type)
SELECT 'Mike Shinoda', 'CANTOR'
    WHERE NOT EXISTS (SELECT 1 FROM artists WHERE name = 'Mike Shinoda');

INSERT INTO artists (name, type)
SELECT 'Michel Teló', 'CANTOR'
    WHERE NOT EXISTS (SELECT 1 FROM artists WHERE name = 'Michel Teló');

INSERT INTO artists (name, type)
SELECT 'Guns N'' Roses', 'BANDA'
    WHERE NOT EXISTS (SELECT 1 FROM artists WHERE name = 'Guns N'' Roses');

-- ALBUMS

-- Serj Tankian
INSERT INTO albums (title)
SELECT 'Harakiri'
    WHERE NOT EXISTS (SELECT 1 FROM albums WHERE title = 'Harakiri');

INSERT INTO albums (title)
SELECT 'Black Blooms'
    WHERE NOT EXISTS (SELECT 1 FROM albums WHERE title = 'Black Blooms');

INSERT INTO albums (title)
SELECT 'The Rough Dog'
    WHERE NOT EXISTS (SELECT 1 FROM albums WHERE title = 'The Rough Dog');


-- Mike Shinoda
INSERT INTO albums (title)
SELECT 'The Rising Tied'
    WHERE NOT EXISTS (SELECT 1 FROM albums WHERE title = 'The Rising Tied');

INSERT INTO albums (title)
SELECT 'Post Traumatic'
    WHERE NOT EXISTS (SELECT 1 FROM albums WHERE title = 'Post Traumatic');

INSERT INTO albums (title)
SELECT 'Post Traumatic EP'
    WHERE NOT EXISTS (SELECT 1 FROM albums WHERE title = 'Post Traumatic EP');

INSERT INTO albums (title)
SELECT 'Where''d You Go'
    WHERE NOT EXISTS (SELECT 1 FROM albums WHERE title = 'Where''d You Go');


-- Michel Teló
INSERT INTO albums (title)
SELECT 'Bem Sertanejo'
    WHERE NOT EXISTS (SELECT 1 FROM albums WHERE title = 'Bem Sertanejo');

INSERT INTO albums (title)
SELECT 'Bem Sertanejo - O Show (Ao Vivo)'
    WHERE NOT EXISTS (SELECT 1 FROM albums WHERE title = 'Bem Sertanejo - O Show (Ao Vivo)');

INSERT INTO albums (title)
SELECT 'Bem Sertanejo - (1ª Temporada) - EP'
    WHERE NOT EXISTS (SELECT 1 FROM albums WHERE title = 'Bem Sertanejo - (1ª Temporada) - EP');


-- Guns N' Roses
INSERT INTO albums (title)
SELECT 'Use Your Illusion I'
    WHERE NOT EXISTS (SELECT 1 FROM albums WHERE title = 'Use Your Illusion I');

INSERT INTO albums (title)
SELECT 'Use Your Illusion II'
    WHERE NOT EXISTS (SELECT 1 FROM albums WHERE title = 'Use Your Illusion II');

INSERT INTO albums (title)
SELECT 'Greatest Hits'
    WHERE NOT EXISTS (SELECT 1 FROM albums WHERE title = 'Greatest Hits');


-- (artist_album)
-- Helper: vincula pelo nome do artista e título do álbum (não depende de IDs fixos)

-- Serj Tankian -> Harakiri, Black Blooms, The Rough Dog
INSERT INTO artist_album (artist_id, album_id)
SELECT a.id, al.id
FROM artists a
         JOIN albums al ON al.title IN ('Harakiri', 'Black Blooms', 'The Rough Dog')
WHERE a.name = 'Serj Tankian'
  AND NOT EXISTS (
    SELECT 1 FROM artist_album aa WHERE aa.artist_id = a.id AND aa.album_id = al.id
);

-- Mike Shinoda -> The Rising Tied, Post Traumatic, Post Traumatic EP, Where'd You Go
INSERT INTO artist_album (artist_id, album_id)
SELECT a.id, al.id
FROM artists a
         JOIN albums al ON al.title IN ('The Rising Tied', 'Post Traumatic', 'Post Traumatic EP', 'Where''d You Go')
WHERE a.name = 'Mike Shinoda'
  AND NOT EXISTS (
    SELECT 1 FROM artist_album aa WHERE aa.artist_id = a.id AND aa.album_id = al.id
);

-- Michel Teló -> Bem Sertanejo, Bem Sertanejo - O Show (Ao Vivo), Bem Sertanejo - (1ª Temporada) - EP
INSERT INTO artist_album (artist_id, album_id)
SELECT a.id, al.id
FROM artists a
         JOIN albums al ON al.title IN (
                                        'Bem Sertanejo',
                                        'Bem Sertanejo - O Show (Ao Vivo)',
                                        'Bem Sertanejo - (1ª Temporada) - EP'
    )
WHERE a.name = 'Michel Teló'
  AND NOT EXISTS (
    SELECT 1 FROM artist_album aa WHERE aa.artist_id = a.id AND aa.album_id = al.id
);

-- Guns N' Roses -> Use Your Illusion I, Use Your Illusion II, Greatest Hits
INSERT INTO artist_album (artist_id, album_id)
SELECT a.id, al.id
FROM artists a
         JOIN albums al ON al.title IN ('Use Your Illusion I', 'Use Your Illusion II', 'Greatest Hits')
WHERE a.name = 'Guns N'' Roses'
  AND NOT EXISTS (
    SELECT 1 FROM artist_album aa WHERE aa.artist_id = a.id AND aa.album_id = al.id
);