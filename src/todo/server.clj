(ns todo.server
 (:require
  [next.jdbc :as jdbc]
  [next.jdbc.sql :as sql]
  [next.jdbc.specs :as specs]))

(specs/instrument)

(def ds (jdbc/get-datasource {:dbtype "postgres"
                              :dbname "postgres"
                              :user "postgres"
                              :password "postgres"}))

(def ex! (partial jdbc/execute! ds))

#_(ex! ["
CREATE TABLE items (
  id serial,
  title text,
  description text
)
"])
#_(ex! ["
INSERT INTO items (
  title,
  description
) VALUES (
  'hello',
  'world'
)"])
#_(ex! ["SELECT * FROM items"])
#_(ex! ["DROP TABLE items"])


#_(sql/get-by-id ds :items 2)
#_(sql/insert! ds :items {:title "Cook eggs"
                          :description "scrambled"})
