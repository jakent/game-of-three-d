(ns game-of-three-d.view
  (:require ["react" :as react]
            ["@react-three/fiber" :refer (Canvas useFrame)]
            ["@react-three/drei" :refer (Stars OrbitControls)]
            [odoyle.rules :as o]
            [game-of-three-d.rules :as rules]))

(defn box [{[x y z] :id alive? :alive?}]
  (when alive?
    [:mesh {:position [x y z]}
     [:boxGeometry {:args [1 1 1]}]
     [:meshStandardMaterial {:color "white"}]]))

(defn grid [app-state]
  (let [{:keys [session]} @app-state
        cells (o/query-all session ::rules/cell)]
    (println cells)
    (into [:<>]
          (for [cell cells]
            ^{:key (:id cell)}
            [box cell]))))

(defn hello [app-state]
  [:> Canvas
   [:> OrbitControls]
   [:> Stars]
   [:ambientLight {:intensity 0.5}]
   [:spotLight {:position [10 15 10]
                :angle    0.3}]
   [grid app-state]])
