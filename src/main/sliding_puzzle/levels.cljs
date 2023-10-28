(ns sliding-puzzle.levels
  (:require
   [sliding-puzzle.sketch-functions :refer [add-derived-state]]
   [sliding-puzzle.game :as game]))

(def levels-raw
  [{:size 3
    :pieces [[0 0] [1 0] [0 1]]
    :won? (fn [{:keys [pieces]}]
            (get pieces [1 1]))}
   {:size 4
    :pieces [[0 0] [1 0] [0 1]]
    :won? (fn [{:keys [pieces]}]
            (->> [[1 1] [1 2] [2 1] [2 2]]
                 (keep (partial get pieces))
                 count
                 pos?))}
   {:size 4
    :pieces [[0 0] [1 0] [0 1]]
    :won? (fn [{:keys [pieces]}]
            (->> [[1 1] [1 2] [2 1] [2 2]]
                 (keep (partial get pieces))
                 count
                 dec
                 pos?))}
   {:size 5
    :pieces [[0 0] [1 0] [0 1]]
    :won? (fn [{:keys [pieces]}]
            (get pieces [2 2]))}])

(def levels
  (reverse
    (rest
      (reductions
        (fn [next-level {:keys [size pieces won?]}]
          (game/mk-state 0 size pieces won? next-level))
        nil
        (reverse levels-raw)))))

(defn unfreeze [level canvas-size]
  (println levels)
  (add-derived-state
   (assoc level
          :canvas-size canvas-size)))
