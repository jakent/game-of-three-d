(ns game-of-three-d.rules
  (:require [odoyle.rules :as o]))

(defn find-neighbors [[x y]]
  (for [x2 [-1 0 1]
        y2 [-1 0 1]
        :when (not= x2 y2 0)]
    (vector (+ x x2) (+ y y2))))

(def rules
  (o/ruleset
    {::time
     [:what
      [::time ::total total]]

     ::cell
     [:what
      [id ::alive? alive?]]

     ::neighbors
     [:what
      [::time ::total total]
      [main-cell ::x main-x {:then false}]
      [main-cell ::y main-y {:then false}]
      [neighbor ::x neighbor-x {:then false}]
      [neighbor ::y neighbor-y {:then false}]
      [neighbor ::alive? alive? {:then false}]
      :when
      (and (#{0 1} (Math/abs (- main-x neighbor-x)))
           (#{0 1} (Math/abs (- main-y neighbor-y)))
           (not= [main-x main-y] [neighbor-x neighbor-y]))
      :then
      (do (println "::neighbors")
          (o/reset!
            (reduce (fn [session [id neighbor]]
                      (o/insert session id {::neighbors (filter :alive? neighbor)}))
                    o/*session*
                    (group-by :main-cell
                              (o/query-all o/*session* ::neighbors)))))]

     ::create-dead-neighbors
     [:what
      [::time ::total total]
      [id ::alive? alive? {:then false}]
      :when
      (true? alive?)
      :then
      (let [living (o/query-all o/*session* ::create-dead-neighbors)]
        (o/reset!
          (reduce (fn [session [x y :as neighbor]]
                    (o/insert session neighbor {::x x ::y y ::alive? false}))
                  o/*session*
                  (remove (set (map :id living))
                          (find-neighbors id)))))]

     ::bury-dead
     [:what
      [id ::neighbors neighbors]
      :when
      (zero? (count neighbors))
      :then
      (do (println ::bury-dead id)
          (-> o/*session*
              (o/retract id ::x)
              (o/retract id ::y)
              (o/retract id ::alive?)
              (o/retract id ::neighbors)
              o/reset!))]

     ::suffocate
     [:what
      [::time ::total total]
      [id ::neighbors neighbors]
      [id ::alive? alive?]
      :when
      (and alive?
           (> (count neighbors) 3))
      :then
      (do (println ::suffocate id)
          (o/reset! (o/insert o/*session* id ::alive? false)))]

     ::starve
     [:what
      [::time ::total total]
      [id ::neighbors neighbors]
      [id ::alive? alive?]
      :when
      (and alive?
           (< (count neighbors) 2))
      :then
      (do (println ::starve id)
          (o/insert! id ::alive? false))]

     ::born
     [:what
      [::time ::total total]
      [id ::neighbors neighbors]
      [id ::alive? alive?]
      :when
      (and (= (count neighbors) 3)
           (not alive?))
      :then
      (do (println ::born id)
          (o/insert! id ::alive? true))]}))
