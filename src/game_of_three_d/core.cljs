(ns game-of-three-d.core
  (:require [game-of-three-d.rules :as rules]
            [game-of-three-d.view :as view]
            [odoyle.rules :as o]
            [reagent.core :as r]
            [reagent.dom :as rdom]))

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

(defn init-cells [session start-cells size]
  (reduce (fn [session1 [x y :as coordinate]]
            (o/insert session1 coordinate {::rules/x      x
                                           ::rules/y      y
                                           ::rules/alive? (some? (start-cells coordinate))}))
          session
          (for [x (range size)
                y (range size)]
            [x y])))

(defn create-session []
  (-> (reduce o/add-rule (o/->session) rules/rules)
      (o/insert ::ruleset {::foo 2333})
      (init-cells blinker 5)
      o/fire-rules))

(defonce app-state
         (r/atom {:session (create-session)}))

(defn tick [n]
  (swap! app-state update :session
         (fn [session] (o/fire-rules
                         (o/insert session ::rules/time ::rules/total n))))
  :done)

(defn ^:dev/after-load mount-root []
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [view/hello app-state] root-el)))

(defn ^:export init []
  (mount-root))
