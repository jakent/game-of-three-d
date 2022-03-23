(ns game-of-three-d.rules
  (:require [odoyle.rules :as o]))

(def rules
  (o/ruleset
    {::time
     [:what
      [::time ::total total]]

     ::neighbors
     [:what
      [::time ::total total]
      [main-cell ::x main-x]
      [main-cell ::y main-y]
      [neighbor ::x neighbor-x]
      [neighbor ::y neighbor-y]
      [neighbor ::alive? alive? {:then false}]
      :when
      (and (#{0 1} (Math/abs (- main-x neighbor-x)))
           (#{0 1} (Math/abs (- main-y neighbor-y)))
           (not= [main-x main-y] [neighbor-x neighbor-y]))
      :then
      (o/reset!
        (reduce (fn [session [id neighbor]]
                  (o/insert session id {::neighbors (count (filter :alive? neighbor))}))
                o/*session*
                (group-by :main-cell
                          (o/query-all o/*session* ::neighbors))))]

     ::cell
     [:what
      [id ::alive? alive?]]

     ::suffocate
     [:what
      [::time ::total total]
      [id ::neighbors neighbors]
      [id ::alive? alive?]
      :when
      (and alive?
           (> neighbors 3))
      :then
      (do
        (println ::suffocate id)
        (o/reset! (o/insert o/*session* id ::alive? false)))]

     ::starve
     [:what
      [::time ::total total]
      [id ::neighbors neighbors]
      [id ::alive? alive?]
      :when
      (and alive?
           (< neighbors 2))
      :then
      (do
        (println ::starve id total)
        (o/insert! id ::alive? false))]

     ::born
     [:what
      [::time ::total total]
      [id ::neighbors neighbors]
      [id ::alive? alive?]
      :when
      (and (= neighbors 3)
           (not alive?))
      :then
      (o/insert! id ::alive? true)]}))
