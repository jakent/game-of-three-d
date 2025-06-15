(ns game-of-three-d.state
  (:require [game-of-three-d.rules :as rules]
            [odoyle.rules :as o]
            [reagent.core :as r]))

;; 2333
(def blinker #{[0 1] [1 1] [2 1]})
(def boat #{[1 1] [2 1] [1 2] [3 2] [2 3]})
(def glider #{[1 0] [2 1] [0 2] [1 2] [2 2]})
(def beacon #{[1 1] [2 1] [1 2] [4 3] [3 4] [4 4]})
(def r-pentomino #{[7 11] [6 12] [7 12] [8 12] [6 13]})
(def combination #{[0 12] [1 12] [2 12] [1 6] [2 7] [0 8] [1 8] [2 8]})
(def gosper-glider-gun #{[7 9] [-14 10] [6 10] [10 6] [-2 14] [-1 8] [-4 12] [8 11] [2 11]
                         [21 8] [3 11] [10 12] [1 9] [-3 9] [-3 13] [10 11] [0 11] [-13 11]
                         [2 12] [20 8] [-4 10] [20 9] [-2 8] [-1 14] [7 10] [1 13] [-14 11]
                         [7 8] [-4 11] [6 8] [8 7] [6 9] [2 10] [21 9] [10 7] [-13 10]})

;; 3444
(def glider-3444 #{[0 0 0] [0 1 0] [1 2 0] [2 2 0] [3 1 0] [3 0 0] [1 0 1] [2 0 1] [1 1 1] [2 1 1]})
(def stable #{[8 4 6] [7 4 7] [8 4 8] [8 5 7] [8 3 7] [9 4 7]})
(def oscillator #{[6 3 9] [5 3 10] [7 3 9] [5 3 9] [6 3 10]})

(defn move [[dx dy dz] positions]
  (map (fn [[x y z]]
         [(+ x dx) (+ y dy) (+ z dz)])
       positions))

(defn take-rand [n coll]
  (take n (shuffle coll)))

(defn random-cells [n]
  (take-rand (* n n n 0.5)
             (for [x (range n)
                   y (range n)
                   z (range n)]
               (vector x y z))))

(defn init-cells [session]
  (reduce (fn [session1 coordinate]
            (o/insert session1 coordinate {::rules/neighbors (set (rules/find-neighbors coordinate))}))
          session
          (move [0 -5 0] (random-cells 10))))

(defn create-session []
  (-> (reduce o/add-rule (o/->session) rules/rules)
      (o/insert ::rules/settings {::rules/ruleset [2 3 3 3]})
      ;(o/insert ::rules/settings {::rules/ruleset [1 1 3 3]})

      ;(o/insert ::rules/settings {::rules/ruleset [4 5 5 5]})
      ;(o/insert ::rules/settings {::rules/ruleset [5 6 5 5]})
      ;(o/insert ::rules/settings {::rules/ruleset [5 7 6 6]})
      ;(o/insert ::rules/settings {::rules/ruleset [10 21 10 21]})
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
                                                   100)))

(defn pause-life [interval]
  #(do (js/clearInterval interval)
       (swap! app-state dissoc :interval)))

(defn restart []
  (swap! app-state assoc :session (create-session)))
