(ns sliding-puzzle.levels
  (:require
   [sliding-puzzle.sketch-functions :refer [add-derived-state]]
   [sliding-puzzle.game :as game]))

(def levels-raw
  [{:size 3
    :pieces [[0 0] [1 0] [0 1]]
    :won? (fn [{:keys [pieces]}]
            (get pieces [1 1]))
    :goal-text "Move a piece to the middle square"}
   {:size 4
    :pieces [[0 0] [1 0] [0 1]]
    :won? (fn [{:keys [pieces]}]
            (->> [[1 1] [1 2] [2 1] [2 2]]
                 (keep (partial get pieces))
                 count
                 pos?))
    :goal-text "Move a piece to the central 2x2 subgrid"}
   {:size 4
    :pieces [[0 0] [1 0] [0 1]]
    :won? (fn [{:keys [pieces]}]
            (->> [[1 1] [1 2] [2 1] [2 2]]
                 (keep (partial get pieces))
                 count
                 dec
                 pos?))
    :goal-text "Move 2 pieces to the central 2x2 subgrid"}
   {:size 5
    :pieces [[0 0] [1 0] [0 1]]
    :won? (fn [{:keys [pieces]}]
            (get pieces [2 2]))
    :goal-text "Move a piece to the middle square"}])

(def levels
  (reverse
    (rest
      (reductions
        (fn [next-level {:keys [size pieces won? goal-text]}]
          (game/mk-state 0 size pieces won? goal-text next-level))
        nil
        (reverse levels-raw)))))

