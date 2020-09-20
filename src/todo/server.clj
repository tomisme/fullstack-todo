(ns todo.server
 (:require
  [compojure.core :refer [defroutes GET DELETE POST PUT]]
  [compojure.handler :as handler]
  [compojure.route :as route]
  [next.jdbc :as jdbc]
  [next.jdbc.sql :as sql]
  [next.jdbc.specs :as specs]
  [ring.adapter.jetty :refer [run-jetty]]
  [ring.middleware.reload :refer [wrap-reload]]
  [ring.middleware.cors :refer [wrap-cors]]
  [ring.middleware.edn :refer [wrap-edn-params]]
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

(def all-items-query
  ["SELECT * FROM items"])

(def create-table-query ["
CREATE TABLE IF NOT EXISTS items (
  id serial,
  title text,
  description text,
  complete boolean DEFAULT false
)"])

(defn all-items-response []
  (edn-response (ex! all-items-query)))

(defroutes routes
  (GET "/" [] index-response)

  (GET "/items" [] (all-items-response))

  (POST "/items" {:keys [edn-params]}
        (let [{:keys [items/title items/description]} edn-params]
          (sql/insert! ds :items
                       {:items/title title
                        :items/description description})
          (all-items-response)))

  (DELETE "/item/:id" [id]
          (do
            (sql/delete! ds :items
                         {:items/id (Integer/parseInt id)})
            (all-items-response)))

  (PUT "/item/:id" {:keys [edn-params]}
       (let [{:keys [items/id
                     items/title
                     items/description
                     items/complete]} edn-params]
         (sql/update! ds :items
                      {:items/title title
                       :items/description description
                       :items/complete complete}
                      {:items/id id})
         (all-items-response)))

  (route/not-found "Nothing here!"))

(def app (-> routes
             handler/site
             wrap-edn-params
             (wrap-cors :access-control-allow-origin [#".*localhost.*"]
                        :access-control-allow-methods [:get :put :post :delete])))

(defn serve []
  (let [port 8081]
    (println "Starting server on port: " port)
    (run-jetty (wrap-reload #'app)
               {:port port
                :join? false})))

(defn -main []
  (ex! create-table-query)
  (reset! server (serve)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

#_@server
#_(reset! server (serve))
#_(.stop @server)
#_(.start @server)

#_(ex! ["
CREATE TABLE IF NOT EXISTS items (
  id serial,
  title text,
  description text,
  complete boolean DEFAULT false
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
#_(sql/delete! ds :items {:items/id 1})
