## todo app

First step, setup postgres, start a socket REPL and connect to it from my IDE (Atom w/ Chlorine).

```
Starting fullstack-todo_db_1 ... done
Attaching to fullstack-todo_db_1
db_1  | Error: Database is uninitialized and superuser password is not specified.
db_1  |        You must specify POSTGRES_PASSWORD to a non-empty value for the
db_1  |        superuser. For example, "-e POSTGRES_PASSWORD=password" on "docker run".
db_1  |
db_1  |        You may also use "POSTGRES_HOST_AUTH_METHOD=trust" to allow all
db_1  |        connections without a password. This is *not* recommended.
db_1  |
db_1  |        See PostgreSQL documentation about "trust":
db_1  |        https://www.postgresql.org/docs/current/auth-trust.html
fullstack-todo_db_1 exited with code 1
```

Added some env variables to docker-compose, success!

Started playing around in `todo.server`, created an items table with `jdbc/execute!` and added some items. `jdbc.sql` has some helpers for CRUD operations, that seems a good middle ground between building SQL strings and pulling in a dedicated library like HoneySQL or HugSQL.
