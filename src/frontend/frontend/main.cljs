(ns frontend.main
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [clojure.string :as str]
            [clojure.math :refer [round]]))

(defn search-city-component [on-city]
  (let [user-input (r/atom "")]
    [:form {:on-submit (fn [e] (.preventDefault e) (on-city @user-input))}
     [:input {:type "text"
              :on-change (fn [e] (reset! user-input (.-value (.-target e))))
              :placeholder "City name"}]
     [:br][:br]
     [:button {:type "submit"} "Search"]]

    ))

(defn api-call [city on-result]
  (-> (js/fetch (str "http://api.openweathermap.org/data/2.5/weather?q="
                     city
                     "&units=metric&appid=14740e209afce5148fe91649253853e9"))
      (.then
       (fn [result] (.json result)))
      (.then (fn [body]
               (on-result (js->clj body))))))

(defn destructure-api-result [api-result]
  (let [{:strs [name]
         {:strs [temp feels_like humidity]} "main"
         {:strs [country]} "sys"
         {:strs [speed]} "wind"
         [{:strs [main description]}] "weather"} api-result]
    {:name name
     :temp temp
     :feels_like feels_like
     :humidity humidity
     :country country
     :speed speed
     :weather-main main
     :description description}))

{:clear "sun.png"
 :rain "rain.png"
 :clouds "clouds.png"}

(defn determine-image [weather]
  (case weather
    "Clear" "/images/sun.png"
    "Rain" "/images/rain.png"
    "Clouds" "/images/clouds.png"
    "Snow" "/images/snow.png"
    "default"
    )
  )

(defn app []
  (let [api-call-result (r/atom nil)]
    (fn []
      [:div
       [:h3 "FIND THE WEATHER"]
       (search-city-component (fn [city]
                                (api-call
                                 city
                                 (fn [weather-data]
                                   (reset! api-call-result weather-data)))))
       (if @api-call-result

         ;;destructuring of api-call-result map
         (let [{:keys [name temp feels_like humidity
                       country speed weather-main description]}
               (destructure-api-result @api-call-result)]

           [:div.search-return
            [:h1 (str name ", " country)]
            [:h1 (str (round temp) "°C")]
            [:h2 weather-main]
            [:img {:class "weather-img" :src (determine-image weather-main) :alt ""}]
            [:h2 description]
            [:ul
             [:li [:h2 (str "Feels like: " (round feels_like) "°C")]]
             [:li
              [:h2 (str "Wind: " (round speed) " km/h")]]
             [:li
              [:h2 (str "Humidity: " humidity "%")]]]]))])))

(defn ^:export run []
  (rdom/render [app] (js/document.getElementById "app")))

(run)
