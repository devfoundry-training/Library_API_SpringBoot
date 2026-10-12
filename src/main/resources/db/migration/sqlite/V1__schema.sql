-- The Library API schema for SQLite (the same tables and rules as the contract's db/schema.sql).
-- INTEGER PRIMARY KEY makes id an alias for the row id, so a new row gets the highest id plus 1.
-- Dates are stored as YYYY-MM-DD text.

CREATE TABLE books (
    id      INTEGER      NOT NULL PRIMARY KEY,
    title   VARCHAR(100) NOT NULL,
    author  VARCHAR(100) NOT NULL,
    year    INTEGER      NOT NULL CHECK (year BETWEEN 1450 AND 2100),
    genre   VARCHAR(50)  NOT NULL,
    copies  INTEGER      NOT NULL CHECK (copies >= 0),
    format  VARCHAR(10)  NOT NULL CHECK (format IN ('print', 'ebook', 'audio')),
    length  INTEGER      NOT NULL CHECK (length > 0)
);

CREATE TABLE members (
    id      INTEGER      NOT NULL PRIMARY KEY,
    name    VARCHAR(100) NOT NULL,
    email   VARCHAR(100) NOT NULL UNIQUE,
    type    VARCHAR(10)  NOT NULL CHECK (type IN ('standard', 'student', 'staff')),
    joined  DATE         NOT NULL
);

CREATE TABLE loans (
    id         INTEGER NOT NULL PRIMARY KEY,
    book_id    INTEGER NOT NULL,
    member_id  INTEGER NOT NULL,
    borrowed   DATE    NOT NULL,
    returned   DATE    NULL,
    FOREIGN KEY (book_id) REFERENCES books (id),
    FOREIGN KEY (member_id) REFERENCES members (id),
    CHECK (returned IS NULL OR returned >= borrowed)
);

CREATE INDEX ix_loans_book_id ON loans (book_id);
CREATE INDEX ix_loans_member_id ON loans (member_id);
