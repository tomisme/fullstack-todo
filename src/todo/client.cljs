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
  (swap! state* assoc :items (sort-by :items/id items)))

(defn get-all-items []
  (-> (js/fetch "http://localhost:8081/items")
      (p/then (fn [res] (.text res)))
      (p/then #(handle-new-todos (deserialize %)))))

(defn put-item [item]
  (-> (js/fetch (str "http://localhost:8081/item/" (:items/id item))
                (clj->js {:method "PUT"
                          :headers {"Content-Type" "application/edn"}
                          :body (serialize item)}))
      (p/then (fn [res] (.text res)))
      (p/then #(handle-new-todos (deserialize %)))))

(defn delete-item [item]
  (-> (js/fetch (str "http://localhost:8081/item/" (:items/id item))
                (clj->js {:method "DELETE"}))
      (p/then (fn [res] (.text res)))
      (p/then #(handle-new-todos (deserialize %)))))

(defn post-new-item []
  (-> (js/fetch "http://localhost:8081/items"
                (clj->js {:method "POST"
                          :headers {"Content-Type" "application/edn"}
                          :body (serialize {:items/title "Test!"
                                            :items/description "hellohello"})}))
      (p/then (fn [res] (.text res)))
      (p/then #(handle-new-todos (deserialize %)))))

(defn app-component []
  [:section
   [:h1 "Fullstack Todo"]
   [:button {:on-click #(get-all-items)}
    "fetch todos"]
   [:div
    [:h2 "New Todo"]]
   [:button {:on-click #(post-new-item)}
    "add new todo"]
   (into [:div]
         (for [{:keys [items/title items/description] :as item}
               (:items @state*)]
           [:div {:style {:border "1px solid"
                          :margin 10}}
            [:h3 title]
            [:pre (prn-str item)]
            [:div description]
            [:button {:on-click #(put-item (assoc item :items/complete true))}
             "Complete"]
            [:button {:on-click #(delete-item item)}
             "Delete"]]))])

(defn main []
  (rd/render [app-component]
             (.getElementById js/document "app")))

(main)
