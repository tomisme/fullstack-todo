(ns todo.client
  (:require
   [cljs.reader]
   [reagent.core :as rg]
   [reagent.dom :as rd]
   [promesa.core :as p]))

(enable-console-print!)

(def state* (rg/atom {:items []}))

(defn serialize
  [data]
  (pr-str data))

(defn deserialize
  [text]
  (cljs.reader/read-string text))

(defn handle-new-todos
  [items]
  (swap! state* assoc :items items))

(defn fetch-all-items []
  (-> (js/fetch "http://localhost:8081/items" (clj->js {}))
      (p/then (fn [res]
                (-> (.text res)
                    (p/then #(handle-new-todos (deserialize %))))))))

(defn app-component []
  [:section
   [:h1 "Fullstack Todo"]
   [:button {:on-click #(fetch-all-items)}
    "fetch todos"]
   (into [:div]
         (for [item (:items @state*)]
           [:div [:pre (prn-str item)]]))])

(defn main []
  (rd/render [app-component]
             (.getElementById js/document "app")))

(main)
