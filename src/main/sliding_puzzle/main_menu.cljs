(ns sliding-puzzle.main-menu
  (:require
   [quil.core :as q]
   [sliding-puzzle.game :as game]
   [sliding-puzzle.sketch-functions :refer [add-derived-state draw-state
                                            key-pressed mouse-dragged mouse-pressed
                                            update-state]]))

(defn mk-state [canvas-size]
  (add-derived-state
   {:type :main-menu
    :canvas-size canvas-size}))

(defmethod add-derived-state :main-menu
  [{:keys [canvas-size] :as state}]
  (assoc state
         :buttons
         [{:rect (let [p #(* canvas-size %)]
                   (map p [0.4 0.45 0.6 0.55]))
           :fn (fn [state] state)}]))
(defmethod mouse-dragged :main-menu
  [state]
  state)

(defmethod update-state :main-menu
  [state]
  state)

(defmethod draw-state :main-menu
  [{:keys [canvas-size]}]
  (q/background 240)
  (q/fill 50)
  (q/text-size (/ canvas-size 8))
  (q/text-align :center :center)
  (q/text "Sliding Puzzle" (/ canvas-size 2) (/ canvas-size 2)))

(defmethod mouse-pressed :main-menu
  [state]
  state)

(defmethod key-pressed :main-menu
  [state event]
  (println (:key event))
  (case (:key event)
    :space (game/mk-state (:canvas-size state)
                          3
                          [[0 0] [1 0] [0 1]])
    state))

