(ns todo.server
 (:require
  [compojure.core :refer [defroutes GET]]
  [compojure.handler :as handler]
  [compojure.route :as route]
  [next.jdbc :as jdbc]
  [next.jdbc.sql :as sql]
  [next.jdbc.specs :as specs]
  [ring.adapter.jetty :refer [run-jetty]]
  [ring.middleware.reload :refer [wrap-reload]]
  [ring.middleware.cors :refer [wrap-cors]]
  [ring.util.response :refer [resource-response content-type]]))

(specs/instrument)

;; next steps, use component/integrant/mount etc.
(defonce server (atom nil))

(def ds (jdbc/get-datasource {:dbtype "postgres"
                              :dbname "postgres"
                              :user "postgres"
                              :password "postgres"}))

(def ex! (partial jdbc/execute! ds))

(defn edn-response
  [data]
  {:status 200
   :headers {"Content-Type" "application/edn"}
   :body (prn-str data)})

(def index-response
  (content-type (resource-response "index.html" {:root "public"})
                "text/html"))

(defroutes routes
  (GET "/" [] index-response)
  (GET "/items" [] (edn-response (ex! ["SELECT * FROM items"])))
  (route/not-found "Nothing here!"))

(def app (-> routes
             handler/site
             (wrap-cors :access-control-allow-origin [#".*localhost.*"]
                        :access-control-allow-methods [:get :put :post :delete])))

(defn serve []
  (let [port 8081]
    (println "Starting server on port: " port)
    (run-jetty (wrap-reload #'app)
               {:port port
                :join? false})))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

#_@server
#_(reset! server (serve))
#_(.stop @server)
#_(.start @server)

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
