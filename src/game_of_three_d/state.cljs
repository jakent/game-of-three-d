(ns game-of-three-d.state
  (:require [game-of-three-d.rules :as rules]
            [odoyle.rules :as o]
            [reagent.core :as r]))

(def blinker #{[0 1] [1 1] [2 1]})
(def boat #{[1 1] [2 1] [1 2] [3 2] [2 3]})
(def glider #{[1 0] [2 1] [0 2] [1 2] [2 2]})
(def beacon #{[1 1] [2 1] [1 2] [4 3] [3 4] [4 4]})
(def r-pentomino #{[7 11] [6 12] [7 12] [8 12] [6 13]})
(def combination #{[0 12] [1 12] [2 12] [1 6] [2 7] [0 8] [1 8] [2 8]})
(def gosper-glider-gun #{[1 5] [2 5] [1 6] [2 6] [11 5] [11 6] [11 7] [12 4]
                         [12 8] [13 3] [13 9] [14 3] [14 9] [15 6] [16 4]
                         [16 8] [17 5] [17 6] [17 7] [18 6] [21 3] [21 4]
                         [21 5] [22 3] [22 4] [22 5] [23 2] [23 6] [25 1]
                         [25 2] [25 6] [25 7] [35 3] [35 4] [36 3] [36 4]})

(defn random-cells []
  (set (take 500 (repeatedly #(vector (rand-int 100) (rand-int 100))))))

(defn init-cells [session]
  (reduce (fn [session1 coordinate]
            (o/insert session1 coordinate {::rules/neighbors (set (rules/find-neighbors coordinate))}))
          session
          (random-cells)))

(defn create-session []
  (-> (reduce o/add-rule (o/->session) rules/rules)
      (o/insert ::rules/settings {::rules/ruleset [2 3 3 3]})
      ;(o/insert ::rules/settings {::rules/ruleset [1 1 3 3]})
      init-cells
      o/fire-rules))

(defonce app-state
         (r/atom {:session (create-session)}))

(defn find-next-tick []
  (-> (:session @app-state)
      (o/query-all ::rules/time)
      first
      :total
      inc))

(defn tick []
  (swap! app-state update :session
         (fn [session] (o/fire-rules
                         (o/insert session ::rules/time ::rules/total (find-next-tick)))))
  :done)

(defn start-life []
  (tick)
  (swap! app-state assoc :interval (js/setInterval tick
                                                   50)))

(defn pause-life [interval]
  #(do (js/clearInterval interval)
       (swap! app-state dissoc :interval)))
