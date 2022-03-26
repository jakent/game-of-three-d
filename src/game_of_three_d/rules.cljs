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

     ::cells
     [:what
      [coordinate ::neighbors neighbors]
      :then-finally
      (let [cells  (o/query-all o/*session* ::cells)
            living (set (map :coordinate cells))
            census (->> (mapcat :neighbors cells)
                        frequencies)]
        (println ::cells {:living living :census census})
        (-> (o/insert! ::derived {::living living
                                  ::census census})))]

     ::suffocate
     [:what
      [::time ::total total]
      [::derived ::census census {:then false}]
      [::settings ::ruleset ruleset {:then false}]
      [coordinate ::neighbors neighbors {:then false}]
      :when
      (> (census coordinate)
         (nth ruleset 1))
      :then
      (do (println ::suffocate coordinate)
          (o/retract! coordinate ::neighbors))]

     ::starve
     [:what
      [::time ::total total]
      [::derived ::census census {:then false}]
      [::settings ::ruleset ruleset {:then false}]
      [coordinate ::neighbors neighbors {:then false}]
      :when
      (< (census coordinate)
         (nth ruleset 0))
      :then
      (do (println ::starve coordinate)
          (o/retract! coordinate ::neighbors))]

     ::born
     [:what
      [::time ::total total]
      [::derived ::census census {:then false}]
      [::derived ::living living {:then false}]
      [::settings ::ruleset ruleset {:then false}]
      :then
      (o/reset!
        (reduce (fn [session [coordinate alive-neighbors]]
                  (if (and (<= (nth ruleset 2) alive-neighbors (nth ruleset 3))
                           (not (living coordinate)))
                    (do (println ::born coordinate)
                        (o/insert session coordinate ::neighbors (set (find-neighbors coordinate))))
                    session))
                o/*session*
                census))]}))
