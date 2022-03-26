(ns game-of-three-d.view
  (:require ["@react-three/fiber" :refer (Canvas useFrame)]
            ["@react-three/drei" :refer (Stars OrbitControls)]
            [game-of-three-d.rules :as rules]
            [game-of-three-d.state :as state]
            [odoyle.rules :as o]
            [reagent.core :as r]))

(defn box [{[x y z] :coordinate}]
  [:mesh {:position [x y z]}
   [:boxGeometry {:args [1 1 1]}]
   [:meshStandardMaterial {:color "white"}]])

(defn grid []
  (let [session @(r/cursor state/app-state [:session])
        cells   (o/query-all session ::rules/cells)]
    (into [:<>]
          (for [cell cells]
            ^{:key (:coordinate cell)}
            [box cell]))))

(defn canvas []
  [:> Canvas
   [:> OrbitControls]
   ;[:> Stars]
   [:ambientLight {:intensity 0.5}]
   [:spotLight {:position [10 15 10]
                :angle    0.3}]
   [grid]])

(defn control-panel []
  (let [interval @(r/cursor state/app-state [:interval])]
    [:div {:style {:position         :absolute
                   :top              20
                   :left             30
                   :background-color :white}}
     [:button {:disabled (some? interval)
               :on-click state/tick}
      "Tick"]
     (if interval
       [:button {:on-click (state/pause-life interval)}
        "Stop"]
       [:button {:on-click state/start-life}
        "Start"])]))

(defn hello []
  [:<>
   [canvas]
   [control-panel]])
