(ns sliding-puzzle.main-menu
  (:require
   [quil.core :as q]
   [sliding-puzzle.buttons :as buttons]
   [sliding-puzzle.game :as game]
   [sliding-puzzle.sketch-functions :refer [add-derived-state draw-state
                                            key-pressed mouse-dragged mouse-pressed
                                            update-state]]
   [sliding-puzzle.levels :as levels]))

(defn mk-state [canvas-size]
  (add-derived-state
   {:type :main-menu
    :canvas-size canvas-size}))

(defmethod add-derived-state :main-menu
  [{:keys [canvas-size] :as state}]
  (assoc state
         :buttons
         [{:rect (let [p #(* canvas-size %)]
                   (map p [0.4 0.45 0.2 0.1]))
           :fn (fn [state] (add-derived-state
                            (assoc (first levels/levels)
                                   :canvas-size (:canvas-size state))))
           :text "Play"
           :text-size (* canvas-size 0.04)}]))

(defmethod mouse-dragged :main-menu
  [state]
  state)

(defmethod update-state :main-menu
  [state]
  state)

(defmethod draw-state :main-menu
  [{:keys [canvas-size] :as state}]
  (q/background 240)
  (q/fill 50)
  (q/text-size (/ canvas-size 8))
  (q/text-align :center :center)
  (q/text "Sliding Puzzle" (* canvas-size 0.5) (* canvas-size 0.3))
  (buttons/draw-buttons state))

(defmethod mouse-pressed :main-menu
  [state {:keys [x y]}]
  (if-let [button-fn (buttons/get-clicked-button-fn state x y)]
    (button-fn state)
    state))

(defmethod key-pressed :main-menu
  [state event]
  (case (:key event)
    :space (add-derived-state
            (assoc (first levels/levels)
                   :canvas-size (:canvas-size state)))
    state))

