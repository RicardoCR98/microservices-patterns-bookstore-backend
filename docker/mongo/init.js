// Crear base de datos booksdb y colección books
db = db.getSiblingDB('booksdb');
db.createCollection('books');

// Crear base de datos usersdb y colección users
db = db.getSiblingDB('usersdb');
db.createCollection('users');
