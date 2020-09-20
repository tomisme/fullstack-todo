(ns todo.client
  (:require
   [cljs.reader]
   [reagent.core :as rg]
   [reagent.dom :as rd]
   [promesa.core :as p]))

(enable-console-print!)

(defonce state* (rg/atom {:items []}))

(defn serialize
  [data]
  (pr-str data))

(defn deserialize
  [text]
  (cljs.reader/read-string text))

(defn handle-new-todos
  [items]
  (swap! state* assoc :items (sort-by :items/id (deserialize items))))

(defn extract-res-text [res] (.text res))

(defn get-all-items []
  (-> (js/fetch "http://localhost:8081/items")
      (p/then extract-res-text)
      (p/then handle-new-todos)))

(defn put-item [item]
  (-> (js/fetch (str "http://localhost:8081/item/" (:items/id item))
                (clj->js {:method "PUT"
                          :headers {"Content-Type" "application/edn"}
                          :body (serialize item)}))
      (p/then extract-res-text)
      (p/then handle-new-todos)))

(defn delete-item [item]
  (-> (js/fetch (str "http://localhost:8081/item/" (:items/id item))
                (clj->js {:method "DELETE"}))
      (p/then extract-res-text)
      (p/then handle-new-todos)))

(defn post-new-item [item]
  (-> (js/fetch "http://localhost:8081/items"
                (clj->js {:method "POST"
                          :headers {"Content-Type" "application/edn"}
                          :body (serialize item)}))
      (p/then extract-res-text)
      (p/then handle-new-todos)))


;;


(defn new-todo-create-btn-handler []
  (let [title-el (.getElementById js/document "new-title")
        description-el (.getElementById js/document "new-description")
        title (.-value title-el)
        description (.-value description-el)]
    (set! (.-value title-el) "")
    (set! (.-value description-el) "")
    (post-new-item {:items/title title
                    :items/description description})))

(defn new-item-component []
  [:div {:style {:margin 10
                 :padding 10}}
   [:h2 "New Todo"]
   [:h3 "Title"]
   [:textarea#new-title]
   [:h3 "Description"]
   [:textarea#new-description]
   [:div
    [:button {:style {:margin-top 10}
              :on-click new-todo-create-btn-handler}
     "Create"]]])

(defn item-list-component []
  (into [:div]
        (for [{:keys [items/title items/description items/complete] :as item}
              (:items @state*)]
          [:div {:style {:border "1px solid"
                         :margin 10
                         :padding 10}}
           [:h3 {:style {:text-decoration (when complete "line-through")}}
            title]
           [:div {:style {:margin-bottom 20}}
            description]
           [:button {:on-click #(put-item (assoc item :items/complete (not complete)))}
            (if complete "Un-complete" "Complete")]
           [:button {:on-click #(delete-item item)}
            "Delete"]])))

(defn app-component []
  [:section {:style {:max-width 600
                     :margin 10}}
   [:h1 "Fullstack Todo App"]
   [new-item-component]
   [item-list-component]])

(defn main []
  (when (empty? (:items @state*))
    (get-all-items))
  (rd/render [app-component]
             (.getElementById js/document "app")))

(main)
