import sqlite3

conn = sqlite3.connect('vessel_objects.db')

c = conn.cursor()

c.execute("DROP TABLE IF EXISTS Classes;")
c.execute("DROP TABLE IF EXISTS Constructors;")
c.execute("DROP TABLE IF EXISTS Args;")
c.execute("DROP TABLE IF EXISTS Fields;")
c.execute("DROP TABLE IF EXISTS Extends;")

c.execute("CREATE TABLE Classes (name NOT_NULL TEXT PRIMARY KEY);")
c.execute("CREATE TABLE Constructors (id INTEGER PRIMARY KEY AUTOINCREMENT, number INTEGER, class_name TEXT, FOREIGN KEY(class_name) REFERENCES Classes(name));")
c.execute("CREATE TABLE Args (id INTEGER PRIMARY KEY AUTOINCREMENT, number INTEGER, class_name TEXT, constructor_id INTEGER, FOREIGN KEY(class_name) REFERENCES Classes(name), FOREIGN KEY(constructor_id) REFERENCES Constructors(id));")
c.execute("CREATE TABLE Fields (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, declaring_class_name TEXT, type_class_name TEXT, FOREIGN KEY(declaring_class_name) REFERENCES Classes(name), FOREIGN KEY(type_class_name) REFERENCES Classes(name));")
c.execute("CREATE TABLE Extends (id INTEGER PRIMARY KEY AUTOINCREMENT, child_class_name TEXT, parent_class_name TEXT, FOREIGN KEY(child_class_name) REFERENCES Classes(name), FOREIGN KEY(parent_class_name) REFERENCES Classes(name));")

conn.commit()
conn.close()