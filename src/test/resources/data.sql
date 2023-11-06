INSERT INTO books (name, number_of_pages, publication_year) VALUES ('first book', 45, 2000);
INSERT INTO books (name, number_of_pages, publication_year) VALUES ('second book', 351, 1994);
INSERT INTO books (name, number_of_pages, publication_year) VALUES ('third book', 132, 1817);
INSERT INTO books (name, number_of_pages, publication_year) VALUES ('forth book', 218, 1921);

INSERT INTO authors (birth_date, name, surname) VALUES ('1973-11-06', 'Moisha', 'Mil-man');
INSERT INTO authors (birth_date, name, surname) VALUES ('1963-09-18', 'Lwee', 'Shtosman');
INSERT INTO authors (birth_date, name, surname) VALUES ('1971-07-22', 'Gary', 'Rivshic');
INSERT INTO authors (birth_date, name, surname) VALUES ('1836-02-28', 'Sarah', 'Abramovich');
INSERT INTO authors (birth_date, name, surname) VALUES ('1902-04-28', 'David', 'Sharp');

INSERT INTO genres (name) VALUES ('fiction');
INSERT INTO genres (name) VALUES ('futurism');
INSERT INTO genres (name) VALUES ('modern');
INSERT INTO genres (name) VALUES ('sport');

INSERT INTO book_authors (book_id, author_id) VALUES (1, 1);
INSERT INTO book_authors (book_id, author_id) VALUES (1, 2);
INSERT INTO book_authors (book_id, author_id) VALUES (2, 2);
INSERT INTO book_authors (book_id, author_id) VALUES (2, 3);
INSERT INTO book_authors (book_id, author_id) VALUES (3, 5);
INSERT INTO book_authors (book_id, author_id) VALUES (4, 4);

INSERT INTO book_genres (book_id, genre_name) VALUES (1, 'fiction');
INSERT INTO book_genres (book_id, genre_name) VALUES (1, 'futurism');
INSERT INTO book_genres (book_id, genre_name) VALUES (2, 'futurism');
INSERT INTO book_genres (book_id, genre_name) VALUES (2, 'modern');
INSERT INTO book_genres (book_id, genre_name) VALUES (3, 'sport');
INSERT INTO book_genres (book_id, genre_name) VALUES (4, 'futurism');
INSERT INTO book_genres (book_id, genre_name) VALUES (4, 'sport');

INSERT INTO users (username, password, role, is_active) VALUES ('test', '$2a$08$yrIfAY9sKWCkMv2TbREFbeNmxtdVYIVoc.pE9A9N97Ve4JQwa6Jta', 'USER', TRUE);
